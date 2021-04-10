package polyrhythmmania.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.paintbox.registry.AssetRegistry
import polyrhythmmania.engine.Engine
import polyrhythmmania.soundsystem.BeadsSound
import polyrhythmmania.world.render.Tileset
import polyrhythmmania.world.render.WorldRenderer
import kotlin.math.floor


class EntityRowBlock(world: World, val baseY: Float, val row: Row, val rowIndex: Int)
    : SimpleRenderedEntity(world) {
    
    companion object {
        val FULL_EXTENSION_TIME_BEATS: Float = 0.05f
    }
    
    enum class Type(val renderHeight: Float) {
        PLATFORM(1f),
        PISTON_A(1.25f),
        PISTON_DPAD(1.25f)
    }
    
    enum class PistonState {
        FULLY_EXTENDED,
        PARTIALLY_EXTENDED,
        RETRACTED
    }
    
    var pistonState: PistonState = PistonState.RETRACTED
    var type: Type = Type.PLATFORM
        set(value) {
            field = value
            pistonState = PistonState.RETRACTED
        }
    var active: Boolean = true
    
    val collisionHeight: Float
        get() = if (pistonState == PistonState.RETRACTED) 1f else 1.15f
    
    private var shouldPartiallyExtend: Boolean = false
    private var fullyExtendedAtBeat: Float = 0f
    
    init {
        this.position.y = baseY - 1f
    }
    
    fun fullyExtend(engine: Engine, beat: Float) {
        pistonState = PistonState.FULLY_EXTENDED
        shouldPartiallyExtend = true
        fullyExtendedAtBeat = beat
        
        // FIXME tmp impl of extension sound
        when (type) {
            Type.PLATFORM -> { }
            Type.PISTON_A -> engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_input_a"))
            Type.PISTON_DPAD -> engine.soundInterface.playAudio(AssetRegistry.get<BeadsSound>("sfx_input_d"))
        }
        
        
        // Bounce any rods that are on this index
        // FIXME this is for testing purposes only
        world.entities.forEach { entity ->
            if (entity is EntityRod) {
                // The index that the rod is on
                val currentIndexFloat = entity.position.x - entity.row.startX
                val currentIndex = floor(currentIndexFloat).toInt()
                if (currentIndex == this.rowIndex && MathUtils.isEqual(entity.position.z, this.position.z)
                        && entity.position.y in (this.position.y + 1f)..(this.position.y + collisionHeight)) {
                    entity.bounce(currentIndex)
                }
            }
        }
    }
    
    fun spawn(percentage: Float) {
        val clamped = percentage.coerceIn(0f, 1f)
        active = clamped > 0f
        position.y = Interpolation.linear.apply(baseY - 1, baseY, clamped)
    }
    
    fun despawn(percentage: Float) {
        val clamped = percentage.coerceIn(0f, 1f)
        if (active) {
            active = clamped < 1f
            position.y = Interpolation.linear.apply(baseY, baseY - 1, clamped)
        }
    }
    
    fun retract() {
        pistonState = PistonState.RETRACTED
    }

    override fun engineUpdate(engine: Engine, beat: Float, seconds: Float) {
        super.engineUpdate(engine, beat, seconds)
        if (shouldPartiallyExtend) {
            if (beat - fullyExtendedAtBeat >= FULL_EXTENSION_TIME_BEATS) {
                shouldPartiallyExtend = false
                pistonState = PistonState.PARTIALLY_EXTENDED
            }
        }
    }

    override fun getRenderWidth(): Float = 1f
    override fun getRenderHeight(): Float = this.type.renderHeight
    override fun getTextureRegionFromTileset(tileset: Tileset): TextureRegion {
        return when (type) {
            Type.PLATFORM -> tileset.platform
            Type.PISTON_A -> when (pistonState) {
                PistonState.FULLY_EXTENDED -> tileset.padAExtended
                PistonState.PARTIALLY_EXTENDED -> tileset.padAPartial
                PistonState.RETRACTED -> tileset.padARetracted
            }
            Type.PISTON_DPAD -> when (pistonState) {
                PistonState.FULLY_EXTENDED -> tileset.padDExtended
                PistonState.PARTIALLY_EXTENDED -> tileset.padDPartial
                PistonState.RETRACTED -> tileset.padDRetracted
            }
        }
    }

    override fun render(renderer: WorldRenderer, batch: SpriteBatch, tileset: Tileset, engine: Engine) {
        if (active) {
            super.render(renderer, batch, tileset, engine)
        }
    }
}