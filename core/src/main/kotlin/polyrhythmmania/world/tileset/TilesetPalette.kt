package polyrhythmmania.world.tileset

import com.badlogic.gdx.graphics.Color
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import paintbox.util.gdxutils.grey
import paintbox.util.gdxutils.set


/**
 * Describes a particular palette configuration of the colours for [Tileset].
 */
class TilesetPalette {
    
    companion object {
        fun createColourlessTilesetPalette(): TilesetPalette {
            return TilesetPalette().apply {
                rodABorder.color.getOrCompute().grey(0f)
                rodAFill.color.getOrCompute().grey(1f)
                rodDpadBorder.color.getOrCompute().grey(0f)
                rodDpadFill.color.getOrCompute().grey(1f)

                cubeBorder.color.getOrCompute().grey(1f)
                cubeBorderZ.color.getOrCompute().grey(1f)
                cubeFaceX.color.getOrCompute().grey(1f)
                cubeFaceY.color.getOrCompute().grey(1f)
                cubeFaceZ.color.getOrCompute().grey(1f)

                pistonAFaceX.color.getOrCompute().grey(1f)
                pistonAFaceZ.color.getOrCompute().grey(1f)
                pistonDpadFaceX.color.getOrCompute().grey(1f)
                pistonDpadFaceZ.color.getOrCompute().grey(1f)

                signShadow.color.getOrCompute().grey(0f)
            }
        }
        
        fun createGBA1TilesetPalette(): TilesetPalette {
            return TilesetPalette().apply {
                rodABorder.color.getOrCompute().set(0, 0, 0)
                rodAFill.color.getOrCompute().set(255, 8, 0)
                rodDpadBorder.color.getOrCompute().set(0, 0, 0)
                rodDpadFill.color.getOrCompute().set(255, 8, 0)
                
                cubeBorder.color.getOrCompute().set(33, 214, 25)
                cubeBorderZ.color.getOrCompute().set(24, 181, 16)
                cubeFaceX.color.getOrCompute().set(42, 224, 48)
                cubeFaceY.color.getOrCompute().set(74, 255, 74)
                cubeFaceZ.color.getOrCompute().set(58, 230, 49)

                pistonAFaceX.color.getOrCompute().set(33, 82, 206)
                pistonAFaceZ.color.getOrCompute().set(41, 99, 255)
                pistonDpadFaceX.color.getOrCompute().set(33, 82, 206)
                pistonDpadFaceZ.color.getOrCompute().set(41, 99, 255)

                signShadow.color.getOrCompute().set(33, 214, 25)
            }
        }

        fun createGBA2TilesetPalette(): TilesetPalette {
            return TilesetPalette().apply {
                rodABorder.color.getOrCompute().set(0, 0, 0)
                rodAFill.color.getOrCompute().set(255, 8, 0)
                rodDpadBorder.color.getOrCompute().set(0, 0, 0)
                rodDpadFill.color.getOrCompute().set(255, 8, 0)

                cubeBorder.color.getOrCompute().set(0, 16, 189)
                cubeBorderZ.color.getOrCompute().set(24, 66, 173)
                cubeFaceX.color.getOrCompute().set(32, 81, 204)
                cubeFaceY.color.getOrCompute().set(41, 99, 255)
                cubeFaceZ.color.getOrCompute().set(33, 82, 214)

                pistonAFaceX.color.getOrCompute().set(214, 181, 8)
                pistonAFaceZ.color.getOrCompute().set(255, 214, 16)
                pistonDpadFaceX.color.getOrCompute().set(214, 181, 8)
                pistonDpadFaceZ.color.getOrCompute().set(255, 214, 16)

                signShadow.color.getOrCompute().set(0, 16, 189)
            }
        }
        
        fun createAssembleTilesetPalette(): TilesetPalette {
            return createGBA1TilesetPalette().apply {
                pistonAFaceX.color.getOrCompute().set(214, 181, 8)
                pistonAFaceZ.color.getOrCompute().set(255, 214, 16)
                
                aliasAsmLaneBorder.color.getOrCompute().set(41, 172, 18)
                aliasAsmLaneTop.color.getOrCompute().set(137, 255, 84)
            }
        }

        fun createOrangeBlueTilesetPalette(): TilesetPalette {
            // https://jfly.uni-koeln.de/color/#pallet
            return TilesetPalette().apply {
                rodABorder.color.getOrCompute().set(0, 0, 0)
                rodAFill.color.getOrCompute().set(255, 0x6E, 0)
                rodDpadBorder.color.getOrCompute().set(0, 0, 0)
                rodDpadFill.color.getOrCompute().set(255, 0x6E, 0)

                cubeBorder.color.getOrCompute().set(0, 0x4C, 0x87)
                cubeBorderZ.color.getOrCompute().set(0, 0x38, 0x65)
                cubeFaceX.color.getOrCompute().set(0, 0x56, 0x91)
                cubeFaceY.color.getOrCompute().set(0, 0x71, 0xB2)
                cubeFaceZ.color.getOrCompute().set(0, 0x5C, 0x98)

                pistonAFaceX.color.getOrCompute().set(188, 179, 50)
                pistonAFaceZ.color.getOrCompute().set(0xEF, 0xE4, 0x40)
                pistonDpadFaceX.color.getOrCompute().set(188, 179, 50)
                pistonDpadFaceZ.color.getOrCompute().set(0xEF, 0xE4, 0x40)

                signShadow.color.getOrCompute().set(0, 0x4C, 0x87)
            }
        }
    }

