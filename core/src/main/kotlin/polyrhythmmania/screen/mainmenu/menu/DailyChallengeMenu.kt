package polyrhythmmania.screen.mainmenu.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import paintbox.binding.*
import paintbox.registry.AssetRegistry
import paintbox.transition.FadeIn
import paintbox.transition.TransitionScreen
import paintbox.ui.Anchor
import paintbox.ui.ImageNode
import paintbox.ui.Pane
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.ui.control.*
import paintbox.ui.element.RectElement
import paintbox.ui.layout.HBox
import paintbox.ui.layout.VBox
import paintbox.util.gdxutils.grey
import polyrhythmmania.Localization
import polyrhythmmania.achievements.Achievements
import polyrhythmmania.discord.DefaultPresences
import polyrhythmmania.discord.DiscordRichPresence
import polyrhythmmania.engine.input.Challenges
import polyrhythmmania.screen.mainmenu.bg.BgType
import polyrhythmmania.screen.play.EnginePlayScreenBase
import polyrhythmmania.screen.play.ResultsBehaviour
import polyrhythmmania.gamemodes.EndlessModeScore
import polyrhythmmania.gamemodes.endlessmode.*
import polyrhythmmania.statistics.GlobalStats
import polyrhythmmania.statistics.PlayTimeType
import polyrhythmmania.ui.PRManiaSkins
import polyrhythmmania.util.flags.CountryFlags
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread


class DailyChallengeMenu(menuCol: MenuCollection) : StandardMenu(menuCol) {
    
    private var numTimesPlayedThisSession: Int = 0

