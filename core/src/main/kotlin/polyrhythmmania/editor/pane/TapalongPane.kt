package polyrhythmmania.editor.pane

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import paintbox.binding.FloatVar
import paintbox.binding.Var
import paintbox.font.TextAlign
import paintbox.ui.Pane
import paintbox.ui.animation.Animation
import paintbox.ui.control.Button
import paintbox.ui.control.TextLabel
import paintbox.ui.layout.HBox
import polyrhythmmania.Localization
import polyrhythmmania.util.DecimalFormats


class TapalongPane(val toolbar: Toolbar) : Pane() {
    companion object {
        const val AUTO_RESET_SEC: Int = 10
        
        private const val FLASH_DURATION: Float = 0.25f
        private val TEXT_COLOR: Color = Color(1f, 1f, 1f, 1f)
        private val FLASH_TEXT_COLOR: Color = Color(0f, 1f, 1f, 1f)
    }

    val editorPane: EditorPane = toolbar.editorPane

    var sumDeltas: Double = 0.0
        private set
    val count: Var<Int> = Var(0)
    var averageBpm: FloatVar = FloatVar(0f)
    var timeSinceLastTap: Long = System.currentTimeMillis()
        private set
    var lastTapMs: Long = timeSinceLastTap
        private set
    private val flashAnimation: FloatVar = FloatVar(0f)

    init {
        val hbox = HBox().apply {
            this.spacing.set(4f)
        }
        this += hbox

        hbox.temporarilyDisableLayouts {
            val flashTextColor: Var<Color> = Var.sideEffecting(TEXT_COLOR.cpy()) { c ->
                val alpha = flashAnimation.useF()
                c.set(TEXT_COLOR).lerp(FLASH_TEXT_COLOR, alpha)
                c
            }
            hbox.addChild(TextLabel("♩=", font = editorPane.main.fontRodinFixed).apply {
                this.bounds.width.set(32f)
                this.renderAlign.set(Align.right)
                this.textAlign.set(TextAlign.RIGHT)
                this.textColor.set(Color.WHITE)
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("tapalong.bpmLabel")))
            })
            hbox.addChild(TextLabel(binding = {
                val count = count.use()
                if (count == 1)
                    Localization.getValue("tapalong.first")
                else
                    DecimalFormats.format("0.0", averageBpm.useF())
            }, font = editorPane.main.mainFontBold).apply {
                this.bounds.width.set(64f)
                this.renderAlign.set(Align.left)
                this.textAlign.set(TextAlign.LEFT)
                this.textColor.bind { flashTextColor.use() }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("tapalong.bpmLabel")))
            })
            hbox.addChild(TextLabel("n=", font = editorPane.main.fontRodinFixed).apply {
                this.bounds.width.set(32f)
                this.renderAlign.set(Align.right)
                this.textAlign.set(TextAlign.RIGHT)
                this.textColor.set(Color.WHITE)
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("tapalong.countLabel")))
            })
            hbox.addChild(TextLabel(binding = { "${count.use()}" }, font = editorPane.main.mainFontBold).apply {
                this.bounds.width.set(40f)
                this.renderAlign.set(Align.left)
                this.textAlign.set(TextAlign.LEFT)
                this.textColor.bind { flashTextColor.use() }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("tapalong.countLabel")))
            })
            hbox.addChild(Button(binding = { Localization.getVar("tapalong.reset").use() }, font = editorPane.main.mainFont).apply {
                this.bounds.width.set(96f)
                this.setOnAction {
                    reset()
                }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("tapalong.reset.tooltip", Var { listOf(AUTO_RESET_SEC) })))
            })
        }
    }

    fun tap() {
        if (count.getOrCompute() >= 1 && System.currentTimeMillis() == lastTapMs) return
        if (System.currentTimeMillis() - timeSinceLastTap > AUTO_RESET_SEC * 1000) {
            reset()
        }

        val last = timeSinceLastTap
        val current = System.currentTimeMillis()
        if (count.getOrCompute() >= 1) {
            val deltaMs = current - last
            sumDeltas += deltaMs / 1000.0
        }

        timeSinceLastTap = System.currentTimeMillis()
        lastTapMs = timeSinceLastTap
        count.set(count.getOrCompute() + 1)
        cancelFlashAnimation()
        sceneRoot.getOrCompute()?.animations?.enqueueAnimation(Animation(Interpolation.linear, FLASH_DURATION, 1f, 0f), flashAnimation)

        if (count.getOrCompute() >= 2) {
            val average = sumDeltas / (count.getOrCompute() - 1)
            averageBpm.set((60.0 / average).toFloat())
        }
    }

    fun reset() {
        sumDeltas = 0.0
        count.set(0)
        averageBpm.set(0f)
        cancelFlashAnimation()
    }
    
    private fun cancelFlashAnimation() {
        sceneRoot.getOrCompute()?.animations?.cancelAnimationFor(flashAnimation)
        flashAnimation.set(0f)
    }
}