    val cubeBorder: ColorMapping = ColorMapping("cubeBorder", { it.cubeBorder.color })
    val cubeBorderZ: ColorMapping = ColorMapping("cubeBorderZ", { it.cubeBorderZ.color }, fallbackIDs = setOf("cubeBorder"))
    val cubeFaceX: ColorMapping = ColorMapping("cubeFaceX", { it.cubeFaceX.color })
    val cubeFaceY: ColorMapping = ColorMapping("cubeFaceY", { it.cubeFaceY.color })
    val cubeFaceZ: ColorMapping = ColorMapping("cubeFaceZ", { it.cubeFaceZ.color })
    val pistonAFaceX: ColorMapping = ColorMapping("pistonAFaceX", { it.pistonAFaceXColor }, fallbackIDs = setOf("pistonFaceX"))
    val pistonAFaceZ: ColorMapping = ColorMapping("pistonAFaceZ", { it.pistonAFaceZColor }, fallbackIDs = setOf("pistonFaceZ"))
    val pistonDpadFaceX: ColorMapping = ColorMapping("pistonDpadFaceX", { it.pistonDpadFaceXColor }, fallbackIDs = setOf("pistonFaceX"))
    val pistonDpadFaceZ: ColorMapping = ColorMapping("pistonDpadFaceZ", { it.pistonDpadFaceZColor }, fallbackIDs = setOf("pistonFaceZ"))
    val signShadow: ColorMapping = ColorMapping("signShadow", { it.signShadowColor }, canAdjustAlpha = true)
    val rodABorder: ColorMapping = ColorMapping("rodABorder", { it.rodABorderColor }, fallbackIDs = setOf("rodBorder"))
    val rodAFill: ColorMapping = ColorMapping("rodAFill", { it.rodAFillColor }, fallbackIDs = setOf("rodFill"))
    val rodDpadBorder: ColorMapping = ColorMapping("rodDpadBorder", { it.rodDpadBorderColor }, fallbackIDs = setOf("rodBorder"))
    val rodDpadFill: ColorMapping = ColorMapping("rodDpadFill", { it.rodDpadFillColor }, fallbackIDs = setOf("rodFill"))
    val backgroundBack: ColorMapping = ColorMapping("backgroundBack", { it.backgroundBack.color }, canAdjustAlpha = true)
    val backgroundMiddle: ColorMapping = ColorMapping("backgroundMiddle", { it.backgroundMiddle.color }, canAdjustAlpha = true)
    val backgroundFore: ColorMapping = ColorMapping("backgroundFore", { it.backgroundFore.color }, canAdjustAlpha = true)