    private var epochSeconds: Long = System.currentTimeMillis() / 1000
    val dailyChallengeDate: Var<LocalDate> = Var(EndlessPolyrhythm.getCurrentDailyChallengeDate())
    private val resetsAtLocalTimeText: ReadOnlyVar<String> = Localization.getVar("mainMenu.dailyChallenge.resetHint", Var { 
        val localTime = ZonedDateTime.of(LocalDateTime.of(dailyChallengeDate.use(), LocalTime.MIDNIGHT), ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalTime()
        listOf("${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}")
    })
    
    private val isFetching: BooleanVar = BooleanVar(false)
    private val disableRefresh: BooleanVar = BooleanVar(false)
    private var disableRefreshUntil: Long = 0L
    private val leaderboardVar: Var<DailyLeaderboard?> = Var(null)
    private val scrollPaneContent: Var<Pane> = Var(Pane())

    private var showRefreshPrompt: Boolean = true
    
    init {
        this.setSize(MMMenu.WIDTH_MID)
        this.titleText.bind { Localization.getVar("mainMenu.dailyChallenge.title").use() }
        this.contentPane.bounds.height.set(520f)
        this.showLogo.set(false)

        val hbox = HBox().apply {
            Anchor.BottomLeft.configure(this)
            this.spacing.set(8f)
            this.padding.set(Insets(4f, 0f, 2f, 2f))
            this.bounds.height.set(40f)
        }

        contentPane.addChild(hbox)


        val vbox = VBox().apply {
            Anchor.TopLeft.configure(this)
            this.spacing.set(0f)
            this.bindHeightToParent(-40f)
        }

        val needsRefreshString = Localization.getVar("mainMenu.dailyChallenge.leaderboard.needsRefresh", Var {
            listOf(resetsAtLocalTimeText.use())
        })
        
        vbox.temporarilyDisableLayouts {
            val dailyChallengeTitle: ReadOnlyVar<String> = Localization.getVar("mainMenu.dailyChallenge.play", Var {
                listOf(dailyChallengeDate.use().format(DateTimeFormatter.ISO_DATE), resetsAtLocalTimeText.use())
            })
            vbox += createLongButton { dailyChallengeTitle.use() }.apply {
                this.setOnAction {
                    val date = dailyChallengeDate.getOrCompute()
                    val onSuccess: (UUID?) -> Unit = { nonce ->
                        Gdx.input.isCursorCatched = true
                        menuCol.playMenuSound("sfx_menu_enter_game")
                        mainMenu.transitionAway {
                            val main = mainMenu.main

                            // Daily challenge streak calculation
                            val lastScore = main.settings.endlessDailyChallenge.getOrCompute()
                            if (lastScore.date == date.plusDays(-1L)) {
                                val newStreak = main.settings.dailyChallengeStreak.incrementAndGet()
                                if (newStreak >= 7) {
                                    Achievements.awardAchievement(Achievements.dailyWeekStreak)
                                }
                            } else {
                                main.settings.dailyChallengeStreak.set(1)
                            }

                            numTimesPlayedThisSession++
                            if (numTimesPlayedThisSession >= 2) {
                                Achievements.awardAchievement(Achievements.dailyTwiceInOneSession)
                            }

                            Gdx.app.postRunnable {
                                val scoreVar = IntVar(0)
                                scoreVar.addListener {
                                    main.settings.endlessDailyChallenge.set(DailyChallengeScore(date, it.getOrCompute()))
                                }
                                val sidemode: EndlessPolyrhythm = EndlessPolyrhythm(main, PlayTimeType.DAILY_CHALLENGE,
                                        EndlessModeScore(scoreVar, showNewHighScoreAtEnd = false),
                                        EndlessPolyrhythm.getSeedFromLocalDate(date), date, disableLifeRegen = false)
                                val playScreen = EnginePlayScreenBase(main, sidemode.playTimeType,
                                        sidemode.container, gameMode = sidemode,
                                        challenges = Challenges.NO_CHANGES,
                                        inputCalibration = main.settings.inputCalibration.getOrCompute(),
                                        resultsBehaviour = ResultsBehaviour.NoResults)
                                main.settings.endlessDailyChallenge.set(DailyChallengeScore(date, 0))
                                main.settings.persist()
                                main.screen = TransitionScreen(main, main.screen, playScreen, null, FadeIn(0.25f, Color(0f, 0f, 0f, 1f))).apply {
                                    this.onEntryEnd = {
                                        sidemode.prepare()
                                        playScreen.resetAndUnpause()
                                        DiscordRichPresence.updateActivity(DefaultPresences.playingDailyChallenge(date))
                                        mainMenu.backgroundType = BgType.ENDLESS
                                        GlobalStats.timesPlayedDailyChallenge.increment()
                                    }
                                }
                                
                                sidemode.dailyChallengeUUIDNonce.set(nonce)
                            }
                        }
                    }
                    
                    val waitingForNonceMenu = WaitingForNonceMenu(menuCol, date, onSuccess)
                    menuCol.addMenu(waitingForNonceMenu)
                    menuCol.pushNextMenu(waitingForNonceMenu)
                }
                
                this.tooltipElement.set(createTooltip(binding = {
                    val (date, hiScore) = main.settings.endlessDailyChallenge.use()
                    if (date == dailyChallengeDate.use()) {
                        Localization.getVar("mainMenu.dailyChallenge.play.tooltip.expired", Var {
                            listOf(hiScore, resetsAtLocalTimeText.use())
                        })
                    } else {
                        Localization.getVar("mainMenu.dailyChallenge.play.tooltip.ready", Var {
                            listOf(resetsAtLocalTimeText.use())
                        })
                    }.use()
                }))
                this.disabled.bind {
                    val (date, _) = main.settings.endlessDailyChallenge.use()
                    date == dailyChallengeDate.use()
                }
            }
            
            
            fun separator(): UIElement {
                return RectElement(Color().grey(90f / 255f, 0.8f)).apply {
                    this.bounds.height.set(10f)
                    this.margin.set(Insets(4f, 4f, 0f, 0f))
                }
            }
            
            vbox += separator()
            
            val internalPane = Pane().apply {
                Anchor.TopLeft.configure(this)
                this.bounds.height.set(390f)
            }
            
            val paneFetching = Pane().apply {
                this.bounds.height.set(200f)
                this += TextLabel(binding = { Localization.getVar("mainMenu.dailyChallenge.leaderboard.fetching").use() }).apply {
                    this.renderAlign.set(Align.center)
                    this.doLineWrapping.set(true)
                    this.markup.set(this@DailyChallengeMenu.markup)
                }
            }
            val paneNoData = Pane().apply {
                this.bounds.height.set(200f)
                this += TextLabel(binding = { Localization.getVar("mainMenu.dailyChallenge.leaderboard.noData").use() }).apply {
                    this.renderAlign.set(Align.center)
                    this.doLineWrapping.set(true)
                    this.markup.set(this@DailyChallengeMenu.markup)
                }
            }
            val paneNeedsRefresh = Pane().apply {
                this.bounds.height.set(200f)
                this += TextLabel(binding = { needsRefreshString.use() }).apply {
                    this.renderAlign.set(Align.center)
                    this.doLineWrapping.set(true)
                    this.markup.set(this@DailyChallengeMenu.markup)
                }
            }
            
            scrollPaneContent.addListener {
                internalPane.children.forEach { c -> internalPane.removeChild(c) }
                internalPane.addChild(it.getOrCompute())
            }
            scrollPaneContent.bind {
                if (isFetching.use()) {
                    paneFetching
                } else {
                    if (showRefreshPrompt) {
                        paneNeedsRefresh
                    } else {
                        val list = leaderboardVar.use()
                        if (list == null || list.isEmpty() || list.values.sumOf { it.size } == 0) {
                            paneNoData
                        } else {
                            createTable(list)
                        }
                    }
                }
            }
            
            vbox += internalPane
        }

        vbox.sizeHeightToChildren(100f)
        contentPane.addChild(vbox)
        
        hbox.temporarilyDisableLayouts {
            hbox += createSmallButton(binding = { Localization.getVar("common.back").use() }).apply {
                this.bounds.width.set(100f)
                this.setOnAction {
                    menuCol.popLastMenu()
                }
            }
            hbox += createSmallButton(binding = { Localization.getVar("mainMenu.dailyChallenge.leaderboard.refreshLeaderboard").use() }).apply {
                this.bounds.width.set(250f)
                this.setOnAction {
                    getLeaderboard()
                }
                this.disabled.bind { 
                    isFetching.use() || disableRefresh.use()
                }
                
                val tt = createTooltip(needsRefreshString)
                this.tooltipElement.bind { 
                    if (disabled.use() || showRefreshPrompt) null else tt
                }
            }
        }
    }
    
    fun prepareShow(): DailyChallengeMenu {
        return this
    }
    
    private fun getLeaderboard() {
        showRefreshPrompt = false
        DailyChallengeUtils.getLeaderboardPastWeek(leaderboardVar, isFetching)
        disableRefresh.set(true)
        disableRefreshUntil = System.currentTimeMillis() + 10_000L
    }
    
    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        super.renderSelf(originX, originY, batch)

        val newEpochSeconds = System.currentTimeMillis() / 1000
        if (newEpochSeconds != epochSeconds) {
            epochSeconds = newEpochSeconds
            val newLocalDate = EndlessPolyrhythm.getCurrentDailyChallengeDate()
            if (newLocalDate != dailyChallengeDate.getOrCompute()) {
                dailyChallengeDate.set(newLocalDate)
            }
        }
        scrollPaneContent.getOrCompute() // Forces refresh.
        if (disableRefresh.get() && System.currentTimeMillis() > disableRefreshUntil) {
            disableRefresh.set(false)
        }
    }
    
