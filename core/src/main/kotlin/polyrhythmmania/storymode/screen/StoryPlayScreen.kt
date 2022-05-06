package polyrhythmmania.storymode.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import paintbox.transition.TransitionScreen
import paintbox.transition.WipeTransitionHead
import paintbox.transition.WipeTransitionTail
import polyrhythmmania.PRManiaGame
import polyrhythmmania.container.Container
import polyrhythmmania.engine.InputCalibration
import polyrhythmmania.engine.input.Challenges
import polyrhythmmania.gamemodes.GameMode
import polyrhythmmania.gamemodes.endlessmode.EndlessPolyrhythm
import polyrhythmmania.screen.play.AbstractEnginePlayScreen
import polyrhythmmania.screen.play.pause.PauseMenuHandler
import polyrhythmmania.screen.play.pause.PauseOption
import polyrhythmmania.screen.play.pause.TengokuBgPauseMenuHandler
import polyrhythmmania.statistics.GlobalStats


class StoryPlayScreen(
        main: PRManiaGame,
        container: Container,
        challenges: Challenges, inputCalibration: InputCalibration,
        gameMode: GameMode?
) : AbstractEnginePlayScreen(main, null, container, challenges, inputCalibration, gameMode)  {

    override val pauseMenuHandler: PauseMenuHandler = TengokuBgPauseMenuHandler(this)

    private var disableCatchingCursorOnHide: Boolean = false
    
    init {
        val optionList = mutableListOf<PauseOption>()
        optionList += PauseOption("play.pause.resume", true) {
            unpauseGame(true)
        }
        optionList += PauseOption("play.pause.startOver", !(gameMode is EndlessPolyrhythm && gameMode.dailyChallenge != null)) {
            playMenuSound("sfx_menu_enter_game")

            val thisScreen: StoryPlayScreen = this
            val resetAction: () -> Unit = {
                resetAndUnpause()
                disableCatchingCursorOnHide = false
            }
            if (shouldCatchCursor()) {
                disableCatchingCursorOnHide = true
                Gdx.input.isCursorCatched = true
            }
            main.screen = TransitionScreen(main, thisScreen, thisScreen,
                    WipeTransitionHead(Color.BLACK.cpy(), 0.4f), WipeTransitionTail(Color.BLACK.cpy(), 0.4f)).apply {
                onEntryEnd = resetAction
            }
        }
        optionList += PauseOption("play.pause.quitToMainMenu", true) {
            quitToMainMenu()
            Gdx.app.postRunnable {
                playMenuSound("sfx_pause_exit")
            }
        }
        this.pauseOptions.set(optionList)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        
        if (!isPaused.get()) {
            // TODO increment play time for save file, call StorySavefile.updatePlayTime
            GlobalStats.updateTotalStoryModePlayTime()
        }
    }

    override fun uncatchCursorOnHide(): Boolean {
        return super.uncatchCursorOnHide() && !disableCatchingCursorOnHide
    }
}