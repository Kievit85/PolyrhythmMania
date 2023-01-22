package polyrhythmmania.storymode.screen.desktop

import com.badlogic.gdx.graphics.Color
import paintbox.PaintboxScreen
import paintbox.transition.FadeToOpaque
import paintbox.transition.FadeToTransparent
import paintbox.transition.TransitionScreen
import polyrhythmmania.PRManiaGame
import polyrhythmmania.engine.input.Challenges
import polyrhythmmania.gamemodes.GameMode
import polyrhythmmania.storymode.StorySession
import polyrhythmmania.storymode.contract.Contract
import polyrhythmmania.storymode.gamemode.AbstractStoryGameMode
import polyrhythmmania.storymode.inbox.InboxItem
import polyrhythmmania.storymode.inbox.InboxItemCompletion
import polyrhythmmania.storymode.inbox.InboxItemState
import polyrhythmmania.storymode.screen.ExitCallback
import polyrhythmmania.storymode.screen.StoryLoadingScreen
import polyrhythmmania.storymode.screen.StoryPlayScreen


interface DesktopController {

    enum class SFXType {
        ENTER_LEVEL, CLICK_INBOX_ITEM, INBOX_ITEM_UNLOCKED, PAUSE_ENTER, PAUSE_EXIT,
        CONTRACT_SIGNATURE,
    }
    
    
    fun createExitCallback(contract: Contract, inboxItem: InboxItem?, inboxItemState: InboxItemState?): ExitCallback
    
    fun playLevel(contract: Contract, inboxItem: InboxItem?, inboxItemState: InboxItemState?)
    
    fun playSFX(sfx: SFXType)

}

object NoOpDesktopController : DesktopController {

    override fun createExitCallback(contract: Contract, inboxItem: InboxItem?,
                                    inboxItemState: InboxItemState?): ExitCallback = ExitCallback { }

    override fun playLevel(contract: Contract, inboxItem: InboxItem?, inboxItemState: InboxItemState?) {
    }

    override fun playSFX(sfx: DesktopController.SFXType) {
    }
}

abstract class DesktopControllerWithPlayLevel
    : DesktopController {

    fun playLevel(
            contract: Contract, inboxItem: InboxItem?, inboxItemState: InboxItemState?,
            main: PRManiaGame, storySession: StorySession, exitToScreen: PaintboxScreen
    ) {
        storySession.musicHandler.fadeOut(0.25f)

        val gamemodeFactory = contract.gamemodeFactory
        val loadingScreen = StoryLoadingScreen<StoryPlayScreen>(main, { delta ->
            val gameMode: GameMode? = gamemodeFactory.load(delta, main)

            if (gameMode != null) {
                val exitCallback = createExitCallback(contract, inboxItem, inboxItemState)
                val playScreen = StoryPlayScreen(main, storySession, gameMode.container, Challenges.NO_CHANGES,
                        main.settings.inputCalibration.getOrCompute(), gameMode, contract,
                        inboxItemState?.completion != InboxItemCompletion.COMPLETED, inboxItemState?.failureCount ?: 0,
                        exitToScreen, exitCallback)

                gameMode.prepareFirstTime()
                if (gameMode is AbstractStoryGameMode) {
                    gameMode.prepareFirstTimeWithStoryPlayScreen(playScreen)
                }
                playScreen.resetAndUnpause(unpause = false)
                

                StoryLoadingScreen.LoadResult(playScreen)
            } else null
        }) { playScreen ->
            playScreen.unpauseGameNoSound()
            playScreen.initializeIntroCard()
            main.screen = TransitionScreen(main, main.screen, playScreen,
                    FadeToOpaque(0.125f, Color.BLACK), FadeToTransparent(0.125f, Color.BLACK))
        }.apply {
            this.minimumShowTime = 0f
            this.minWaitTimeBeforeLoadStart = 0.25f
            this.minWaitTimeAfterLoadFinish = 0f
        }

        main.screen = TransitionScreen(main, main.screen, loadingScreen,
                FadeToOpaque(0.125f, Color.BLACK), FadeToTransparent(0.125f, Color.BLACK))
    }
}
