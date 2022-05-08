package polyrhythmmania.engine.input

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import paintbox.Paintbox
import paintbox.binding.*
import paintbox.lazysound.LazySound
import paintbox.registry.AssetRegistry
import polyrhythmmania.Localization
import polyrhythmmania.PRManiaGame
import polyrhythmmania.engine.*
import polyrhythmmania.engine.music.MusicVolume
import polyrhythmmania.gamemodes.SidemodeAssets
import polyrhythmmania.soundsystem.BeadsSound
import polyrhythmmania.statistics.GlobalStats
import polyrhythmmania.world.*
import polyrhythmmania.world.entity.EntityPiston
import kotlin.math.max


/**
 * Receives the player inputs.
 *
 * Input results are stored PER [EntityRodPR] as the input pattern is determined at runtime. See [EntityRodPR.InputTracker].
 *
 * **Flow:**
 *   - Pre-req: Inputs have to not be locked ([areInputsLocked] = `false`).
 *   - When a A or D-pad input is received, the appropriate pistons are triggered, assuming that inputs are not locked.
 *   - For each triggered piston:
 *     - For each [EntityRod] on the appropriate row for the piston:
 *       - Update its internal active blocks
 *       - Verify that the piston is the NEXT input for the rod
 *       - Check the timing from [InputThresholds]. If valid, then the rod can bounce and continue
 *
 *
 * **When a rod is killed after expiry:**
 *   - It sends its input data to this [EngineInputter]
 */
class EngineInputter(val engine: Engine) {
    
    companion object {
        const val DEBUG_LOG_INPUTS: Boolean = false
        const val BEAT_EPSILON: Float = 0.01f
    }
    
    data class RequiredInput(val beat: Float, val inputType: InputType) {
        var wasHit: Boolean = false
        var hitScore: InputScore = InputScore.MISS
    }
    
    class PracticeData {
        var practiceModeEnabled: Boolean = false
        val moreTimes: IntVar = IntVar(0)
        var requiredInputs: List<RequiredInput> = emptyList()
        
        var clearText: Float = 0f
        
        fun reset() {
            practiceModeEnabled = false
            requiredInputs = emptyList()
            moreTimes.set(0)
            clearText = 0f
        }
    }
    
    class PerfectChallengeData {
        var goingForPerfect: Boolean = false
        var hit: Float = 0f
        var failed: Boolean = false
        
        fun reset() {
            hit = 0f
            failed = false
        }
    }
    
    class InputChallengeData {
        var acesOnly: Boolean = false
        
        fun isInputScoreMiss(inputScore: InputScore): Boolean {
            if (inputScore == InputScore.MISS) return true
            if (acesOnly && inputScore != InputScore.ACE) return true
            return false
        }
    }
    
    class EndlessScore {
        val score: IntVar = IntVar(0)
        var highScore: Var<Int> = GenericVar(0)
        var showNewHighScoreAtEnd: Boolean = true
        var hideHighScoreText: Boolean = false
        val maxLives: IntVar = IntVar(0)
        val startingLives: IntVar = IntVar { maxLives.use() }
        val lives: IntVar = IntVar(startingLives.get())
        
        val gameOverSeconds: FloatVar = FloatVar(Float.MAX_VALUE)
        val gameOverUIShown: BooleanVar = BooleanVar(false)
        
        fun reset() {
            score.set(0)
            lives.set(startingLives.get())
            gameOverSeconds.set(Float.MAX_VALUE)
            gameOverUIShown.set(false)
        }
    }
    
    class InputCountStats {
        var total: Int = 0
        var missed: Int = 0
        var aces: Int = 0
        var goods: Int = 0
        var barelies: Int = 0
        var early: Int = 0
        var late: Int = 0
        
        fun reset() {
            total = 0
            missed = 0
            aces = 0
            goods = 0
            barelies = 0
            early = 0
            late = 0
        }
    }

    private val world: World = engine.world

    val inputCountStats: InputCountStats = InputCountStats()
    var areInputsLocked: Boolean = true
    var skillStarBeat: Float = Float.POSITIVE_INFINITY
    val practice: PracticeData = PracticeData()
    val perfectChallenge: PerfectChallengeData = PerfectChallengeData()
    val inputChallenge: InputChallengeData = InputChallengeData()
    val endlessScore: EndlessScore = EndlessScore()

