package polyrhythmmania.engine.input

import com.badlogic.gdx.graphics.Color
import com.eclipsesource.json.JsonObject
import polyrhythmmania.engine.Engine


data class Challenges(val tempoUp: Int, val goingForPerfect: Boolean) {
    companion object {
        val NO_CHANGES: Challenges = Challenges(tempoUp = 100, goingForPerfect = false)
        
        val TEMPO_DOWN_COLOR: Color = Color.valueOf("3E5BEF")
        val TEMPO_UP_COLOR: Color = Color.valueOf("ED3D3D")


        fun fromJson(obj: JsonObject): Challenges {
            return Challenges(obj.getInt("tempoUp", 100), obj.getBoolean("perfect", false))
        }
    }

    fun toJson(obj: JsonObject) {
        obj.add("tempoUp", tempoUp)
        obj.add("perfect", goingForPerfect)
    }
    
    fun applyToEngine(engine: Engine) {
        engine.playbackSpeed = tempoUp / 100f
        engine.modifiers.perfectChallenge.enabled.set(goingForPerfect)
    }
}
