package polyrhythmmania.screen.mainmenu.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import paintbox.PaintboxGame
import paintbox.binding.BooleanVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.font.Markup
import paintbox.font.PaintboxFont
import paintbox.font.TextAlign
import paintbox.font.TextRun
import paintbox.transition.FadeToTransparent
import paintbox.transition.TransitionScreen
import paintbox.ui.*
import paintbox.ui.area.Insets
import paintbox.ui.control.*
import paintbox.ui.element.RectElement
import paintbox.ui.layout.VBox
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.SkinFactory
import paintbox.util.gdxutils.grey
import polyrhythmmania.Localization
import polyrhythmmania.PRManiaGame
import polyrhythmmania.Settings
import polyrhythmmania.engine.input.Challenges
import polyrhythmmania.engine.input.InputKeymapKeyboard
import polyrhythmmania.screen.mainmenu.MainMenuScreen
import polyrhythmmania.screen.play.regular.EnginePlayScreenBase
import polyrhythmmania.screen.play.regular.ResultsBehaviour
import polyrhythmmania.gamemodes.GameMode


/**
 * Represents a main menu-menu.
 *
 * There is a list of options with fixed functions. They will either go to another [MMMenu] or require a
 * total screen wipe.
 *
 * When transitioning to another [MMMenu], the tile flip effect will play for just the maximal area that changes.
 *
 */
abstract class MMMenu(val menuCol: MenuCollection) : Pane() {

    companion object {
        const val WIDTH_EXTRA_SMALL: Float = 0.3f
        const val WIDTH_SMALL: Float = 0.4f
        const val WIDTH_SMALL_MID: Float = 0.45f
        const val WIDTH_MID: Float = 0.5f
        const val WIDTH_MEDIUM: Float = 0.6f
        const val WIDTH_LARGE: Float = 0.75f
        const val WIDTH_FULL: Float = 1f
    }

    protected val root: SceneRoot get() = menuCol.sceneRoot
    protected val mainMenu: MainMenuScreen get() = menuCol.mainMenu
    
    val showLogo: BooleanVar = BooleanVar(true)

    /**
     * Removes the [MMMenu] from [menuCol] when popped [MenuCollection.popLastMenu]
     */
    val deleteWhenPopped: BooleanVar = BooleanVar(false)

    protected fun setSize(percentage: Float, adjust: Float = 0f) {
        bounds.width.bind {
            (this@MMMenu.parent.use()?.let { p -> p.contentZone.width.use() } ?: 0f) * percentage + adjust
        }
    }
    
    open fun getTileFlipAnimationBounds(): UIElement = this
    
    open fun onMenuEntered() {}
    open fun onMenuExited() {}
}


/**
 * Standard menu.
 */
open class StandardMenu(menuCol: MenuCollection) : MMMenu(menuCol) {

    companion object {
        val BUTTON_LONG_SKIN_ID: String = "MMMenu_StandardMenu_Button_Long"
        val BUTTON_SMALL_SKIN_ID: String = "MMMenu_StandardMenu_Button_Small"

        init {
            DefaultSkins.register(BUTTON_LONG_SKIN_ID, SkinFactory { element: Button ->
                StandardMenu.LongButtonSkin(element)
            })
            DefaultSkins.register(BUTTON_SMALL_SKIN_ID, SkinFactory { element: Button ->
                StandardMenu.SmallButtonSkin(element)
            })
        }
    }

    class LongButtonSkin(element: Button) : paintbox.ui.control.ButtonSkin(element) {
        companion object {
            val TEXT_COLOR: Color = Color().grey(90f / 255f, 1f)
            val DISABLED_TEXT: Color = Color().grey(150f / 255f)
            val HOVERED_TEXT: Color = Color().grey(1f)
            val PRESSED_TEXT: Color = Color(0.4f, 1f, 1f, 1f)
            val PRESSED_AND_HOVERED_TEXT: Color = Color(0.5f, 1f, 1f, 1f)

            val BG_COLOR: Color = Color(1f, 1f, 1f, 0f)
            val HOVERED_BG: Color = Color().grey(90f / 255f, 0.8f)
            val DISABLED_BG: Color = BG_COLOR.cpy()
            val PRESSED_BG: Color = HOVERED_BG.cpy()
            val PRESSED_AND_HOVERED_BG: Color = HOVERED_BG.cpy()
        }
        
