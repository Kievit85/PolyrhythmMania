package polyrhythmmania.screen.mainmenu.menu

import com.badlogic.gdx.audio.Sound
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.registry.AssetRegistry
import paintbox.ui.Anchor
import paintbox.ui.Corner
import paintbox.ui.Pane
import paintbox.ui.SceneRoot
import paintbox.util.RectangleStack
import paintbox.util.Vector2Stack
import paintbox.util.gdxutils.maxX
import paintbox.util.gdxutils.maxY
import polyrhythmmania.PRManiaGame
import polyrhythmmania.Settings
import polyrhythmmania.library.menu.LibraryMenu
import polyrhythmmania.screen.mainmenu.MainMenuScreen
import polyrhythmmania.solitaire.SolitaireHelpMenu
import polyrhythmmania.solitaire.SolitaireMenu
import java.lang.Float.max
import java.lang.Float.min
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor


class MenuCollection(val mainMenu: MainMenuScreen, val sceneRoot: SceneRoot, val menuPane: Pane) {
    
    val main: PRManiaGame = mainMenu.main
    val settings: Settings = main.settings
    
    val menus: List<MMMenu> = mutableListOf()
    val activeMenu: ReadOnlyVar<MMMenu?> = Var(null)
    
    private val menuStack: Deque<MMMenu> = ArrayDeque()
    
    val uppermostMenu: UppermostMenu = UppermostMenu(this)
    val quitMenu: QuitMenu = QuitMenu(this)
    val dailyChallengeMenu: DailyChallengeMenu = DailyChallengeMenu(this) // Must be inited before playMenu
    val playMenu: PlayMenu = PlayMenu(this)
    val libraryMenu: LibraryMenu = LibraryMenu(this)
    val practiceMenu: PracticeMenu = PracticeMenu(this)
    val extrasMenu: ExtrasMenu = ExtrasMenu(this)
    val endlessMenu: EndlessModeMenu = EndlessModeMenu(this)
    val endlessHelpMenu: EndlessModeHelpMenu = EndlessModeHelpMenu(this)
    val settingsMenu: SettingsMenu = SettingsMenu(this)
    val audioSettingsMenu: AudioSettingsMenu = AudioSettingsMenu(this)
    val advancedAudioMenu: AdvAudioMenu = AdvAudioMenu(this)
    val videoSettingsMenu: VideoSettingsMenu = VideoSettingsMenu(this)
    val graphicsSettingsMenu: GraphicsSettingsMenu = GraphicsSettingsMenu(this)
    val inputSettingsMenu: InputSettingsMenu = InputSettingsMenu(this)
    val dataSettingsMenu: DataSettingsMenu = DataSettingsMenu(this)
    val languageMenu: LanguageMenu = LanguageMenu(this)
    val calibrationSettingsMenu: CalibrationSettingsMenu = CalibrationSettingsMenu(this)
    val updateNotesMenu: UpdateNotesMenu = UpdateNotesMenu(this)
    val achievementsStatsForkMenu: AchievementsStatsForkMenu = AchievementsStatsForkMenu(this)
    val statisticsMenu: StatisticsMenu = StatisticsMenu(this)
    val achievementsMenu: AchievementsMenu = AchievementsMenu(this)
    val solitaireMenu: SolitaireMenu = SolitaireMenu(this)
    val solitaireHelpMenu: SolitaireHelpMenu = SolitaireHelpMenu(this)
    
    init {
        addStockMenus()
        
        changeActiveMenu(uppermostMenu, false, instant = true)
        menuStack.push(uppermostMenu)
        
        playMenuSound("sfx_silence", volume = 0f) // Immediately load sound playing code. Initial lag causes a spike which sounds very loud and piercing on slower hardware
    }
    
    private fun addStockMenus() {
        addMenu(uppermostMenu)
        addMenu(quitMenu)
        addMenu(dailyChallengeMenu)
        addMenu(playMenu)
        addMenu(libraryMenu)
        addMenu(practiceMenu)
        addMenu(extrasMenu)
        addMenu(endlessMenu)
        addMenu(endlessHelpMenu)
        addMenu(settingsMenu)
        addMenu(videoSettingsMenu)
        addMenu(graphicsSettingsMenu)
        addMenu(audioSettingsMenu)
        addMenu(advancedAudioMenu)
        addMenu(inputSettingsMenu)
        addMenu(dataSettingsMenu)
        addMenu(languageMenu)
        addMenu(calibrationSettingsMenu)
        addMenu(updateNotesMenu)
        addMenu(achievementsStatsForkMenu)
        addMenu(statisticsMenu)
        addMenu(achievementsMenu)
        addMenu(solitaireMenu)
        addMenu(solitaireHelpMenu)
    }
    
