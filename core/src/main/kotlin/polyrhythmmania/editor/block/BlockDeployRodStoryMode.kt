package polyrhythmmania.editor.block

import com.badlogic.gdx.graphics.Color
import com.eclipsesource.json.JsonObject
import paintbox.ui.area.Insets
import paintbox.ui.border.SolidBorder
import paintbox.ui.contextmenu.ContextMenu
import paintbox.ui.contextmenu.CustomMenuItem
import paintbox.ui.contextmenu.LabelMenuItem
import paintbox.ui.contextmenu.SeparatorMenuItem
import paintbox.ui.control.DecimalTextField
import paintbox.ui.element.RectElement
import paintbox.ui.layout.HBox
import paintbox.util.DecimalFormats
import polyrhythmmania.Localization
import polyrhythmmania.editor.Editor
import polyrhythmmania.engine.Engine
import polyrhythmmania.engine.Event
import polyrhythmmania.world.EventDeployRod
import polyrhythmmania.world.EventDeployRodStoryMode
import polyrhythmmania.world.entity.EntityRodDecor
import java.util.*


class BlockDeployRodStoryMode(engine: Engine, blockTypes: EnumSet<BlockType> = BlockDeployRod.BLOCK_TYPES)
    : BlockDeployRod(engine, blockTypes) {

    var xUnitsPerBeat: Float = 2f
    
    init {
        this.defaultText.set("Rod (SM)")
    }

    override fun compileIntoEvents(): List<Event> {
        val b = this.beat - 4
        return RowSetting.getRows(rowData.rowSetting.getOrCompute(), engine.world).map { row ->
            EventDeployRodStoryMode(engine, row, b) { rod ->
                rod.xUnitsPerBeat = this.xUnitsPerBeat.coerceAtLeast(0.25f)
            }
        }
    }
    
    override fun copy(): BlockDeployRodStoryMode {
        return BlockDeployRodStoryMode(engine).also {
            this.copyBaseInfoTo(it)
            it.rowData.rowSetting.set(this.rowData.rowSetting.getOrCompute())
            it.xUnitsPerBeat = this.xUnitsPerBeat
        }
    }

    override fun createContextMenu(editor: Editor): ContextMenu {
        return super.createContextMenu(editor).also { ctxmenu ->
            ctxmenu.addMenuItem(SeparatorMenuItem())
            ctxmenu.addMenuItem(LabelMenuItem.create("X-units per beat (def. 2)", editor.editorPane.palette.markup))
            ctxmenu.addMenuItem(CustomMenuItem(
                HBox().apply {
                    this.bounds.height.set(32f)
                    this.spacing.set(4f)
                    this += RectElement(Color(0f, 0f, 0f, 1f)).apply {
                        this.border.set(Insets(1f))
                        this.borderStyle.set(SolidBorder(Color.WHITE))
                        this.padding.set(Insets(2f))
                        this += DecimalTextField(startingValue = xUnitsPerBeat, decimalFormat = DecimalFormats["0.0##"],
                            font = editor.editorPane.palette.musicDialogFont).apply {
                            this.minimumValue.set(0.25f)
                            this.textColor.set(Color(1f, 1f, 1f, 1f))

                            this.value.addListener {
                                xUnitsPerBeat = it.getOrCompute()
                            }
                        }
                    }
                }
            ))
        }
    }

    override fun writeToJson(obj: JsonObject) {
        super.writeToJson(obj)
        obj.add("xUnitsPerBeat", this.xUnitsPerBeat)
    }

    override fun readFromJson(obj: JsonObject) {
        super.readFromJson(obj)
        xUnitsPerBeat = obj.getFloat("xUnitsPerBeat", EntityRodDecor.DEFAULT_X_UNITS_PER_BEAT)
    }
}