        init {
            val grey = TEXT_COLOR
            this.defaultTextColor.set(grey)
            this.disabledTextColor.set(DISABLED_TEXT)
            this.hoveredTextColor.set(HOVERED_TEXT)
            this.pressedTextColor.set(PRESSED_TEXT)
            this.pressedAndHoveredTextColor.set(PRESSED_AND_HOVERED_TEXT)
            this.defaultBgColor.set(BG_COLOR)
            this.hoveredBgColor.set(HOVERED_BG)
            this.disabledBgColor.set(DISABLED_BG)
            this.pressedBgColor.set(PRESSED_BG)
            this.pressedAndHoveredBgColor.set(PRESSED_AND_HOVERED_BG)
            this.roundedRadius.set(0)
        }
    }

    class SmallButtonSkin(element: Button) : paintbox.ui.control.ButtonSkin(element) {
        companion object {
            val TEXT_COLOR: Color = Color().grey(0f / 255f, 1f)
            val DISABLED_TEXT: Color = Color().grey(100f / 255f, 1f)
            val HOVERED_TEXT: Color = Color().grey(30f / 255f, 1f)
            val PRESSED_TEXT: Color = Color(0.4f, 1f, 1f, 1f)
            val PRESSED_AND_HOVERED_TEXT: Color = Color(0.3f, 0.5f, 0.5f, 1f)

            val BG_COLOR: Color = Color().grey(145f / 255f, 1f)
            val HOVERED_BG: Color = Color().grey(155f / 255f, 1f)
            val DISABLED_BG: Color = Color().grey(125f / 255f, 1f)
            val PRESSED_BG: Color = HOVERED_BG.cpy()
            val PRESSED_AND_HOVERED_BG: Color = HOVERED_BG.cpy()
        }
        
        init {
            this.defaultTextColor.set(TEXT_COLOR)
            this.disabledTextColor.set(DISABLED_TEXT)
            this.hoveredTextColor.set(HOVERED_TEXT)
            this.pressedTextColor.set(PRESSED_TEXT)
            this.pressedAndHoveredTextColor.set(PRESSED_AND_HOVERED_TEXT)

            this.defaultBgColor.set(BG_COLOR)
            this.hoveredBgColor.set(HOVERED_BG)
            this.disabledBgColor.set(DISABLED_BG)
            this.pressedBgColor.set(PRESSED_BG)
            this.pressedAndHoveredBgColor.set(PRESSED_AND_HOVERED_BG)

            this.roundedRadius.set(1)
        }
    }

    val main: PRManiaGame get() = mainMenu.main
    val font: PaintboxFont = main.fontMainMenuMain
    protected val markup: Markup = Markup(mapOf(
            Markup.FONT_NAME_ITALIC to main.fontMainMenuItalic,
            Markup.FONT_NAME_BOLDITALIC to main.fontMainMenuItalic,
            "prmania_icons" to main.fontIcons,
            "rodin" to main.fontMainMenuRodin,
            "thin" to main.fontMainMenuThin
    ), TextRun(font, ""), Markup.FontStyles.ALL_USING_BOLD_ITALIC)
    protected val titleHeight: Float = 64f
    protected val grey: Color = Color().grey(0.8f, 1f)
    protected val blipSoundListener: (MouseEntered?) -> Unit = {
        menuCol.playBlipSound()
    }

    protected val titleText: Var<String> = Var("")
    protected val titleLabel: TextLabel
    protected val contentPane: RectElement = RectElement(grey).apply {
        this.bounds.height.set(titleHeight * 2) // Default.
        this.padding.set(Insets(16f))
    }

    init {
        titleLabel = TextLabel(binding = { titleText.use() }, font = main.fontMainMenuHeading).apply {
            this.bounds.height.set(titleHeight)
            this.bgPadding.set(Insets(16f, 16f, 16f, 32f))
            this.backgroundColor.set(grey)
            this.renderBackground.set(true)
            this.renderAlign.set(Align.bottomLeft)
        }
        val vbox: VBox = VBox().apply {
            this.spacing.set(0f)
            this.align.set(VBox.Align.TOP)
            Anchor.BottomLeft.configure(this)
        }
        vbox.temporarilyDisableLayouts {
            vbox += titleLabel
            vbox += contentPane
        }
        addChild(vbox)
        vbox.bounds.height.bind { contentPane.bounds.height.use() + titleLabel.bounds.height.use() }

        this.bounds.height.bind { vbox.bounds.height.use() }
        this.setSize(MMMenu.WIDTH_MEDIUM) // Default size
    }
    
