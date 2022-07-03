package polyrhythmmania.engine.modifiers

import paintbox.binding.FloatVar
import paintbox.binding.IntVar
import polyrhythmmania.engine.ResultFlag
import polyrhythmmania.engine.input.EngineInputter
import polyrhythmmania.engine.input.InputResult

/**
 * Lives are separate from [EndlessScore] lives, and only flag the engine result to be failed.
 * 
 * A life is lost when a miss occurs. There is a cooldown before more lives can be lost.
 * 
 * Not compatible with [EndlessScore] mode.
 */
class LivesMode : ModifierModule() {
    
    companion object {
        const val DEFAULT_COOLDOWN: Float = 1.75f
    }

    // Settings
    val maxLives: IntVar = IntVar(3)
    val cooldownBaseAmount: FloatVar = FloatVar(DEFAULT_COOLDOWN)


    // Data
    val lives: IntVar = IntVar(maxLives.get())
    val currentCooldown: FloatVar = FloatVar(0f)

    override fun resetState() {
        lives.set(maxLives.get())
        currentCooldown.set(0f)
    }

    override fun engineUpdate(beat: Float, seconds: Float, deltaSec: Float) {
        if (currentCooldown.get() > 0f && deltaSec > 0f) {
            currentCooldown.set((currentCooldown.get() - deltaSec).coerceAtLeast(0f))
        }
    }
    
    fun loseALife(inputter: EngineInputter) {
        if (currentCooldown.get() > 0 || lives.get() <= 0 || maxLives.get() <= 0) {
            return
        }

        val oldLives = this.lives.get()
        val newLives = (oldLives - 1).coerceIn(0, this.maxLives.get())
        this.lives.set(newLives)
        this.currentCooldown.set(cooldownBaseAmount.get())

        if (oldLives > 0 && newLives == 0) {
            onAllLivesLost(inputter)
        }
    }

    private fun onAllLivesLost(inputter: EngineInputter) {
        val engine = inputter.engine
        
        engine.resultFlag.set(ResultFlag.FAIL)
    }


    // InputterListener overrides

    override fun onMissed(inputter: EngineInputter, firstMiss: Boolean) {
        loseALife(inputter)
    }

    override fun onInputResultHit(inputter: EngineInputter, result: InputResult, countsAsMiss: Boolean) {
        // NO-OP
    }
}