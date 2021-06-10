package polyrhythmmania.editor.pane.track

import com.badlogic.gdx.graphics.Color
import paintbox.binding.Var
import paintbox.ui.Pane
import paintbox.ui.control.ScrollBar
import paintbox.util.gdxutils.grey
import polyrhythmmania.ui.PRManiaSkins


class ScrubTrack(allTracksPane: AllTracksPane) : LongTrackPane(allTracksPane, true) {

    val scrollBar: ScrollBar
    
    init {
        this.sidePanel.sidebarBgColor.bind { editorPane.palette.trackPaneTimeBg.use() }
        this.contentBgColor.bind { editorPane.palette.trackPaneTimeBg.use() }
        this.bounds.height.set(16f)
        this.showContentBorder.set(true)

        val contentPane = Pane()
        scrollBar = ScrollBar(ScrollBar.Orientation.HORIZONTAL).apply { 
            (this.skin.getOrCompute() as ScrollBar.ScrollBarSkin).also { skin ->
                skin.bgColor.set(Color().grey(0.1f, 1f))
                skin.incrementColor.set(Color(0.64f, 0.64f, 0.64f, 1f))
                skin.disabledColor.set(Color(0.31f, 0.31f, 0.31f, 1f))
                skin.thumbColor.set(Color(0.64f, 0.64f, 0.64f, 1f))
                skin.thumbHoveredColor.set(Color(0.70f, 0.70f, 0.70f, 1f))
                skin.thumbPressedColor.set(Color(0.50f, 0.64f, 0.64f, 1f))
            }
            val isDragging: Var<Boolean> = Var {
                thumbPressedState.use().pressed
            }
            this.userChangedValueListener = { newValue ->
                val min = this.minimum.getOrCompute()
                val percentage = ((newValue - min) / (this.maximum.getOrCompute() - min)).coerceIn(0f, 1f)
                val lastPos = editor.container.lastBlockPosition.getOrCompute()
                val pxPerBeat = editor.trackView.pxPerBeat.getOrCompute()
                val beatWidth = contentPane.bounds.width.getOrCompute() / pxPerBeat
                val newBeat = percentage * (if (lastPos < beatWidth) (beatWidth) else (lastPos))
                editor.trackView.beat.set(newBeat)
            }
            fun updateOwnValue(newValue: Float) {
                if (!isDragging.getOrCompute()) {
                    val lastPos = editor.container.lastBlockPosition.getOrCompute()
                    val pxPerBeat = editor.trackView.pxPerBeat.getOrCompute()
                    val beatWidth = contentPane.bounds.width.getOrCompute() / pxPerBeat
                    val maxBeat = (if (lastPos < beatWidth) (beatWidth) else (lastPos))
                    val min = this.minimum.getOrCompute()
                    setValue(min + (newValue / maxBeat) * (this.maximum.getOrCompute() - min))
                }
            }
            editor.trackView.beat.addListener {
                updateOwnValue(it.getOrCompute())
            }
            editor.container.lastBlockPosition.addListener {
                updateOwnValue(editor.trackView.beat.getOrCompute())
            }
        }
        contentPane += scrollBar
        this.contentSection += contentPane
    }


}