    val inputFeedbackFlashes: FloatArray = FloatArray(5) { -10000f }
    var totalExpectedInputs: Int = 0
        private set
    var noMiss: Boolean = true
        private set
    var rodsExplodedPR: Int = 0
        private set
    var minimumInputCount: Int = 0
    
    val skillStarGotten: BooleanVar = BooleanVar(false)
    val inputResults: List<InputResult> = mutableListOf()
    val expectedInputsPr: List<EntityRodPR.ExpectedInput.Expected> = mutableListOf()
    
    init {
        reset()
    }
    
    fun clearInputs(beforeBeat: Float = Float.POSITIVE_INFINITY) {
        totalExpectedInputs = 0
        (inputResults as MutableList).removeIf { it.perfectBeat < beforeBeat }
        (expectedInputsPr as MutableList).removeIf { it.perfectBeat < beforeBeat }
        practice.requiredInputs = emptyList()
    }
    
    fun reset() {
        clearInputs()
        inputFeedbackFlashes.fill(-10000f)
        noMiss = true
        skillStarGotten.set(false)
        skillStarBeat = Float.POSITIVE_INFINITY
        practice.reset()
        perfectChallenge.reset()
        endlessScore.reset()
        rodsExplodedPR = 0
        inputCountStats.reset()
    }
    
    fun onButtonPressed(release: Boolean, type: InputType) {
        if (type == InputType.A) {
            val activeTextBox = engine.activeTextBox
            if (activeTextBox != null && activeTextBox.textBox.requiresInput && activeTextBox.secondsTimer <= 0f) {
                if (!activeTextBox.isADown) {
                    if (!release) {
                        engine.soundInterface.playMenuSfx(AssetRegistry.get<Sound>("sfx_text_advance_1"))
                        activeTextBox.isADown = true
                    }
                } else {
                    if (release) {
                        engine.soundInterface.playMenuSfx(AssetRegistry.get<Sound>("sfx_text_advance_2"))
                        engine.removeActiveTextbox(unpauseSoundInterface = true, runTextboxOnComplete = true)
                    }
                }
            } else {
                if (!release) {
                    onInput(InputType.A)
                }
            }
        } else {
            if (!release) {
                onInput(InputType.DPAD_ANY)
            }
        }
    }

