package polyrhythmmania.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import polyrhythmmania.world.render.Tileset
import polyrhythmmania.world.render.WorldRenderer


open class Entity(val world: World) {

    val position: Vector3 = Vector3()

    open fun render(renderer: WorldRenderer, batch: SpriteBatch, tileset: Tileset) {
    }
    
}

open class SimpleRenderedEntity(world: World) : Entity(world) {
    
    companion object {
        protected val tmpVec = Vector3()
    }

    /*protected */open fun getTextureRegionFromTileset(tileset: Tileset): TextureRegion? = null
    /*protected */open fun getWidth(): Float = 1f
    /*protected */open fun getHeight(): Float = 1f
    
    override fun render(renderer: WorldRenderer, batch: SpriteBatch, tileset: Tileset) {
        val texReg = getTextureRegionFromTileset(tileset) ?: return
        val convertedVec = renderer.convertWorldToScreen(tmpVec.set(this.position))
        batch.draw(texReg, convertedVec.x, convertedVec.y, getWidth(), getHeight())
    }

}