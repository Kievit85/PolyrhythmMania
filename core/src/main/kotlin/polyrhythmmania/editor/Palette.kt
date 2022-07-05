package polyrhythmmania.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import paintbox.binding.Var
import paintbox.font.Markup
import paintbox.font.PaintboxFont
import paintbox.font.TextRun
import paintbox.registry.AssetRegistry
import paintbox.util.gdxutils.grey
import polyrhythmmania.PRManiaColors
import polyrhythmmania.PRManiaGame


open class Palette(val main: PRManiaGame) {
    protected val background: Var<Color> = Var(Color().grey(0f, 0.75f))
    protected val borderTrim: Var<Color> = Var(Color().grey(1f, 1f))
    protected val separator: Var<Color> = Var(Color().grey(0.75f, 0.5f))

    val bgColor: Var<Color> = Var(Color().grey(0.094f))
    val toolbarBg: Var<Color> = Var(Color().grey(0.3f))

    val menubarIconTint: Var<Color> = Var(Color().grey(0.1f))

    val statusBg: Var<Color> = Var.bind { background.use() }
    val statusBorder: Var<Color> = Var.bind { borderTrim.use() }
    val statusTextColor: Var<Color> = Var(Color().grey(1f, 1f))

    val upperPaneBorder: Var<Color> = Var.bind { borderTrim.use() }
    val instantiatorPaneBorder: Var<Color> = Var.bind { upperPaneBorder.use() }
    val instantiatorSummaryText: Var<Color> = Var.bind { Color().grey(1f, 1f) }
    val instantiatorDescText: Var<Color> = Var.bind { instantiatorSummaryText.use() }
    val previewPaneBorder: Var<Color> = Var.bind { borderTrim.use() }
    val previewPaneSeparator: Var<Color> = Var.bind { separator.use() }
    val toolbarIconTint: Var<Color> = Var.bind { menubarIconTint.use() }
    val toolbarIconToolNeutralTint: Var<Color> = Var.bind { toolbarIconTint.use() }
    val toolbarIconToolActiveTint: Var<Color> = Var(Color.valueOf("00A070"))
    val toolbarIndentedButtonBorderTint: Var<Color> = Var(Color.valueOf("00F2AD"))

    val trackPaneBorder: Var<Color> = Var.bind { borderTrim.use() }
    val trackPaneTimeBg: Var<Color> = Var(Color().grey(0f, 1f))
    val trackPaneTimeText: Var<Color> = Var(Color().grey(1f, 1f))
    val trackPaneTextColor: Var<Color> = Var(Color().grey(1f, 1f))
    val trackPaneTempoBg: Var<Color> = Var(PRManiaColors.TEMPO)
    val trackPaneMusicVolBg: Var<Color> = Var(PRManiaColors.MUSIC_VOLUME)
    val trackVerticalBeatLineColor: Var<Color> = Var(Color().grey(1f, 0.4f))
    val trackPlayback: Var<Color> = Var(Color(0f, 1f, 0f, 1f))

    // Fonts

    val sidePanelFont: PaintboxFont = main.mainFontBordered
    val beatTimeFont: PaintboxFont = main.fontEditorBeatTime
    val beatSecondsFont: PaintboxFont = main.fontCache["editor_status"]
    val beatTrackFont: PaintboxFont = main.fontEditorBeatTrack
    val beatMarkerFont: PaintboxFont = main.fontEditorMarker
    val instantiatorNameFont: PaintboxFont = main.fontEditorInstantiatorName
    val instantiatorSummaryFont: PaintboxFont = main.fontEditorInstantiatorSummary
    val instantiatorDescFont: PaintboxFont = main.fontEditorInstantiatorSummary
    val exitDialogFont: PaintboxFont = main.mainFont
    val musicDialogFont: PaintboxFont = main.mainFont
    val musicDialogFontBold: PaintboxFont = main.mainFontBold
    val loadDialogFont: PaintboxFont = main.mainFont
    val musicScoreFont: PaintboxFont = main.fontEditorMusicScore
    val rodinDialogFont: PaintboxFont = main.fontRodinFixed
    val rodinBorderedDialogFont: PaintboxFont = main.fontRodinFixedBordered

