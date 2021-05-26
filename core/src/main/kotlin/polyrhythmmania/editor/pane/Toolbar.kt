package polyrhythmmania.editor.pane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.paintbox.binding.Var
import io.github.chrislo27.paintbox.packing.PackedSheet
import io.github.chrislo27.paintbox.registry.AssetRegistry
import io.github.chrislo27.paintbox.ui.Anchor
import io.github.chrislo27.paintbox.ui.ImageNode
import io.github.chrislo27.paintbox.ui.Pane
import io.github.chrislo27.paintbox.ui.Tooltip
import io.github.chrislo27.paintbox.ui.area.Insets
import io.github.chrislo27.paintbox.ui.border.SolidBorder
import io.github.chrislo27.paintbox.ui.control.Button
import io.github.chrislo27.paintbox.ui.control.ButtonSkin
import io.github.chrislo27.paintbox.ui.layout.HBox
import polyrhythmmania.Localization
import polyrhythmmania.editor.PlayState
import polyrhythmmania.editor.Tool
import polyrhythmmania.editor.pane.dialog.BasicDialog
import polyrhythmmania.editor.pane.dialog.MusicDialog


class Toolbar(val upperPane: UpperPane) : Pane() {

    val editorPane: EditorPane = upperPane.editorPane

    val previewSection: Pane
    val mainSection: Pane

    val pauseButton: Button
    val playButton: Button
    val stopButton: Button

    init {
        this.border.set(Insets(2f, 2f, 0f, 0f))
        this.borderStyle.set(SolidBorder().apply { this.color.bind { editorPane.palette.upperPaneBorder.use() } })
        this.padding.set(Insets(2f))


        // Preview section
        previewSection = Pane().apply {
            Anchor.TopLeft.configure(this)
            this.bounds.width.bind { upperPane.previewPane.contentZone.width.use() - 2f }
            this.border.set(Insets(0f, 0f, 0f, 2f))
            this.borderStyle.set(SolidBorder().apply { this.color.bind { editorPane.palette.previewPaneSeparator.use() } })
            this.padding.set(Insets(0f, 0f, 2f, 4f))
        }
        this += previewSection

        val playbackButtonPane = HBox().apply {
            Anchor.Centre.configure(this)
            this.spacing.set(4f)
            this.bounds.width.set(32f * 3 + this.spacing.getOrCompute() * 2)
            this.align.set(HBox.Align.CENTRE)
        }
        previewSection += playbackButtonPane
        pauseButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.pause")))
            this.disabled.bind { editorPane.editor.playState.use() != PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PAUSED)
            }
        }
        playButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.play")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PLAYING)
            }
        }
        stopButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.stop")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.STOPPED }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.STOPPED)
            }
        }
        playbackButtonPane.temporarilyDisableLayouts {
            playbackButtonPane += pauseButton
            playbackButtonPane += playButton
            playbackButtonPane += stopButton
        }


        // Main section
        mainSection = Pane().apply {
            Anchor.TopRight.configure(this)
            this.bounds.width.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - previewSection.bounds.width.use()
            }
            this.margin.set(Insets(0f, 0f, 2f, 4f))
        }
        this += mainSection
        
        val tools = Tool.VALUES
        val toolsPane = HBox().apply {
            Anchor.TopRight.configure(this)
            this.align.set(HBox.Align.RIGHT)
            this.spacing.set(4f)
            this.bounds.width.set((32f + this.spacing.getOrCompute()) * tools.size)
        }
        mainSection += toolsPane
        val toolActiveBorder: Insets = Insets(2f)
        toolsPane.temporarilyDisableLayouts {
            tools.forEachIndexed { index, thisTool ->
                toolsPane.addChild(Button("").apply {
                    this.bounds.width.set(32f)
                    this.skinID.set(EditorSkins.BUTTON)
                    this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_tool")[thisTool.textureKey])).apply {
                        this.tint.bind {
                            val selectedTool = editorPane.editor.tool.use()
                            if (selectedTool == thisTool) {
                                editorPane.palette.toolbarIconToolActiveTint.use()
                            } else {
                                editorPane.palette.toolbarIconToolNeutralTint.use()
                            }
                        }
                    }
                    this.borderStyle.set(SolidBorder().apply {
                        this.color.bind {
                            editorPane.palette.toolbarIconToolActiveBorderTint.use()
                        }
                    })
                    this.border.bind {
                        val selectedTool = editorPane.editor.tool.use()
                        if (selectedTool == thisTool) {
                            toolActiveBorder
                        } else {
                            Insets.ZERO
                        }
                    }
                    this.setOnAction {
                        editorPane.editor.changeTool(thisTool)
                    }
                    val tooltipStr = Localization.getVar("tool.tooltip", Var.bind {
                        listOf(Localization.getVar(thisTool.localizationKey).use(), "${index + 1}")
                    })
                    this.tooltipElement.set(editorPane.createDefaultTooltip(tooltipStr))
                })
            }
        }


        val musicControlsPane = HBox().apply {
            Anchor.TopLeft.configure(this)
            this.align.set(HBox.Align.LEFT)
            this.spacing.set(4f)
            this.bounds.width.set((32f + this.spacing.getOrCompute()) * tools.size)
        }
        mainSection += musicControlsPane
        musicControlsPane.temporarilyDisableLayouts {
            musicControlsPane.addChild(Button("").apply {
                this.bounds.width.set(32f)
                this.skinID.set(EditorSkins.BUTTON)
                this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_music"])).apply {
                    this.tint.bind {
                        editorPane.palette.toolbarIconToolNeutralTint.use()
                    }
                }
                this.setOnAction {
                    Gdx.app.postRunnable {
                        editorPane.editor.changePlayState(PlayState.STOPPED)
                        editorPane.openDialog(editorPane.musicDialog.prepareShow())
                    }
                }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.music")))
            })
        }
    }

}