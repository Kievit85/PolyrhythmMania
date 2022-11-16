package polyrhythmmania.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.binding.IntVar
import paintbox.binding.Var
import paintbox.ui.Pane
import paintbox.util.ColorStack
import paintbox.util.gdxutils.drawUV
import kotlin.math.min


open class NinepatchPane : Pane() {
    
    companion object {
        private val DEFAULT_TEXTURE_TO_USE: () -> Texture? = { null }
    }
    
    val cornerSize: IntVar = IntVar(32)
    var textureToUse: () -> Texture? = DEFAULT_TEXTURE_TO_USE
    val color: Var<Color> = Var(Color(1f, 1f, 1f, 1f))
    
    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val renderBounds = this.paddingZone
        val x = renderBounds.x.get() + originX
        val y = originY - renderBounds.y.get()
        val w = renderBounds.width.get()
        val h = renderBounds.height.get()
        val lastPackedColor = batch.packedColor

        val opacity: Float = this.apparentOpacity.get()
        val tmpColor = ColorStack.getAndPush().set(color.getOrCompute())
        tmpColor.a *= opacity
        batch.color = tmpColor
        val cornerSize = this.cornerSize.get().toFloat()
        val cornerHeight = min(cornerSize, h / 2)
        val cornerWidth = min(cornerSize, w / 2)
        val innerHeight = h - cornerHeight * 2
        val innerWidth = w - cornerWidth * 2
        
        val texture: Texture? = textureToUse()
        if (texture != null) {
            val third = 1 / 3f

            // Corners
            batch.drawUV(texture, x, y - cornerHeight, cornerWidth, cornerHeight,
                    0f, 0f, third, third)
            batch.drawUV(texture, x + w - cornerWidth, y - cornerHeight, cornerWidth, cornerHeight,
                    2 * third, 0f, 1f, third)
            batch.drawUV(texture, x, y - h, cornerWidth, cornerHeight,
                    0f, 2 * third, third, 1f)
            batch.drawUV(texture, x + w - cornerWidth, y - h, cornerWidth, cornerHeight,
                    2 * third, 2 * third, 1f, 1f)

            // Sides
            batch.drawUV(texture, x + cornerWidth, y - cornerHeight, innerWidth, cornerHeight,
                    third, 0f, 1f - third, third)
            batch.drawUV(texture, x + cornerWidth, y - h, innerWidth, cornerHeight,
                    third, 1f - third, 1f - third, 1f)
            batch.drawUV(texture, x, y - h + cornerHeight, cornerWidth, innerHeight,
                    0f, third, third, 1f - third)
            batch.drawUV(texture, x + w - cornerWidth, y - h + cornerHeight, cornerWidth, innerHeight,
                    1f - third, third, 1f, 1f - third)

            // Centre
            batch.drawUV(texture, x + cornerWidth, y - h + cornerHeight, innerWidth, innerHeight,
                    third, third, 1f - third, 1f - third)
        }

        ColorStack.pop()
        batch.packedColor = lastPackedColor
    }
}