    val allMappings: List<ColorMapping> = listOf(cubeBorder, cubeBorderZ, /*cubeFaceX,*/ cubeFaceY, cubeFaceZ,
            pistonAFaceX, pistonAFaceZ, pistonDpadFaceX, pistonDpadFaceZ, signShadow, rodABorder, rodAFill, rodDpadBorder, rodDpadFill,
            backgroundBack, backgroundMiddle, backgroundFore) + listOf(cubeFaceX) /* Deprioritized to be later in the list. */
    val allMappingsByID: Map<String, ColorMapping> = allMappings.associateBy { it.id }

    // Extra mapping aliases, DO NOT ADD to allMappings
    val aliasAsmLaneBorder: ColorMapping get() = pistonDpadFaceX
    val aliasAsmLaneTop: ColorMapping get() = pistonDpadFaceZ
    
    fun copy(): TilesetPalette {
        return TilesetPalette().also { copy ->
            val copyMap = copy.allMappings.associateBy { it.id }
            this.allMappings.forEach { m ->
                val copiedMapping = copyMap.getValue(m.id)
                copiedMapping.color.set(m.color.getOrCompute().cpy())
                copiedMapping.enabled.set(m.enabled.get())
            }
        }
    }
    
    fun copyFrom(tileset: Tileset) {
        allMappings.forEach { m ->
            m.copyFrom(tileset)
        }
    }
    
    fun applyTo(tileset: Tileset) {
        allMappings.forEach { m ->
            m.applyTo(tileset)
        }
    }

    fun toJson(): JsonObject {
        return Json.`object`().apply {
            allMappings.forEach { m ->
                add(m.id, m.color.getOrCompute().toString())
            }
            add("_metadata", Json.`object`().also { metadataObj ->
                metadataObj.add("enabled", Json.array(*allMappings.filter { it.enabled.get() }.map { it.id }.toTypedArray()))
            })
        }
    }

    fun fromJson(obj: JsonObject) {
        fun attemptParse(id: String): Color? {
            return try {
                val str = obj.getString(id, "")
                if (str == "") return null
                Color.valueOf(str)
            } catch (ignored: Exception) {
                null
            }
        }

        val mappingsWithoutValidJson = mutableSetOf<ColorMapping>()
        allMappings.forEach { m ->
            val c = attemptParse(m.id)
            if (c != null) {
                m.color.set(c)
            } else {
                var foundFallback = false
                if (m.fallbackIDs.isNotEmpty()) {
                    for (fallback in m.fallbackIDs) {
                        val fallbackColor = attemptParse(fallback)
                        if (fallbackColor != null) {
                            m.color.set(fallbackColor)
                            foundFallback = true
                            break
                        }
                    }
                }
                if (!foundFallback) {
                    mappingsWithoutValidJson += m
                }
            }
        }
        
        val metadataObj = obj.get("_metadata")?.asObject()
        if (metadataObj != null) {
            val enabledArr = metadataObj.get("enabled")?.asArray()
            if (enabledArr != null) {
                val set = enabledArr.map { it.asString() }.toSet()
                val mappingsByID: Map<String, ColorMapping> = allMappingsByID
                allMappings.forEach { m ->
                    if (m in mappingsWithoutValidJson) {
                        m.enabled.set(m.defaultEnabledStateIfWasOlderVersion)
                    } else {
                        m.enabled.set(false)
                    }
                }
                set.forEach { id ->
                    val m = mappingsByID[id]
                    if (m != null) {
                        m.enabled.set(true)
                    } else {
                        // Find any mappings who have a fallback ID with id
                        mappingsByID.values.filter { id in it.fallbackIDs }.forEach { cm ->
                            cm.enabled.set(true)
                        }
                    }
                }
            }
        } else {
            allMappings.forEach { m ->
                if (m in mappingsWithoutValidJson) {
                    m.enabled.set(m.defaultEnabledStateIfWasOlderVersion)
                } else {
                    m.enabled.set(true)
                }
            }
        }
    }
}
