package polyrhythmmania.storymode.screen.title

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import paintbox.util.gdxutils.set
import paintbox.util.wave.WaveUtils
import polyrhythmmania.PRManiaGame
import polyrhythmmania.storymode.StoryAssets
import kotlin.math.absoluteValue


class TitleBackground {

    private val blue: Color = Color().set(181, 183, 240, a = 255)
    private val green: Color = Color().set(187, 249, 191, a = 255)
    private val red: Color = Color().set(240, 152, 168, a = 255)

    fun render(batch: SpriteBatch) {
        val checkerboardTex: Texture = StoryAssets["title_checkerboard"]
        
        val isReducedMotionOn = PRManiaGame.instance.settings.reducedMotion.getOrCompute()

        val time = if (isReducedMotionOn) 0L else System.currentTimeMillis()
        val checkerboardScroll = WaveUtils.getSawtoothWave(5f, timeMs = time)
        val colourTransition = ((WaveUtils.getSineWave(15f, time) - 0.5f) / 0.5f)
        val color = ColorStack.getAndPush().set(green).lerp(if (colourTransition < 0f) red else blue, colourTransition.absoluteValue)

        batch.color = color
        batch.fillRect(0f, 0f, 1280f, 720f)

        val squareSize = 50f / 24f
        val u = checkerboardScroll
        val v = checkerboardScroll
        color.mul(0.9f)
        color.a = 1f
        batch.color = color
        batch.draw(checkerboardTex, 0f, 0f, 1280f, 720f, u, v, u + (1280 / squareSize) / checkerboardTex.width, v - (720 / squareSize) / checkerboardTex.height)

        // Reset
        batch.flush()
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        batch.setColor(1f, 1f, 1f, 1f)
        
        ColorStack.pop()
    }
}