    private fun onInput(type: InputType, atSeconds: Float = engine.seconds) {
        if (areInputsLocked || engine.activeTextBox?.textBox?.requiresInput == true) return
        
        val atBeat = engine.tempos.secondsToBeats(atSeconds)
        
        val worldMode = world.worldMode
        if (worldMode.worldType is WorldType.Polyrhythm) {
            val rowBlockType: EntityPiston.Type = if (type.isDpad) EntityPiston.Type.PISTON_DPAD else EntityPiston.Type.PISTON_A

            for (row in world.rows) {
                row.updateInputIndicators()
                val activeIndex = row.nextActiveIndex
                if (activeIndex !in 0 until row.length) continue
                val rowBlock = row.rowBlocks[activeIndex]
                if (rowBlock.type != rowBlockType) continue

                for (entity in engine.world.entities) {
                    if (entity !is EntityRodPR || entity.row !== row || !entity.acceptingInputs) continue
                    val rod: EntityRodPR = entity
                    rod.updateInputTracking(atBeat)
                    val inputTracker = rod.inputTracker
                    val nextBlockIndex = entity.getCurrentIndexFloor(entity.getPosXFromBeat(atBeat - entity.deployBeat))
                    if (nextBlockIndex !in 0 until entity.row.length) continue // Not in range
                    if (inputTracker.expected[nextBlockIndex] !is EntityRodPR.ExpectedInput.Expected) continue // Not an expected input
                    if (nextBlockIndex != activeIndex) { // Not the current active index
//                    Paintbox.LOGGER.debug("$rod: Skipping input because nextBlockIndex != activeIndex (${nextBlockIndex} >= ${activeIndex})")
                        if (activeIndex < nextBlockIndex) {
                            missed()
                        }
                        continue
                    }
                    if (inputTracker.results.any { it.expectedIndex == nextBlockIndex }) { // Prevent potential duplicates
                        // Technically shouldn't happen since the active index changes when updateInputIndicators is called
                        Paintbox.LOGGER.debug("$rod: duplicate input for index $nextBlockIndex caught and ignored!")
                        continue
                    }

                    // Compare timing
                    val perfectBeats = rod.getBeatForIndex(nextBlockIndex)
                    val perfectSeconds = engine.tempos.beatsToSeconds(perfectBeats)

                    val differenceSec = atSeconds - perfectSeconds
                    val minSec = perfectSeconds - InputThresholds.MAX_OFFSET_SEC
                    val maxSec = perfectSeconds + InputThresholds.MAX_OFFSET_SEC

                    val accuracyPercent = (differenceSec / InputThresholds.MAX_OFFSET_SEC).coerceIn(-1f, 1f)
                    val inputResult = InputResult(perfectBeats, type, accuracyPercent, differenceSec, activeIndex)
                    if (DEBUG_LOG_INPUTS) {
                        Paintbox.LOGGER.debug("${rod.toString().substringAfter("polyrhythmmania.world.Entity")}: Input ${type}: ${if (differenceSec < 0) "EARLY" else if (differenceSec > 0) "LATE" else "PERFECT"} ${inputResult.inputScore} \t | perfectBeat=$perfectBeats, perfectSec=$perfectSeconds, diffSec=$differenceSec, minmaxSec=[$minSec, $maxSec], actualSec=$atSeconds")
                    }
                    inputTracker.results += inputResult

                    if (practice.practiceModeEnabled && !inputChallenge.isInputScoreMiss(inputResult.inputScore)) {
                        val allWereHit = practice.requiredInputs.all { it.wasHit }
                        if (!allWereHit) {
                            practice.requiredInputs.forEach { ri ->
                                if (!ri.wasHit && type.isInputEquivalent(ri.inputType) && MathUtils.isEqual(perfectBeats, ri.beat, BEAT_EPSILON)) {
                                    ri.wasHit = true
                                    ri.hitScore = inputResult.inputScore
                                }
                            }
                            if (practice.requiredInputs.all { it.wasHit }) {
                                val newValue = (practice.moreTimes.get() - 1).coerceAtLeast(0)
                                practice.moreTimes.set(newValue)
                                if (newValue == 0) {
                                    engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_practice_moretimes_2"), SoundInterface.SFXType.NORMAL)
                                    practice.clearText = 1f
                                } else {
                                    engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_practice_moretimes_1"), SoundInterface.SFXType.NORMAL)
                                }
                            }
                        }
                    }

                    val inputFeedbackIndex: Int = getInputFeedbackIndex(inputResult.inputScore, inputResult.accuracySec < 0f)
                    if (inputFeedbackIndex in inputFeedbackFlashes.indices) {
                        inputFeedbackFlashes[inputFeedbackIndex] = atSeconds
                    }

                    // Bounce the rod
                    if (!inputChallenge.isInputScoreMiss(inputResult.inputScore)) {
                        rod.bounce(nextBlockIndex)
                        if (inputResult.inputScore == InputScore.ACE) {
                            attemptSkillStar(perfectBeats)
                        }
                        if (perfectChallenge.goingForPerfect && !perfectChallenge.failed) {
                            perfectChallenge.hit = 1f
                        }
                    } else {
                        missed()
                    }
                }

                // Trigger this piston (will also call updateInputIndicators)
                rowBlock.fullyExtend(engine, atBeat)
            }
        } else if (worldMode.worldType == WorldType.Dunk && type == InputType.A) {
            for (entity in engine.world.entities) {
                if (entity !is EntityRodDunk || !entity.acceptingInputs) continue
                val rod: EntityRodDunk = entity
                val nextBlockIndex = entity.getCurrentIndexFloor(entity.getPosXFromBeat(atBeat - entity.deployBeat))
                val activeIndex = 0
                if (nextBlockIndex != activeIndex) continue // Not in range

                // Compare timing
                val perfectBeats = rod.deployBeat + 3f + nextBlockIndex / rod.xUnitsPerBeat
                val perfectSeconds = engine.tempos.beatsToSeconds(perfectBeats)

                val differenceSec = atSeconds - perfectSeconds
                val minSec = perfectSeconds - InputThresholds.MAX_OFFSET_SEC
                val maxSec = perfectSeconds + InputThresholds.MAX_OFFSET_SEC

                val accuracyPercent = (differenceSec / InputThresholds.MAX_OFFSET_SEC).coerceIn(-1f, 1f)
                val inputResult = InputResult(perfectBeats, type, accuracyPercent, differenceSec, activeIndex)
                if (DEBUG_LOG_INPUTS) {
                    Paintbox.LOGGER.debug("${rod.toString().substringAfter("polyrhythmmania.world.Entity")}: Input ${type}: ${if (differenceSec < 0) "EARLY" else if (differenceSec > 0) "LATE" else "PERFECT"} ${inputResult.inputScore} \t | perfectBeat=$perfectBeats, perfectSec=$perfectSeconds, diffSec=$differenceSec, minmaxSec=[$minSec, $maxSec], actualSec=$atSeconds")
                }

                val inputFeedbackIndex: Int = getInputFeedbackIndex(inputResult.inputScore, inputResult.accuracySec < 0f)
                if (inputFeedbackIndex in inputFeedbackFlashes.indices) {
                    inputFeedbackFlashes[inputFeedbackIndex] = atSeconds
                }

                // Bounce the rod regardless of miss; if aces only, bounce still has to apply for early/late
                rod.bounce(engine, inputResult)
                if (inputResult.inputScore == InputScore.MISS) { // Explicitly checking for MISS, since rod.bounce returns immediately if it is MISS
                    missed()
                }
            }
            world.dunkPiston.fullyExtend(engine, atBeat)
        } else if (worldMode.worldType == WorldType.Assemble && type == InputType.A) {
            val asmPlayerPiston = world.asmPlayerPiston
            var hit = false
            var hitDuration = 1.5f
            if (asmPlayerPiston.pistonState == EntityPiston.PistonState.RETRACTED) {
                for (entity in engine.world.entities) {
                    if (entity !is EntityRodAsm || !entity.acceptingInputs) continue
                    val rod: EntityRodAsm = entity

                    // Compare timing
                    val nextExpected = rod.expectedInputs.lastOrNull() ?: continue
                    val perfectBeats = nextExpected.inputBeat
                    val perfectSeconds = engine.tempos.beatsToSeconds(perfectBeats)

                    val differenceSec = atSeconds - perfectSeconds
                    val minSec = perfectSeconds - InputThresholds.MAX_OFFSET_SEC
                    val maxSec = perfectSeconds + InputThresholds.MAX_OFFSET_SEC

                    if (atSeconds !in minSec..maxSec) {
                        continue
                    }

                    val accuracyPercent = (differenceSec / InputThresholds.MAX_OFFSET_SEC).coerceIn(-1f, 1f)
                    val inputResult = InputResult(perfectBeats, type, accuracyPercent, differenceSec, 0)
                    if (DEBUG_LOG_INPUTS) {
                        Paintbox.LOGGER.debug("${rod.toString().substringAfter("polyrhythmmania.world.Entity")}: Input ${type}: ${if (differenceSec < 0) "EARLY" else if (differenceSec > 0) "LATE" else "PERFECT"} ${inputResult.inputScore} \t | perfectBeat=$perfectBeats, perfectSec=$perfectSeconds, diffSec=$differenceSec, minmaxSec=[$minSec, $maxSec], actualSec=$atSeconds")
                    }

                    if (!worldMode.endlessType.isEndless) {
                        (inputResults as MutableList).add(inputResult)
                    }
                    
                    val inputFeedbackIndex: Int = getInputFeedbackIndex(inputResult.inputScore, inputResult.accuracySec < 0f)
                    if (inputFeedbackIndex in inputFeedbackFlashes.indices) {
                        inputFeedbackFlashes[inputFeedbackIndex] = atSeconds
                    }

                    // Bounce the rod
                    if (!inputChallenge.isInputScoreMiss(inputResult.inputScore)) {
                        rod.addInputResult(engine, inputResult)
                        hit = true
                        hitDuration = 1f
                    }
                }

                val animation = asmPlayerPiston.animation
                if (animation is EntityPistonAsm.Animation.Neutral) {
                    asmPlayerPiston.fullyExtend(engine, atBeat, hitDuration, doWiggle = hit)
                    if (!hit) {
                        engine.soundInterface.playAudioNoOverlap(SidemodeAssets.assembleSfx.getValue("sfx_asm_middle_right"), SoundInterface.SFXType.PLAYER_INPUT) {
                            it.pitch = 1.5f
                        }
                    }
                } else if (animation is EntityPistonAsm.Animation.Charged && !hit) {
                    // Release charge
                    val piston = world.asmPlayerPiston
                    piston.animation = EntityPistonAsm.Animation.Fire(piston, atBeat)
                    piston.retract()
                    engine.soundInterface.playAudioNoOverlap(SidemodeAssets.assembleSfx.getValue("sfx_asm_shoot"), SoundInterface.SFXType.PLAYER_INPUT)

                    for (entity in engine.world.entities) {
                        if (entity !is EntityRodAsm || !entity.acceptingInputs) continue
                        val nextExpected = entity.expectedInputs.lastOrNull() ?: continue
                        if (nextExpected.isFire) {
                            entity.disableInputs = true
                        }
                    }
                }
            }
        }
    }
    
