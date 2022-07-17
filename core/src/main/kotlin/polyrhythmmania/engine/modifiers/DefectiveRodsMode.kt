package polyrhythmmania.engine.modifiers

import paintbox.binding.FloatVar
import paintbox.binding.IntVar
import polyrhythmmania.engine.Engine
import polyrhythmmania.engine.ResultFlag
import polyrhythmmania.engine.input.EngineInputter
import polyrhythmmania.engine.input.InputResult
import polyrhythmmania.world.EntityRodPR

/**
 * Tracks the number of defective rods that have escaped. If the threshold is met, the level has failed.
 * 
 * A life is lost when a miss occurs. There is a cooldown before more lives can be lost.
 * 
 * Not compatible with [EndlessScore] mode nor [LivesMode].
 */
class DefectiveRodsMode : ModifierModule() {
    
    companion object {
        const val DEFAULT_COOLDOWN: Float = 0.5f
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
    
    fun onDefectiveRodEscaped(engine: Engine, rod: EntityRodPR) {
        if (!this.enabled.get()) {
            return
        }
        if (currentCooldown.get() > 0 || lives.get() <= 0 || maxLives.get() <= 0) {
            return
        }

        val oldLives = this.lives.get()
        val newLives = (oldLives - 1).coerceIn(0, this.maxLives.get())
        this.lives.set(newLives)
        this.currentCooldown.set(cooldownBaseAmount.get())

        if (oldLives > 0 && newLives == 0) {
            onAllLivesLost(engine.inputter)
        }
    }
    
    fun onDefectiveRodExploded(engine: Engine, rod: EntityRodPR) {
        if (!this.enabled.get()) {
            return
        }
        
    }

    private fun onAllLivesLost(inputter: EngineInputter) {
        val engine = inputter.engine
        
        engine.resultFlag.set(ResultFlag.FAIL)
    }
}