    // Markup
    val markup: Markup = Markup(mapOf(
            Markup.FONT_NAME_BOLD to main.mainFontBold,
            Markup.FONT_NAME_ITALIC to main.mainFontItalic,
            Markup.FONT_NAME_BOLDITALIC to main.mainFontBoldItalic,
            "rodin" to main.fontRodinFixed,
            "prmania_icons" to main.fontIcons,
    ), TextRun(main.mainFont, ""), Markup.FontStyles.ALL_USING_BOLD_ITALIC)
    val markupBordered: Markup = Markup(mapOf(
            Markup.FONT_NAME_BOLD to main.mainFontBoldBordered,
            Markup.FONT_NAME_ITALIC to main.mainFontItalicBordered,
            Markup.FONT_NAME_BOLDITALIC to main.mainFontBoldItalicBordered,
            "rodin" to main.fontRodinFixedBordered,
            "prmania_icons" to main.fontIcons,
    ), TextRun(main.mainFontBordered, ""), Markup.FontStyles.ALL_USING_BOLD_ITALIC)
    val markupInstantiatorSummary: Markup = Markup(mapOf(
            "rodin" to main.fontRodinFixed,
            "prmania_icons" to main.fontIcons,
    ), TextRun(instantiatorSummaryFont, ""), Markup.FontStyles.ALL_USING_DEFAULT_FONT)
    val markupInstantiatorDesc: Markup = Markup(mapOf(
            Markup.FONT_NAME_BOLD to main.fontCache["editor_instantiator_desc_BOLD"],
            Markup.FONT_NAME_ITALIC to main.fontCache["editor_instantiator_desc_ITALIC"],
            Markup.FONT_NAME_BOLDITALIC to main.fontCache["editor_instantiator_desc_BOLD_ITALIC"],
            "rodin" to main.fontRodinFixed,
            "prmania_icons" to main.fontIcons,
    ), TextRun(main.fontCache["editor_instantiator_desc"], ""), Markup.FontStyles.ALL_USING_BOLD_ITALIC)
    val markupStatusBar: Markup = Markup(mapOf(
            Markup.FONT_NAME_BOLD to main.fontCache["editor_status_BOLD"],
            Markup.FONT_NAME_ITALIC to main.fontCache["editor_status_ITALIC"],
            Markup.FONT_NAME_BOLDITALIC to main.fontCache["editor_status_BOLD_ITALIC"],
            "rodin" to main.fontRodinFixed,
            "prmania_icons" to main.fontIcons,
    ), TextRun(main.fontCache["editor_status"], ""), Markup.FontStyles.ALL_USING_BOLD_ITALIC)
    val markupHelp: Markup = markupInstantiatorDesc /*Markup(mapOf(
            Markup.FONT_NAME_BOLD to main.fontCache["editor_help_BOLD"],
            Markup.FONT_NAME_ITALIC to main.fontCache["editor_help_ITALIC"],
            Markup.FONT_NAME_BOLDITALIC to main.fontCache["editor_help_BOLD_ITALIC"],
            "rodin" to main.fontRodinFixed,
            "prmania_icons" to main.fontIcons,
    ), TextRun(main.fontCache["editor_help"], ""), Markup.FontStyles("bold", "italic", "bolditalic"))*/

    // Textures
    val blockFlatTexture: Texture by lazy { AssetRegistry.get<Texture>("ui_icon_block_flat") }
    val blockFlatPlatformRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 0, 16 * 0, 16, 16) }
    val blockFlatPistonARegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 1, 16 * 0, 16, 16) }
    val blockFlatPistonDpadRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 2, 16 * 0, 16, 16) }
    val blockFlatNoneRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 3, 16 * 0, 16, 16) }
    val blockFlatCube1Region: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 0, 16 * 1, 16, 16) }
    val blockFlatCube2Region: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 1, 16 * 1, 16, 16) }
    val blockFlatCube1LineRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 2, 16 * 1, 16, 16) }
    val blockFlatCube2LineRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 3, 16 * 1, 16, 16) }
    val blockFlatPistonAOpenRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 1, 16 * 2, 16, 16) }
    val blockFlatPistonDpadOpenRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 2, 16 * 2, 16, 16) }
    val blockFlatNoChangeRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 3, 16 * 2, 16, 16) }
    val blockFlatRetractRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 0, 16 * 3, 16, 16) }
    val blockFlatSpotlightRegion: TextureRegion by lazy { TextureRegion(blockFlatTexture, 16 * 1, 16 * 3, 16, 16) }
    
}