    private fun createTable(leaderboard: DailyLeaderboard): Pane {
        return Pane().apply {
            val headerHBox = HBox().apply {
                this.bounds.height.set(32f)
            }
            val scrollPane = ScrollPane().apply {
                Anchor.TopLeft.configure(this, offsetY = 32f)
                this.bindHeightToParent(-32f)

                (this.skin.getOrCompute() as ScrollPaneSkin).bgColor.set(Color(1f, 1f, 1f, 0f))

                this.hBarPolicy.set(ScrollPane.ScrollBarPolicy.NEVER)
                this.vBarPolicy.set(ScrollPane.ScrollBarPolicy.AS_NEEDED)

                val scrollBarSkinID = PRManiaSkins.SCROLLBAR_SKIN
                this.vBar.skinID.set(scrollBarSkinID)
                this.hBar.skinID.set(scrollBarSkinID)

                this.vBar.unitIncrement.set(10f)
                this.vBar.blockIncrement.set(40f)
            }
            
            fun DailyLeaderboardScore.createPane(place: Int): Pane {
                return Pane().apply {
                    this.bounds.height.set(32f)
                    this += HBox().apply {
                        this.bindWidthToParent(adjust = -70f)
                        this += TextLabel("${place}", font = main.fontMainMenuMain).apply {
                            this.renderAlign.set(Align.right)
                            this.padding.set(Insets(0f, 0f, 4f, 4f))
                            this.bounds.width.set(48f)
                        }
                        val flag = CountryFlags.getFlagByCountryCode(this@createPane.countryCode)
                        this += ImageNode(CountryFlags.getTextureRegionForFlag(flag,
                                AssetRegistry.get<Texture>("country_flags"))).apply {
                            this.bindWidthToSelfHeight()
                        }
                        this += TextLabel((this@createPane.name.takeUnless { it.isBlank() } ?: "..."), font = main.fontMainMenuThin).apply {
                            this.renderAlign.set(Align.left)
                            this.padding.set(Insets(0f, 0f, 4f, 4f))
                            this.bounds.width.set(350f)
                        }
                    }

                    this += TextLabel("${this@createPane.score}", font = main.fontMainMenuMain).apply {
                        Anchor.TopRight.configure(this)
                        this.renderAlign.set(Align.center)
                        this.bounds.width.set(64f)
                    }
                }
            }
            
            val dateList = leaderboard.keys.sortedDescending()
            fun updateList(date: LocalDate) {
                val newPane = VBox().apply { 
                    this.temporarilyDisableLayouts {
                        val list = leaderboard.getOrDefault(date, emptyList())

                        val sorted = list.sortedByDescending { it.score }
                        val placeNumbersInverse = mutableMapOf<DailyLeaderboardScore, Int>()
                        
                        var placeNumber = 0
                        var placeValue = -1
                        sorted.asReversed().forEachIndexed { i, score ->
                            if (score.score != placeValue) {
                                placeValue = score.score
                                placeNumber = i + 1
                            }
                            placeNumbersInverse[score] = placeNumber
                        }
                        
                        sorted.forEach { score ->
                            this += score.createPane(sorted.size - placeNumbersInverse.getOrDefault(score, 99999) + 1)
                        }
                    }
                    this.sizeHeightToChildren(100f)
                }
                
                scrollPane.setContent(newPane)
            }


            val startingDate = dateList.firstOrNull() ?: dailyChallengeDate.getOrCompute()

            this += headerHBox.apply {
                this += TextLabel(binding = {
                    Localization.getVar("mainMenu.dailyChallenge.leaderboard.header").use() + " "
                }, font = main.fontMainMenuMain).apply {
                    this.renderAlign.set(Align.left)
                    this.padding.set(Insets(0f, 0f, 4f, 2f))
                    this.tooltipElement.set(createTooltip(resetsAtLocalTimeText))
                    this.resizeBoundsToContent(affectWidth = true, affectHeight = false, limitWidth = 300f)
                }
                this += ComboBox(dateList, startingDate, font = main.fontMainMenuMain).apply {
                    this.bounds.width.set(160f)
                    this.onItemSelected = { newDate ->
                        updateList(newDate)
                    }
                }
            }
            this += scrollPane

            updateList(startingDate)
        }
    }

    
    class WaitingForNonceMenu(menuCol: MenuCollection, val date: LocalDate, val onSuccess: (nonce: UUID?) -> Unit)
        : StandardMenu(menuCol) {
        
        companion object {
            const val TIMEOUT: Long = 5_000L
        }
        
        private sealed class NonceValue {
            object None : NonceValue()
            object Errored : NonceValue()
            class Success(val uuid: UUID) : NonceValue()
        }
        
        private val nonce: AtomicReference<NonceValue> = AtomicReference(NonceValue.None)
        private val timeoutThread: Thread
        private val fetchThread: Thread
        
        init {
            this.setSize(MMMenu.WIDTH_SMALL)
            this.titleText.bind { Localization.getVar("mainMenu.dailyChallenge.title").use() }
            this.contentPane.bounds.height.set(250f)
            this.deleteWhenPopped.set(true)

            contentPane.addChild(TextLabel(Localization.getVar("mainMenu.dailyChallenge.contactingServer")).apply {
                this.markup.set(this@WaitingForNonceMenu.markup)
                this.textColor.set(LongButtonSkin.TEXT_COLOR)
                this.renderAlign.set(Align.center)
                this.doLineWrapping.set(true)
            })
        }
        
        init {
            fun goToErrorMenu() { // Will already be in the gdx thread
                menuCol.popLastMenu(instant = true, playSound = false)
                
                val errorMenu = NonceErrorMenu(menuCol, onSuccess)
                menuCol.addMenu(errorMenu)
                menuCol.pushNextMenu(errorMenu, playSound = false)
            }
            
            timeoutThread = thread(isDaemon = true, name = "Daily Challenge UUID getter timeout", start = true) {
                Thread.sleep(TIMEOUT)
                if (nonce.get() == NonceValue.None) {
                    nonce.set(NonceValue.Errored)
                    Gdx.app.postRunnable {
                        goToErrorMenu()
                    }
                }
            }
            fetchThread = thread(isDaemon = true, name = "Daily Challenge UUID getter", start = true) {
                val uuid: UUID? = try {
                    DailyChallengeUtils.sendNonceRequestSync(date)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                val nonceValue = if (uuid != null) NonceValue.Success(uuid) else NonceValue.Errored
                
                if (nonce.get() == NonceValue.None) {
                    nonce.set(nonceValue)

                    if (nonceValue is NonceValue.Success) {
                        // If nonce is gotten, set in gdx thread, and enter game, and pop this menu
                        Gdx.app.postRunnable {
                            menuCol.popLastMenu(instant = true, playSound = false)
                            onSuccess(nonceValue.uuid)
                        }
                    } else {
                        Gdx.app.postRunnable {
                            goToErrorMenu()
                        }
                    }
                }
            }
        }
    }

    class NonceErrorMenu(menuCol: MenuCollection, val onSuccess: (nonce: UUID?) -> Unit)
        : StandardMenu(menuCol) {
        init {
            this.setSize(MMMenu.WIDTH_SMALL)
            this.titleText.bind { Localization.getVar("mainMenu.dailyChallenge.title").use() }
            this.contentPane.bounds.height.set(250f)
            this.deleteWhenPopped.set(true)

            contentPane.addChild(TextLabel(Localization.getVar("mainMenu.dailyChallenge.contactingServer.failed")).apply {
                this.bindHeightToParent(adjust = -40f)
                this.markup.set(this@NonceErrorMenu.markup)
                this.textColor.set(LongButtonSkin.TEXT_COLOR)
                this.renderAlign.set(Align.topLeft)
                this.doLineWrapping.set(true)
            })
            
            val hbox = HBox().apply {
                Anchor.BottomLeft.configure(this)
                this.spacing.set(8f)
                this.padding.set(Insets(4f, 0f, 2f, 2f))
                this.bounds.height.set(40f)
            }
            contentPane.addChild(hbox)
            hbox.temporarilyDisableLayouts {
                hbox += createSmallButton(binding = { Localization.getVar("common.cancel").use() }).apply {
                    this.bounds.width.set(100f)
                    this.setOnAction {
                        menuCol.popLastMenu()
                    }
                }
                hbox += createSmallButton(binding = { Localization.getVar("mainMenu.play.playAction").use() }).apply {
                    this.bounds.width.set(200f)
                    this.setOnAction {
                        Gdx.app.postRunnable {
                            menuCol.popLastMenu(instant = true, playSound = false)
                            onSuccess(null)
                        }
                    }
                }
            }
        }
    }
    
}
