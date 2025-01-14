package polyrhythmmania.storymode

import polyrhythmmania.PRManiaGame
import polyrhythmmania.discord.DefaultPresences
import polyrhythmmania.discord.DiscordRichPresence
import polyrhythmmania.screen.mainmenu.bg.BgType
import polyrhythmmania.statistics.GlobalStats
import polyrhythmmania.storymode.music.StoryMusicAssets
import polyrhythmmania.storymode.music.StoryMusicHandler
import polyrhythmmania.storymode.screen.StoryAssetsLoadingScreen


/**
 * Represents where story mode assets are loaded. You have to enter a [StorySession] by loading assets,
 * and leave it by unloading all of them.
 */
class StorySession {
    
    val musicHandler: StoryMusicHandler = StoryMusicHandler(this)
    
    var currentSavefile: StorySavefile? = null
        private set
    
    fun renderUpdate() {
        musicHandler.frameUpdate()
        
        GlobalStats.updateTotalStoryModePlayTime()
        currentSavefile?.updatePlayTime()
    }

    /**
     * Attempts to save the game. Will never throw an exception.
     */
    fun attemptSave() {
        val savefile = currentSavefile ?: return
        savefile.persist()
    }
    
    fun stopUsingSavefile() {
        currentSavefile = null
    }
    
    fun useSavefile(savefile: StorySavefile) {
        stopUsingSavefile()
        currentSavefile = savefile
    }
    
    fun createEntryLoadingScreen(main: PRManiaGame, doAfterLoad: () -> Unit): StoryAssetsLoadingScreen {
        DiscordRichPresence.updateActivity(DefaultPresences.playingStoryMode())
        main.mainMenuScreen.backgroundType = BgType.STORY_MODE
        StoryMusicAssets.initTitleStems()
        return StoryAssetsLoadingScreen(main, false, doAfterLoad)
    }

    fun createExitLoadingScreen(main: PRManiaGame, doAfterUnload: () -> Unit): StoryAssetsLoadingScreen {
        DiscordRichPresence.updateActivity(DefaultPresences.idle())
        attemptSave()
        return StoryAssetsLoadingScreen(main, true, doAfterUnload)
    }
}