    protected fun createTooltip(binding: Var.Context.() -> String): Tooltip {
        return Tooltip(binding).apply { 
            this.markup.set(this@StandardMenu.markup)
        }
    }

    protected fun createTooltip(varr: ReadOnlyVar<String>): Tooltip {
        return Tooltip(binding = { varr.use() }).apply {
            this.markup.set(this@StandardMenu.markup)
        }
    }

    protected fun createLongButton(binding: Var.Context.() -> String): Button {
        return Button(binding, font = font).apply {
            this.skinID.set(BUTTON_LONG_SKIN_ID)
            this.padding.set(Insets(4f, 4f, 12f, 12f))
            this.bounds.height.set(40f)
            this.textAlign.set(TextAlign.LEFT)
            this.renderAlign.set(Align.left)
            this.markup.set(this@StandardMenu.markup)
            this.setOnHoverStart(blipSoundListener)
        }
    }

    protected fun createLongButtonWithIcon(icon: TextureRegion?, binding: Var.Context.() -> String): Button {
        return createLongButton(binding).apply {
            if (icon != null) {
                val existingPadding = this.padding.getOrCompute()
                this.padding.set(existingPadding.copy(left = existingPadding.left * 2 + 32f))
                this.addChild(ImageIcon(icon, renderingMode = ImageRenderingMode.MAINTAIN_ASPECT_RATIO).apply {
                    this.bounds.x.set(-(32f + 4f * 2))
                    this.bounds.width.set(32f)
                    this.bounds.height.set(32f)
                })
            }
        }
    }

    protected fun createLongButtonWithNewIndicator(newIndicator: Settings.NewIndicator, icon: TextureRegion? = null,
                                                   binding: Var.Context.() -> String): Button {
        return createLongButtonWithIcon(icon) {
            (if (newIndicator.value.use())
                (Localization.getVar("common.newIndicator").use() + " ")
            else "") + binding.invoke(this)
        }
    }

    protected fun createSmallButton(binding: Var.Context.() -> String): Button = Button(binding, font = font).apply {
        this.skinID.set(BUTTON_SMALL_SKIN_ID)
        this.padding.set(Insets(2f))
        this.textAlign.set(TextAlign.CENTRE)
        this.renderAlign.set(Align.center)
        this.setScaleXY(0.75f)
//        this.setOnHoverStart(blipSoundListener)
    }
    
    protected fun createSettingsOption(labelText: Var.Context.() -> String, font: PaintboxFont = this.font,
                                       percentageContent: Float = 0.5f, twoRowsTall: Boolean = false): SettingsOptionPane {
        return SettingsOptionPane(labelText, font, percentageContent, twoRowsTall).apply {
            this.bounds.height.set(36f * (if (twoRowsTall) 2 else 1))
            this.addInputEventListener {
                if (it is MouseEntered) {
                    blipSoundListener(null)
                }
                false
            }
        }
    }
    
    protected fun createSliderPane(slider: Slider, percentageContent: Float = 0.5f,
                                   labelText: Var.Context.() -> String): SettingsOptionPane {
        return createSettingsOption(labelText, percentageContent = percentageContent).apply {
            this.content.addChild(slider)
            Anchor.CentreRight.configure(slider)
        }
    }
    
    protected fun createCheckboxOption(labelText: Var.Context.() -> String, font: PaintboxFont = this.font,
                                     percentageContent: Float = 1f): Pair<SettingsOptionPane, CheckBox> {
        val settingsOptionPane = createSettingsOption({""}, font, percentageContent)
        val checkBox = CheckBox(binding = labelText, font = font).apply { 
            Anchor.TopRight.configure(this)
            this.boxAlignment.set(CheckBox.BoxAlign.RIGHT)
            this.color.bind {
                if (apparentDisabledState.use()) {
                    LongButtonSkin.DISABLED_TEXT
                } else {
                    settingsOptionPane.textColorVar.use()
                }
            }
            this.textLabel.renderAlign.set(Align.left)
            this.textLabel.textAlign.set(TextAlign.LEFT)
            this.textLabel.margin.set(this.textLabel.margin.getOrCompute().copy(left = 0f))
        }
        settingsOptionPane.content += checkBox
        
        return settingsOptionPane to checkBox
    }

