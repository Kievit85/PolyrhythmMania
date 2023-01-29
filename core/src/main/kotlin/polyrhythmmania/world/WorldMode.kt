package polyrhythmmania.world

import polyrhythmmania.world.render.bg.NoOpWorldBackground
import polyrhythmmania.world.render.bg.WorldBackground


sealed class WorldType(val defaultBackground: WorldBackground) {

    class Polyrhythm(
        val showRaisedPlatformsRepeated: Boolean,
    ) : WorldType(NoOpWorldBackground) {

        constructor() : this(showRaisedPlatformsRepeated = false)

    }

    object Dunk : WorldType(DunkWorldBackground.Default)

    object Assemble : WorldType(AssembleWorldBackground)

}

data class WorldMode(val worldType: WorldType)
