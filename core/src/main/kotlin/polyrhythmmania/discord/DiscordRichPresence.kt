package polyrhythmmania.discord

import com.badlogic.gdx.utils.Disposable
import de.jcm.discordgamesdk.*
import de.jcm.discordgamesdk.activity.Activity
import de.jcm.discordgamesdk.user.DiscordUser
import paintbox.Paintbox
import paintbox.binding.BooleanVar
import paintbox.binding.ReadOnlyBooleanVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.logging.Logger

/**
 * A basic interface to [Core] that supports changing the activity and getting the current user.
 */
object DiscordRichPresence : Disposable {

    private val _loaded: BooleanVar = BooleanVar(false)
    val loaded: ReadOnlyBooleanVar = _loaded
    private val _lastCallbacksResult: Var<Result> = Var(Result.OK)
    val lastCallbacksResult: ReadOnlyVar<Result> = _lastCallbacksResult

    val initTime: Long = System.currentTimeMillis()

    val enableRichPresence: BooleanVar = BooleanVar(true)

    val eventHandler: DiscordEventHandler = DiscordEventHandler()
    private lateinit var core: Core

    init {
        synchronized(this) {
            attemptLoadCore()

            enableRichPresence.addListener {
                if (!it.getOrCompute()) {
                    clearActivity()
                }
            }
        }
    }

    private fun isLoaded(): Boolean = this.loaded.get()

    private fun attemptLoadCore() {
        try {
            DiscordSDKNatives.initCore(force = false)

            val createParams = CreateParams().apply {
                this.clientID = DiscordAppID.DISCORD_APP_ID
                this.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD)
                this.registerEventHandler(eventHandler)
            }
            this.core = Core(createParams).apply { 
                this.setLogHook(LogLevel.INFO) { lvl, msg ->
                    Paintbox.LOGGER.log(when (lvl) {
                        LogLevel.ERROR -> Logger.LogLevel.ERROR
                        LogLevel.WARN, null -> Logger.LogLevel.WARN
                        LogLevel.INFO -> Logger.LogLevel.INFO
                        LogLevel.DEBUG -> Logger.LogLevel.DEBUG
                    }, msg ?: "null", tag = "DiscordRichPresence")
                }
            }

            this._loaded.set(true)
            Paintbox.LOGGER.info("[DiscordRichPresence] Loaded successfully.")
        } catch (t: Throwable) {
            Paintbox.LOGGER.warn("[DiscordRichPresence] Could not init Core. Discord functionality disabled.")
            t.printStackTrace()
            return
        }
    }

    fun runCallbacks() {
        if (!isLoaded()) return
        try {
            this.core.runCallbacks() // May throw exception if Discord is closed?
            _lastCallbacksResult.set(Result.OK)
        } catch (e: Exception) {
            var result: Result? = null
            if (e is GameSDKException) {
                result = e.result
                _lastCallbacksResult.set(e.result)
            }
            this._loaded.set(false)
            Paintbox.LOGGER.warn("[DiscordRichPresence] Error while running callbacks. Discord functionality has been disabled. Result = $result")
            e.printStackTrace()
        }
    }

    fun updateActivity(presence: PresenceData, callback: ((Result) -> Unit)? = null) {
        if (!isLoaded()) return
        if (!enableRichPresence.get()) return
        try {
            Activity().use { activity ->
                presence.mutateActivity(activity)

                this.core.activityManager().updateActivity(activity, callback ?: { result ->
                    if (result != Result.OK) {
                        Paintbox.LOGGER.warn("[DiscordRichPresence] Non-OK result when updating activity: $result")
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearActivity() {
        if (!isLoaded()) return
        try {
            this.core.activityManager().clearActivity()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCore(): Core? {
        if (!isLoaded()) return null
        return this.core
    }

    fun getCurrentUser(): DiscordUser? {
        if (!isLoaded()) return null
        return try {
            this.core.userManager().currentUser
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun dispose() {
        if (!isLoaded()) return
        this._loaded.set(false)
        this.core.close()
    }
}