package polyrhythmmania.editor.pane.dialog

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import paintbox.font.TextAlign
import paintbox.ui.Anchor
import paintbox.ui.control.Button
import paintbox.ui.control.ButtonSkin
import paintbox.ui.control.TextLabel
import paintbox.ui.layout.HBox
import polyrhythmmania.Localization
import polyrhythmmania.editor.EditorScreen
import polyrhythmmania.editor.pane.EditorPane


class NewDialog(editorPane: EditorPane) : EditorDialog(editorPane) {

    init {
        this.titleLabel.text.bind { Localization.getVar("editor.dialog.new.title").use() }

        bottomPane.addChild(Button(binding = { Localization.getVar("common.cancel").use() }, font = editorPane.palette.exitDialogFont).apply {
            Anchor.BottomRight.configure(this)
            this.bindWidthToSelfHeight(multiplier = 3f)
            this.applyDialogStyleBottom()
            this.setOnAction {
                attemptClose()
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.dialog.exit.cancel.tooltip")))
        })

        contentPane.addChild(TextLabel(binding = { Localization.getVar("editor.dialog.new.desc").use() }).apply {
            this.markup.set(editorPane.palette.markup)
            this.textColor.set(Color.WHITE.cpy())
            this.renderAlign.set(Align.center)
            this.textAlign.set(TextAlign.CENTRE)
        })

        val hbox = HBox().apply {
            Anchor.BottomCentre.configure(this)
            this.align.set(HBox.Align.CENTRE)
            this.spacing.set(16f)
            this.bounds.width.set(700f)
        }
        bottomPane.addChild(hbox)

        hbox.addChild(Button(binding = { Localization.getVar("editor.dialog.new.confirm").use() }, font = editorPane.palette.exitDialogFont).apply {
            this.bounds.width.set(600f)
            this.applyDialogStyleBottom()
            val skin = (this.skin.getOrCompute() as? ButtonSkin)
            if (skin != null) {
                skin.hoveredBgColor.set(Color(1f, 0.75f, 0.75f, 1f))
                skin.pressedBgColor.set(Color(1f, 0.55f, 0.55f, 1f))
                skin.pressedAndHoveredBgColor.set(Color(1f, 0.6f, 0.6f, 1f))
            }
            this.setOnAction {
                val currentScreen = main.screen
                val currentEditorScreen = currentScreen as? EditorScreen
                val newScreen = if (currentEditorScreen == null) EditorScreen(main) else EditorScreen(main, currentEditorScreen.editorFlags, currentEditorScreen.editorSpecialParams)
                if (currentScreen is EditorScreen) {
                    Gdx.app.postRunnable {
                        main.screen = null
                        currentScreen.dispose()
                        main.screen = newScreen
                    }
                }
            }
        })
    }

    override fun canCloseDialog(): Boolean {
        return true
    }
}