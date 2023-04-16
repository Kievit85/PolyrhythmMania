package polyrhythmmania.soundsystem.sample

import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.Gain
import net.beadsproject.beads.ugens.SamplePlayer
import net.beadsproject.beads.ugens.Static

open class SamplePlayerWrapper(private val samplePlayer: SamplePlayer)
    : PlayerLike(samplePlayer.context, samplePlayer.outs, samplePlayer.outs) {

    private val loopStartUGen: Static = Static(context, 0f)
    private val loopEndUGen: Static = Static(context, 0f)

    // Note: delegates could be useful here (e.g.: `by samplePlayer::position`) but there is
    // boxing/unboxing overhead compared to simply creating a getter and setter
    override var position: Double
        get() = samplePlayer.position
        set(value) {
            samplePlayer.position = value
        }
//    override var pitchUGen: UGen
//        get() = samplePlayer.pitchUGen
//        set(value) {
//            samplePlayer.setPitch(value)
//        }
    private val pitchUGen: Static = Static(context, 1f)
    override var pitch: Float
        get() = pitchUGen.value
        set(value) {
            pitchUGen.value = value
        }
    override var loopStartMs: Float = 0f
        set(value) {
            field = value
            loopStartUGen.x = value
        }
    override var loopEndMs: Float = 0f
        set(value) {
            field = value
            loopEndUGen.x = value
        }
    override var loopType: SamplePlayer.LoopType
        get() = samplePlayer.loopType
        set(value) {
            samplePlayer.loopType = value
        }
    override val durationMs: Double
        get() = samplePlayer.sample?.length ?: 0.0
    
    private val gainObj: Gain = Gain(this.context, this.outs, 1f)
    override var gain: Float
        get() = super.gain
        set(value) {
            super.gain = value
            gainObj.gain = value
        }
    override var killOnEnd: Boolean
        get() = samplePlayer.killOnEnd
        set(value) {
            samplePlayer.killOnEnd = value
        }

    init {
        this.addInput(gainObj)
        gainObj.addInput(samplePlayer)
        samplePlayer.setLoopStart(loopStartUGen)
        samplePlayer.setLoopEnd(loopEndUGen)
        
        samplePlayer.setPitch(pitchUGen)

        this.outputInitializationRegime = OutputInitializationRegime.RETAIN
        this.bufOut = this.bufIn
        
        samplePlayer.killListener = object : Bead() {
            override fun messageReceived(message: Bead?) {
                if (message === samplePlayer) {
                    this@SamplePlayerWrapper.kill()
                }
            }
        }
    }

    override fun start() {
        super.start()
        samplePlayer.start()
    }

    override fun pause(paused: Boolean) {
        super.pause(paused)
        samplePlayer.pause(paused)
    }

    override fun reset() {
        samplePlayer.reset()
    }

    override fun kill() {
        super.kill()
        samplePlayer.kill()
    }

    override fun calculateBuffer() {
        // NO-OP: outputInitializationRegime is RETAIN and bufOut <- bufIn
    }
}