    protected fun createRadioButtonOption(labelText: Var.Context.() -> String, toggleGroup: ToggleGroup,
                                          font: PaintboxFont = this.font,
                                          percentageContent: Float = 1f): Pair<SettingsOptionPane, RadioButton> {
        val settingsOptionPane = createSettingsOption({ "" }, font, percentageContent)
        val radioButton = RadioButton(binding = labelText, font = font).apply { 
            Anchor.TopRight.configure(this)
            toggleGroup.addToggle(this)
            this.color.bind { settingsOptionPane.textColorVar.use() }
            this.textLabel.renderAlign.set(Align.left)
            this.textLabel.textAlign.set(TextAlign.LEFT)
            this.imageNode.margin.set(this.imageNode.margin.getOrCompute().copy(left = 0f))
        }
        settingsOptionPane.content += radioButton
        
        return settingsOptionPane to radioButton
    }

    protected fun <T> createCycleOption(items: List<T>, firstItem: T,
                                        labelText: Var.Context.() -> String, font: PaintboxFont = this.font,
                                        percentageContent: Float = 0.5f, twoRowsTall: Boolean = false,
                                        itemToString: (T) -> String = { it.toString() }): Pair<SettingsOptionPane, CycleControl<T>> {
        val settingsOptionPane = createSettingsOption(labelText, font, percentageContent, twoRowsTall = twoRowsTall)
        val cycle = CycleControl<T>(settingsOptionPane, items, firstItem, itemToString)
        settingsOptionPane.content += cycle

        return settingsOptionPane to cycle
    }

    protected fun <T> createComboboxOption(items: List<T>, firstItem: T,
                                           labelText: Var.Context.() -> String, font: PaintboxFont = this.font,
                                           percentageContent: Float = 0.5f, twoRowsTall: Boolean = false,
                                           itemToString: (T) -> String = { it.toString() }): Pair<SettingsOptionPane, ComboBox<T>> {
        val settingsOptionPane = createSettingsOption(labelText, font, percentageContent, twoRowsTall = twoRowsTall)
        val combobox = ComboBox<T>(items, firstItem, font)
        combobox.itemStringConverter.set(itemToString)
        settingsOptionPane.content += combobox

        return settingsOptionPane to combobox
    }
    
    protected fun createSidemodeLongButton(icon: TextureRegion?, name: String,
                                           tooltipVar: ReadOnlyVar<String> = Localization.getVar("${name}.tooltip"),
                                           challenges: Challenges = Challenges.NO_CHANGES,
                                           resultsBehaviour: ResultsBehaviour = ResultsBehaviour.NoResults,
                                           newIndicator: Settings.NewIndicator? = null,
                                           factory: (PRManiaGame, InputKeymapKeyboard) -> GameMode): UIElement {
        return (if (newIndicator == null)
                (createLongButtonWithIcon(icon) { Localization.getVar(name).use() })
        else (createLongButtonWithNewIndicator(newIndicator, icon) { Localization.getVar(name).use() })).apply {
            this.tooltipElement.set(createTooltip(tooltipVar))
            this.setOnAction {
                Gdx.input.isCursorCatched = true
                menuCol.playMenuSound("sfx_menu_enter_game")
                mainMenu.transitionAway {
                    val main = mainMenu.main
                    if (newIndicator != null) {
                        newIndicator.value.set(false)
                        main.settings.persist()
                    }
                    Gdx.app.postRunnable {
                        val sidemode: GameMode = factory.invoke(main, main.settings.inputKeymapKeyboard.getOrCompute().copy())
                        val playScreen = EnginePlayScreenBase(main, sidemode.playTimeType, gameMode = sidemode, container = sidemode.container,
                                inputCalibration = main.settings.inputCalibration.getOrCompute(),
                                challenges = challenges,
                                resultsBehaviour = resultsBehaviour)
                        main.screen = TransitionScreen(main, main.screen, playScreen, null, FadeToTransparent(0.25f, Color(0f, 0f, 0f, 1f))).apply {
                            this.onEntryEnd = {
                                sidemode.prepareFirstTime()
                                playScreen.resetAndUnpause()
                            }
                        }
                    }
                }
            }
        }
    }