    fun getInputFeedbackIndex(score: InputScore, early: Boolean): Int {
        return when (score) {
            InputScore.ACE -> 2
            InputScore.GOOD -> if (early) 1 else 3
            InputScore.BARELY -> if (early) 0 else 4
            InputScore.MISS -> -1
        }
    }
    
    fun submitInputsFromRod(rod: EntityRodPR) {
        val inputTracker = rod.inputTracker
        
        val numExpected = inputTracker.expected.count { it is EntityRodPR.ExpectedInput.Expected }
        val validResults = inputTracker.results.filter { !inputChallenge.isInputScoreMiss(it.inputScore) }
        
        totalExpectedInputs += numExpected
        if (world.worldMode.endlessType.isEndless) {
            // Special case when in endless mode
            // Inputs don't count when lives = 0
            if (engine.areStatisticsEnabled && endlessScore.lives.get() > 0) {
                inputCountStats.total += numExpected
                inputCountStats.missed += (numExpected - validResults.size).coerceAtLeast(0)
                inputCountStats.aces += validResults.count { it.inputScore == InputScore.ACE }
                inputCountStats.goods += validResults.count { it.inputScore == InputScore.GOOD }
                inputCountStats.barelies += validResults.count { it.inputScore == InputScore.BARELY }
                inputCountStats.early += validResults.count { it.inputScore != InputScore.ACE && it.accuracyPercent < 0f }
                inputCountStats.late += validResults.count { it.inputScore != InputScore.ACE && it.accuracyPercent > 0f }
            }
        } else {
            (inputResults as MutableList).addAll(validResults)
            (expectedInputsPr as MutableList).addAll(inputTracker.expected.filterIsInstance<EntityRodPR.ExpectedInput.Expected>())
            if (engine.areStatisticsEnabled && !rod.exploded && numExpected > 0 && validResults.size == numExpected) {
                GlobalStats.rodsFerriedPolyrhythm.increment()
            }
        }
        
        if (!engine.autoInputs && noMiss && !rod.registeredMiss) {
            if ((rod.exploded && numExpected > 0) || (numExpected > validResults.size) || inputTracker.results.any { inputChallenge.isInputScoreMiss(it.inputScore) }) {
                missed()
            }
        }
    }
    