    fun addMenu(menu: MMMenu) {
        menus as MutableList
        menus.add(menu)
        
        menu.visible.set(false)
        menuPane.addChild(menu)
        Anchor.BottomLeft.configure(menu)
    }
    
    fun removeMenu(menu: MMMenu) {
        menus as MutableList
        menus.remove(menu)
        menuPane.removeChild(menu)
    }
    
    fun resetMenuStack() {
        val existing = menuStack.toList()
        existing.forEach {
            if (it.deleteWhenPopped.get()) {
                this.removeMenu(it)
            }
        }
        menuStack.clear()
        menuStack.push(uppermostMenu)
    }
    
    fun changeActiveMenu(menu: MMMenu, backOut: Boolean, instant: Boolean = false, playSound: Boolean = !instant) {
        if (playSound) {
            main.playMenuSfx(AssetRegistry.get<Sound>("sfx_menu_${if (backOut) "deselect" else "select"}"))
        }
        if (!instant) {
            val changedBounds = RectangleStack.getAndPush().apply {
                val currentBoundsElement = menu.getTileFlipAnimationBounds()
                val currentBounds = currentBoundsElement.bounds
                val relToRoot = currentBoundsElement.getPosRelativeToRoot(Vector2Stack.getAndPush())
                this.set(relToRoot.x, relToRoot.y,
                        currentBounds.width.get(), currentBounds.height.get())
                Vector2Stack.pop()
            }
            
            val currentActive = activeMenu.getOrCompute()
            if (currentActive != null) {
                val secondBounds = RectangleStack.getAndPush()
                val currentActiveElement = currentActive.getTileFlipAnimationBounds()
                val curActiveBounds = currentActiveElement.bounds
                val relToRoot = currentActiveElement.getPosRelativeToRoot(Vector2Stack.getAndPush())
                secondBounds.set(relToRoot.x, relToRoot.y,
                        curActiveBounds.width.get(), curActiveBounds.height.get())
                Vector2Stack.pop()
                
                // Merge the two rectangles to be maximal.
                changedBounds.x = min(changedBounds.x, secondBounds.x)
                changedBounds.y = min(changedBounds.y, secondBounds.y)
                changedBounds.width = max(changedBounds.maxX, secondBounds.maxX) - changedBounds.x
                changedBounds.height = max(changedBounds.maxY, secondBounds.maxY) - changedBounds.y
                
                RectangleStack.pop()
            }
            
            val rootWidth = 1280f
            val rootHeight = 720f
            val tileX = floor(changedBounds.x / rootWidth * mainMenu.tilesWidth).toInt()
            val tileY = floor(changedBounds.y / rootHeight * mainMenu.tilesHeight).toInt()
            val tileW = (ceil(changedBounds.maxX / rootWidth * mainMenu.tilesWidth).toInt() - tileX).coerceAtLeast(1)
            val tileH = (ceil(changedBounds.maxY / rootHeight * mainMenu.tilesHeight).toInt() - tileY).coerceAtLeast(1)
            mainMenu.requestTileFlip(MainMenuScreen.TileFlip(tileX, tileY, tileW, tileH,
                    if (backOut) Corner.TOP_RIGHT else Corner.TOP_LEFT))
            
            RectangleStack.pop()
        }
        menus.forEach { 
            if (it !== menu && it.visible.get()) {
                it.visible.set(false)
                it.onMenuExited()
            }
        }
        menu.visible.set(true)
        (activeMenu as Var).set(menu)
        menu.onMenuEntered()
    }
    
    fun pushNextMenu(menu: MMMenu, instant: Boolean = false, playSound: Boolean = true) {
        changeActiveMenu(menu, false, instant, playSound)
        menuStack.push(menu)
    }

    fun popLastMenu(instant: Boolean = false, playSound: Boolean = !instant, backOut: Boolean = true): MMMenu {
        if (menuStack.size <= 1) return menuStack.peek()
        val popped = menuStack.pop()
        val menu = menuStack.peek()
        changeActiveMenu(menu, backOut, instant, playSound)
        if (popped.deleteWhenPopped.get()) {
            this.removeMenu(popped)
        }
        return popped
    }

    fun playBlipSound(volume: Float = 1f, pitch: Float = 1f, pan: Float = 0f) {
        val sound = AssetRegistry.get<Sound>("sfx_menu_blip")
        sound.stop()
        main.playMenuSfx(sound, volume, pitch, pan)
    }
    
    fun playMenuSound(sound: Sound, volume: Float = 1f, pitch: Float = 1f, pan: Float = 0f): Pair<Sound, Long> {
        return sound to main.playMenuSfx(sound, volume, pitch, pan)
    }
    
    fun playMenuSound(id: String, volume: Float = 1f, pitch: Float = 1f, pan: Float = 0f): Pair<Sound, Long> {
        return playMenuSound(AssetRegistry.get<Sound>(id), volume, pitch, pan)
    }
    
}