    open class SettingsOptionPane(labelText: Var.Context.() -> String, val font: PaintboxFont,
                                  percentageContent: Float = 0.5f, val twoRowsTall: Boolean = false)
        : Pane(), HasPressedState by HasPressedState.DefaultImpl() {

        val textColorVar: ReadOnlyVar<Color> = Var.bind {
            if (isHoveredOver.use()) LongButtonSkin.HOVERED_TEXT else LongButtonSkin.TEXT_COLOR
        }
        val bgColorVar: ReadOnlyVar<Color> = Var.bind {
            if (isHoveredOver.use()) LongButtonSkin.HOVERED_BG else LongButtonSkin.BG_COLOR
        }

        val label: TextLabel
        val content: Pane

        init {
            val rect = RectElement(binding = { bgColorVar.use() }).apply {
//                this.padding.set(Insets(4f))
                this.padding.set(Insets(4f, 4f, 12f, 4f))
            }
            addChild(rect)
            
            label = TextLabel(labelText, font).apply {
                Anchor.TopLeft.configure(this)
                if (twoRowsTall) {
                    this.bindHeightToParent(multiplier = 0.5f) // Half height
                } else {
                    this.bindWidthToParent(adjust = 0f, multiplier = 1f - percentageContent) // Share space w/ content
                }
                this.textColor.bind { textColorVar.use() }
                this.renderAlign.set(Align.left)
                this.textAlign.set(TextAlign.RIGHT)
            }
            rect.addChild(label)
            
            content = Pane().apply {
                if (twoRowsTall) {
                    this.bindHeightToParent(multiplier = 0.5f)
                    Anchor.BottomRight.configure(this)
                } else {
                    Anchor.TopRight.configure(this)
                }
                this.bindWidthToParent(adjust = 0f, multiplier = percentageContent)
            }
            rect.addChild(content)
            
            @Suppress("LeakingThis")
            HasPressedState.DefaultImpl.addDefaultPressedStateInputListener(this)
        }
    }
    
    class CycleControl<T>(settingsOptionPane: SettingsOptionPane, val list: List<T>, firstItem: T,
                val itemToString: (T) -> String = { it.toString() })
        : Pane(), HasPressedState by HasPressedState.DefaultImpl() {
        
        val left: Button
        val right: Button
        val label: TextLabel
        
        val currentItem: Var<T> = Var(firstItem)
        
        init {
            left = Button("").apply { 
                Anchor.TopLeft.configure(this)
                this.bindWidthToSelfHeight()
                this.skinID.set(BUTTON_LONG_SKIN_ID)
                addChild(ImageNode(TextureRegion(PaintboxGame.paintboxSpritesheet.upArrow)).apply { 
                    this.rotation.set(90f)
                    this.padding.set(Insets(4f))
                    this.tint.bind { settingsOptionPane.textColorVar.use() }
                })
                this.setOnAction { 
                    val index = list.indexOf(currentItem.getOrCompute())
                    val nextIndex = (index - 1 + list.size) % list.size
                    currentItem.set(list[nextIndex])
                }
            }
            right = Button("").apply { 
                Anchor.TopRight.configure(this)
                this.bindWidthToSelfHeight()
                this.skinID.set(BUTTON_LONG_SKIN_ID)
                addChild(ImageNode(TextureRegion(PaintboxGame.paintboxSpritesheet.upArrow)).apply {
                    this.rotation.set(270f)
                    this.padding.set(Insets(4f))
                    this.tint.bind { settingsOptionPane.textColorVar.use() }
                })
                this.setOnAction {
                    val index = list.indexOf(currentItem.getOrCompute())
                    val nextIndex = (index + 1) % list.size
                    currentItem.set(list[nextIndex])
                }
            }
            label = TextLabel(binding = { itemToString(currentItem.use()) }, font = settingsOptionPane.font).apply {
                Anchor.Centre.configure(this)
                this.bindWidthToParent { -(bounds.height.use() * 2) }
                this.textColor.bind { settingsOptionPane.textColorVar.use() }
                this.textAlign.set(TextAlign.CENTRE)
                this.renderAlign.set(Align.center)
            }
            
            addChild(left)
            addChild(right)
            addChild(label)
            
            @Suppress("LeakingThis")
            HasPressedState.DefaultImpl.addDefaultPressedStateInputListener(this)
        }
    }
}