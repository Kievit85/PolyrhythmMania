package polyrhythmmania.engine.modifiers

import paintbox.binding.*
import paintbox.lazysound.LazySound
import paintbox.registry.AssetRegistry
import polyrhythmmania.Localization
import polyrhythmmania.PRManiaGame
import polyrhythmmania.engine.*
import polyrhythmmania.engine.input.EngineInputter
import polyrhythmmania.gamemodes.ChangeMusicVolMultiplierEvent
import polyrhythmmania.gamemodes.endlessmode.EntityRodPREndless
import polyrhythmmania.statistics.GlobalStats
import polyrhythmmania.world.EntityRodPR
import polyrhythmmania.world.EventEndState
import polyrhythmmania.world.WorldType

/**
 * Designed for Endless Mode/Daily Challenge.
 *
 * Not compatible with [LivesMode].
 */
class EndlessScore(parent: EngineModifiers) : ModifierModule(parent) {

    // Settings
    var showNewHighScoreAtEnd: Boolean = true // Hidden for something like Daily Challenge
    var hideHighScoreText: Boolean = false // The label that shows the Prev. High Score or daily challenge seed, etc
    var flashHudRedWhenLifeLost: Boolean = false // Only used in Endless Polyrhythm

    val maxLives: IntVar = IntVar(0)
    val startingLives: IntVar = IntVar { maxLives.use() }

    /**
     * Will be set when the [score] is higher than this [highScore].
     */
    var highScore: Var<Int> = GenericVar(0) // Intentionally a Var<Int> and not a specialized IntVar.


    // Data
    val score: IntVar = IntVar(0)
    val lives: IntVar = IntVar(startingLives.get())
    val gameOverSeconds: FloatVar = FloatVar(Float.MAX_VALUE)
    val gameOverUIShown: BooleanVar = BooleanVar(false)

    override fun resetState() {
        score.set(0)
        lives.set(startingLives.get())
        gameOverSeconds.set(Float.MAX_VALUE)
        gameOverUIShown.set(false)
    }

    override fun engineUpdate(beat: Float, seconds: Float, deltaSec: Float) {
    }

    override fun onRodPRExploded(rod: EntityRodPR, inputter: EngineInputter, countedAsMiss: Boolean) {
        if (rod !is EntityRodPREndless) return
        
        val lifeLostVar = rod.lifeLost
        if (countedAsMiss) {
            if (!lifeLostVar.lifeLost) {
                lifeLostVar.markLifeLost()

                val oldLives = this.lives.get()
                this.triggerEndlessLifeLost(inputter)
                if (engine.areStatisticsEnabled && this.lives.get() < oldLives) {
                    GlobalStats.livesLostEndless.increment()
                }
            }
        }
    }

    /**
     * Triggers a life to be lost.
     */
    fun triggerEndlessLifeLost(inputter: EngineInputter) {
        val oldLives = this.lives.get()
        val newLives = (oldLives - 1).coerceIn(0, this.maxLives.get())
        this.lives.set(newLives)

        if (oldLives > 0 && newLives == 0) {
            onEndlessGameOver(inputter)
        }
    }

    private fun onEndlessGameOver(inputter: EngineInputter) {
        val engine = inputter.engine
        val world = engine.world

        engine.playbackSpeed = 1f
        engine.resultFlag.set(ResultFlag.Fail.RanOutOfLives(this.maxLives.get() == 1))

        val currentSeconds = engine.seconds
        val currentBeat = engine.beat
        this.gameOverSeconds.set(currentSeconds)
        val score = this.score.get()
        val wasNewHighScore = score > this.highScore.getOrCompute()
        val afterBeat = engine.tempos.secondsToBeats(currentSeconds + 2f)

        engine.addEvent(ChangeMusicVolMultiplierEvent(engine, 1f, 0f, currentBeat, (afterBeat - currentBeat) / 2f))
        engine.addEvent(object : Event(engine) {
            override fun onStart(currentBeat: Float) {
                super.onStart(currentBeat)

                val activeTextBox: ActiveTextBox = if (wasNewHighScore && this@EndlessScore.showNewHighScoreAtEnd) {
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
                    this@EndlessScore.highScore.set(score)
                    PRManiaGame.instance.settings.persist()
                }
                this@EndlessScore.gameOverUIShown.set(true)

                if (engine.areStatisticsEnabled) {
                    when (world.worldMode.worldType) {
                        is WorldType.Polyrhythm, WorldType.Dunk -> {
                            inputter.inputCountStats.addToGlobalStats()
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