    fun missed() {
        noMiss = false
        if (perfectChallenge.goingForPerfect) {
            if (!perfectChallenge.failed) {
                perfectChallenge.failed = true
                perfectChallenge.hit = 1f
                engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_perfect_fail"), SoundInterface.SFXType.NORMAL) { player ->
                    player.gain = 0.45f
                }
                if (engine.areStatisticsEnabled) {
                    GlobalStats.perfectsLost.increment()
                }
            }
        }

        val worldMode = world.worldMode
        when (worldMode.worldType) {
            WorldType.Dunk -> {
                val oldLives = endlessScore.lives.get()
                triggerEndlessLifeLost()
                if (engine.areStatisticsEnabled && endlessScore.lives.get() < oldLives) {
                    GlobalStats.livesLostDunk.increment()
                }
            }
            WorldType.Assemble -> {
                triggerEndlessLifeLost()
            }
            else -> {}
        }
    }
    
    fun onRodPRExploded() {
        rodsExplodedPR++
        if (engine.areStatisticsEnabled) {
            GlobalStats.rodsExploded.increment()
            GlobalStats.rodsExplodedPolyrhythm.increment()
        }
    }
    
    fun triggerEndlessLifeLost() {
        val endlessScore = this.endlessScore
        val oldLives = endlessScore.lives.get()
        val newLives = (oldLives - 1).coerceIn(0, endlessScore.maxLives.get())
        endlessScore.lives.set(newLives)
        if (oldLives > 0 && newLives == 0) {
            onEndlessGameOver()
        }
    }
    
    fun onEndlessGameOver() {
        engine.playbackSpeed = 1f
        val currentSeconds = engine.seconds
        val currentBeat = engine.beat
        endlessScore.gameOverSeconds.set(currentSeconds)
        val score = endlessScore.score.get()
        val wasNewHighScore = score > endlessScore.highScore.getOrCompute()
        val afterBeat = engine.tempos.secondsToBeats(currentSeconds + 2f)
        
        engine.musicData.volumeMap.addMusicVolume(MusicVolume(currentBeat, (afterBeat - currentBeat) / 2f, 0))
        
        if (world.worldMode.endlessType == EndlessType.REGULAR_ENDLESS) {
            engine.addEvent(object : Event(engine) {
                override fun onStart(currentBeat: Float) {
                    super.onStart(currentBeat)

                    val activeTextBox: ActiveTextBox = if (wasNewHighScore && endlessScore.showNewHighScoreAtEnd) {
                        engine.soundInterface.playMenuSfx(AssetRegistry.get<LazySound>("sfx_fail_music_hi").sound)
                        engine.setActiveTextbox(TextBox(Localization.getValue("play.endless.gameOver.results.newHighScore", score), true, style = TextBoxStyle.BANNER))
                    } else {
                        engine.soundInterface.playMenuSfx(AssetRegistry.get<LazySound>("sfx_fail_music_nohi").sound)
                        engine.setActiveTextbox(TextBox(Localization.getValue("play.endless.gameOver.results", score), true, style = TextBoxStyle.BANNER))
                    }
                    activeTextBox.onComplete = { engine ->
                        engine.addEvent(EventEndState(engine, currentBeat))
                    }

                    if (wasNewHighScore) {
                        endlessScore.highScore.set(score)
                        PRManiaGame.instance.settings.persist()
                    }
                    endlessScore.gameOverUIShown.set(true)

                    if (engine.areStatisticsEnabled) {
                        val worldType = world.worldMode.worldType
                        when (worldType) {
                            is WorldType.Polyrhythm, WorldType.Dunk -> {
                                GlobalStats.inputsGottenTotal.increment(inputCountStats.total)
                                GlobalStats.inputsMissed.increment(inputCountStats.missed)
                                GlobalStats.inputsGottenAce.increment(inputCountStats.aces)
                                GlobalStats.inputsGottenGood.increment(inputCountStats.goods)
                                GlobalStats.inputsGottenBarely.increment(inputCountStats.barelies)
                                GlobalStats.inputsGottenEarly.increment(inputCountStats.early)
                                GlobalStats.inputsGottenLate.increment(inputCountStats.late)
                            }
                            WorldType.Assemble -> {
                                // NO-OP
                            }
                        }
                    }
                }
            }.apply {
                this.beat = afterBeat
                this.width = 0.5f
            })
        }
    }

    fun attemptSkillStar(beat: Float): Boolean {
        if (!skillStarGotten.get() && MathUtils.isEqual(skillStarBeat, beat, BEAT_EPSILON)) {
            skillStarGotten.set(true)
            onSkillStarHit()
            return true
        }
        return false
    }
    
    fun onSkillStarHit() {
        engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_skill_star"), SoundInterface.SFXType.PLAYER_INPUT) { player ->
            player.gain = 0.6f
        }
        if (engine.areStatisticsEnabled) {
            GlobalStats.skillStarsEarned.increment()
        }
    }
    
    fun addNonEndlessInputStats() {
        if (!engine.areStatisticsEnabled) return
        if (world.worldMode.endlessType.isEndless) return
        
        when (world.worldMode.worldType) {
            is WorldType.Polyrhythm, WorldType.Assemble -> {
                val results = this.inputResults
                val nInputs = max(results.size, max(totalExpectedInputs, minimumInputCount))
                val nonMissResults = results.filter { !inputChallenge.isInputScoreMiss(it.inputScore) }
                val missed = nInputs - nonMissResults.size
                val aces = results.count { it.inputScore == InputScore.ACE }
                val goods = results.count { it.inputScore == InputScore.GOOD }
                val barelies = results.count { it.inputScore == InputScore.BARELY }
                val earlies = nonMissResults.count { it.inputScore != InputScore.ACE && it.accuracyPercent < 0f }
                val lates = nonMissResults.count { it.inputScore != InputScore.ACE && it.accuracyPercent > 0f }

                GlobalStats.inputsGottenTotal.increment(nInputs)
                GlobalStats.inputsMissed.increment(missed)
                GlobalStats.inputsGottenAce.increment(aces)
                GlobalStats.inputsGottenGood.increment(goods)
                GlobalStats.inputsGottenBarely.increment(barelies)
                GlobalStats.inputsGottenEarly.increment(earlies)
                GlobalStats.inputsGottenLate.increment(lates)
            }
            WorldType.Dunk -> {
                // NO-OP
            }
        }
    }
    
}
