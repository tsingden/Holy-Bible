package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.model.AppThemeConfig
import com.example.data.model.ButtonStyleOption
import com.example.data.model.NavBarAppearance
import com.example.data.model.ReadingPageBackground
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.SystemLanguage
import com.example.data.model.ThemePreset
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.localization.SystemLocaleManager

@Immutable
data class BibleBookPalette(
    val paperBackground: Color,
    val paperEdge: Color,
    val deskBackground: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val verseNumberInk: Color,
    val spineShadow: Color,
    val goldAccent: Color,
    val leatherAccent: Color,
    val activeVerseTint: Color,
    val highlightGold: Color,
    val highlightRose: Color,
    val highlightSage: Color,
    val highlightSky: Color,
    val isDark: Boolean
)

@Immutable
data class LiquidGlassTokens(
    val config: AppThemeConfig,
    val isDark: Boolean,
    val isGlassActive: Boolean,
    val isCrystalClear: Boolean = false,
    val backdropStart: Color,
    val backdropEnd: Color,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val orbTertiary: Color,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val specularTop: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val accentColor: Color,
    val onAccentColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val transparencyLevel: Float,
    val blurIntensityDp: Float,
    val buttonStyle: ButtonStyleOption,
    val navBarAppearance: NavBarAppearance,
    val fontStyle: ScriptureFontStyle,
    val readingPageBackground: ReadingPageBackground
)

val LocalBiblePalette = staticCompositionLocalOf {
    BibleBookPalette(
        paperBackground = ParchmentPaper,
        paperEdge = ParchmentEdge,
        deskBackground = ParchmentOuterBg,
        inkPrimary = InkEspresso,
        inkSecondary = InkSepiaMuted,
        verseNumberInk = InkVerseNumber,
        spineShadow = SpineShadowColor,
        goldAccent = AntiqueGold,
        leatherAccent = BurgundyLeather,
        activeVerseTint = ActiveVerseLight,
        highlightGold = HighlightGoldLight,
        highlightRose = HighlightRoseLight,
        highlightSage = HighlightSageLight,
        highlightSky = HighlightSkyLight,
        isDark = false
    )
}

val LocalLiquidGlass = staticCompositionLocalOf {
    resolveLiquidGlassTokens(AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE))
}

fun resolveLiquidGlassTokens(config: AppThemeConfig): LiquidGlassTokens {
    val baseBg = Color(config.effectiveBackgroundArgb)
    val accent = Color(config.effectiveAccentArgb)
    val textBase = Color(config.effectiveTextArgb)
    val isDark = if (config.customBackgroundArgb != null) {
        baseBg.luminance() < 0.44f
    } else {
        config.isDarkSurface
    }
    val safeTextPrimary = if (config.customTextArgb != null) {
        textBase
    } else {
        if (isDark) Color(0xFFF3F6FB) else Color(0xFF141A24)
    }
    val safeTextSecondary = safeTextPrimary.copy(alpha = if (isDark) 0.74f else 0.68f)
    val onAccent = if (accent.luminance() < 0.48f) Color.White else Color(0xFF141820)

    val preset = config.preset
    val isCrystalClear = preset == ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS ||
        preset == ThemePreset.CRYSTAL_CLEAR_DARK_GLASS
    val isGlass = preset.isGlassDefault || config.transparencyLevel < 0.88f || config.blurIntensity > 1f
    val alpha = config.transparencyLevel.coerceIn(0.15f, 0.96f)

    val backdropStart: Color
    val backdropEnd: Color
    val orb1: Color
    val orb2: Color
    val orb3: Color
    val surfaceTint: Color

    when (preset) {
        ThemePreset.IOS_LIQUID_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFFDCEBFC)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.14f) else Color(0xFFF3EBFA)
            orb1 = accent.copy(alpha = 0.34f)
            orb2 = Color(0xFF5E5CE6).copy(alpha = 0.24f)
            orb3 = Color(0xFF64D2FF).copy(alpha = 0.28f)
            surfaceTint = if (isDark) Color(0xFF172233) else Color(0xFFFFFFFF)
        }
        ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFFD2ECFF)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.16f) else Color(0xFFEEE8FF)
            orb1 = Color(0xFF38BDF8).copy(alpha = 0.42f)
            orb2 = Color(0xFF818CF8).copy(alpha = 0.32f)
            orb3 = Color(0xFFA5F3FC).copy(alpha = 0.46f)
            surfaceTint = if (isDark) Color(0xFF132238) else Color(0xFFFFFFFF)
        }
        ThemePreset.CRYSTAL_CLEAR_DARK_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF060F1D)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.18f) else Color(0xFF112642)
            orb1 = Color(0xFF0EA5E9).copy(alpha = 0.38f)
            orb2 = Color(0xFF6366F1).copy(alpha = 0.30f)
            orb3 = Color(0xFF22D3EE).copy(alpha = 0.30f)
            surfaceTint = if (isDark) Color(0xFF132238) else Color(0xFFFFFFFF)
        }
        ThemePreset.TRANSPARENT_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF0A1628)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.2f) else Color(0xFF16324F)
            orb1 = accent.copy(alpha = 0.42f)
            orb2 = Color(0xFF7B61FF).copy(alpha = 0.30f)
            orb3 = Color(0xFF38BDF8).copy(alpha = 0.30f)
            surfaceTint = if (isDark) Color(0xFF1A2F4B) else Color(0xFFFFFFFF)
        }
        ThemePreset.DARK_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF0F131C)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.12f) else Color(0xFF1A2234)
            orb1 = accent.copy(alpha = 0.26f)
            orb2 = Color(0xFF4F46E5).copy(alpha = 0.22f)
            orb3 = Color(0xFF0EA5E9).copy(alpha = 0.18f)
            surfaceTint = Color(0xFF1B2333)
        }
        ThemePreset.LIGHT_GLASS -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFFF2F6FC)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.10f) else Color(0xFFE7EFFA)
            orb1 = accent.copy(alpha = 0.20f)
            orb2 = Color(0xFFA78BFA).copy(alpha = 0.16f)
            orb3 = Color(0xFF7DD3FC).copy(alpha = 0.22f)
            surfaceTint = Color(0xFFFFFFFF)
        }
        ThemePreset.AMOLED_BLACK -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF000000)
            backdropEnd = if (config.customBackgroundArgb != null) baseBg else Color(0xFF05070B)
            orb1 = accent.copy(alpha = 0.14f)
            orb2 = Color(0xFF1D4ED8).copy(alpha = 0.10f)
            orb3 = Color.Transparent
            surfaceTint = Color(0xFF10131A)
        }
        ThemePreset.CLASSIC_BIBLE -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFFE7DEC9)
            backdropEnd = if (config.customBackgroundArgb != null) baseBg else Color(0xFFF5ECD9)
            orb1 = accent.copy(alpha = 0.08f)
            orb2 = AntiqueGold.copy(alpha = 0.10f)
            orb3 = Color.Transparent
            surfaceTint = if (isDark) Color(0xFF1E1915) else ParchmentPaper
        }
        ThemePreset.ELEGANT_BLUE -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF09192F)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.16f) else Color(0xFF132E53)
            orb1 = accent.copy(alpha = 0.34f)
            orb2 = Color(0xFF60A5FA).copy(alpha = 0.24f)
            orb3 = Color(0xFF38BDF8).copy(alpha = 0.18f)
            surfaceTint = Color(0xFF163156)
        }
        ThemePreset.NATURE_GREEN -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFFE4F2E9)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.12f) else Color(0xFFCDE5D6)
            orb1 = accent.copy(alpha = 0.26f)
            orb2 = Color(0xFF34D399).copy(alpha = 0.22f)
            orb3 = Color(0xFFA7F3D0).copy(alpha = 0.26f)
            surfaceTint = if (isDark) Color(0xFF142A20) else Color(0xFFF9FCF9)
        }
        ThemePreset.GOLDEN_PREMIUM -> {
            backdropStart = if (config.customBackgroundArgb != null) baseBg else Color(0xFF16100B)
            backdropEnd = if (config.customBackgroundArgb != null) blendColor(baseBg, accent, 0.15f) else Color(0xFF281D13)
            orb1 = accent.copy(alpha = 0.30f)
            orb2 = Color(0xFFB58938).copy(alpha = 0.22f)
            orb3 = Color(0xFF8C4A27).copy(alpha = 0.18f)
            surfaceTint = if (isDark) Color(0xFF241B12) else Color(0xFFFFF8EC)
        }
    }

    val glassSurface = surfaceTint.copy(alpha = alpha)
    val glassSurfaceElevated = if (isCrystalClear) {
        surfaceTint.copy(alpha = (alpha + 0.10f).coerceAtMost(0.84f))
    } else {
        surfaceTint.copy(alpha = (alpha + 0.16f).coerceAtMost(0.97f))
    }

    val specularTop = when {
        isCrystalClear && isDark -> Color(0xFFE0F7FF).copy(alpha = 0.30f)
        isCrystalClear -> Color.White.copy(alpha = 0.94f)
        isDark -> Color.White.copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.78f)
    }
    val borderTop = when {
        isCrystalClear && isDark -> Color(0xFFCFFAFE).copy(alpha = 0.44f)
        isCrystalClear -> Color.White.copy(alpha = 0.96f)
        isDark -> Color.White.copy(alpha = 0.28f)
        else -> Color.White.copy(alpha = 0.85f)
    }
    val borderBottom = when {
        isCrystalClear -> accent.copy(alpha = if (isDark) 0.42f else 0.36f)
        isDark -> accent.copy(alpha = 0.24f)
        else -> accent.copy(alpha = 0.18f)
    }

    return LiquidGlassTokens(
        config = config,
        isDark = isDark,
        isGlassActive = isGlass,
        isCrystalClear = isCrystalClear,
        backdropStart = backdropStart,
        backdropEnd = backdropEnd,
        orbPrimary = orb1,
        orbSecondary = orb2,
        orbTertiary = orb3,
        glassSurface = glassSurface,
        glassSurfaceElevated = glassSurfaceElevated,
        specularTop = specularTop,
        borderTop = borderTop,
        borderBottom = borderBottom,
        accentColor = accent,
        onAccentColor = onAccent,
        textPrimary = safeTextPrimary,
        textSecondary = safeTextSecondary,
        transparencyLevel = alpha,
        blurIntensityDp = config.blurIntensity,
        buttonStyle = config.buttonStyle,
        navBarAppearance = config.navBarAppearance,
        fontStyle = config.fontStyle,
        readingPageBackground = config.readingPageBackground
    )
}

fun resolveBiblePalette(tokens: LiquidGlassTokens): BibleBookPalette {
    val config = tokens.config
    val accent = tokens.accentColor

    val effectiveReadingBg = when (config.readingPageBackground) {
        ReadingPageBackground.MATCH_THEME -> {
            if (config.preset == ThemePreset.CLASSIC_BIBLE) {
                if (tokens.isDark) ReadingPageBackground.NIGHT_OBSIDIAN else ReadingPageBackground.CREAM_PARCHMENT
            } else if (config.preset == ThemePreset.AMOLED_BLACK) {
                ReadingPageBackground.AMOLED_BLACK
            } else if (config.preset == ThemePreset.GOLDEN_PREMIUM) {
                ReadingPageBackground.SEPIA_WARM
            } else {
                ReadingPageBackground.FROSTED_GLASS
            }
        }
        else -> config.readingPageBackground
    }

    val paperBg: Color
    val paperEdge: Color
    val deskBg: Color
    val inkPrimary: Color
    val inkSecondary: Color
    val verseInk: Color
    val pageIsDark: Boolean

    when (effectiveReadingBg) {
        ReadingPageBackground.CREAM_PARCHMENT -> {
            paperBg = ParchmentPaper
            paperEdge = ParchmentEdge
            deskBg = if (tokens.isGlassActive) Color.Transparent else ParchmentOuterBg
            inkPrimary = if (config.customTextArgb != null && !tokens.isDark) tokens.textPrimary else InkEspresso
            inkSecondary = InkSepiaMuted
            verseInk = if (config.preset == ThemePreset.CLASSIC_BIBLE) InkVerseNumber else accent
            pageIsDark = false
        }
        ReadingPageBackground.PURE_WHITE -> {
            paperBg = Color(0xFFFFFFFF)
            paperEdge = Color(0xFFEFF3F8)
            deskBg = if (tokens.isGlassActive) Color.Transparent else Color(0xFFE4E9F0)
            inkPrimary = Color(0xFF111827)
            inkSecondary = Color(0xFF4B5563)
            verseInk = accent
            pageIsDark = false
        }
        ReadingPageBackground.SEPIA_WARM -> {
            paperBg = Color(0xFFF3E7D0)
            paperEdge = Color(0xFFE4D2B2)
            deskBg = if (tokens.isGlassActive) Color.Transparent else Color(0xFFDCC7A3)
            inkPrimary = Color(0xFF2D2114)
            inkSecondary = Color(0xFF6E5740)
            verseInk = Color(0xFF8D4E22)
            pageIsDark = false
        }
        ReadingPageBackground.NIGHT_OBSIDIAN -> {
            paperBg = Color(0xFF161B26).copy(alpha = if (tokens.isGlassActive) 0.90f else 1f)
            paperEdge = Color(0xFF222A3A)
            deskBg = if (tokens.isGlassActive) Color.Transparent else NightOuterBg
            inkPrimary = Color(0xFFEBF0F7)
            inkSecondary = Color(0xFF9BA8BE)
            verseInk = if (accent.luminance() < 0.35f) Color(0xFF7CC4FF) else accent
            pageIsDark = true
        }
        ReadingPageBackground.AMOLED_BLACK -> {
            paperBg = Color(0xFF000000)
            paperEdge = Color(0xFF12151C)
            deskBg = Color(0xFF000000)
            inkPrimary = Color(0xFFF3F4F6)
            inkSecondary = Color(0xFF9CA3AF)
            verseInk = if (accent.luminance() < 0.35f) GoldShimmer else accent
            pageIsDark = true
        }
        ReadingPageBackground.FROSTED_GLASS, ReadingPageBackground.MATCH_THEME -> {
            val frostedAlpha = if (tokens.isCrystalClear) {
                (tokens.transparencyLevel + 0.24f).coerceIn(0.42f, 0.80f)
            } else {
                (tokens.transparencyLevel + 0.16f).coerceIn(0.46f, 0.92f)
            }
            if (tokens.isDark) {
                paperBg = blendColor(tokens.backdropStart, Color(0xFF1A2233), 0.45f).copy(alpha = frostedAlpha)
                paperEdge = if (tokens.isCrystalClear) Color(0xFFCFFAFE).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
                deskBg = Color.Transparent
                inkPrimary = tokens.textPrimary
                inkSecondary = tokens.textSecondary
                verseInk = if (accent.luminance() < 0.35f) Color(0xFF7CC4FF) else accent
                pageIsDark = true
            } else {
                val lightAlpha = if (tokens.isCrystalClear) {
                    (tokens.transparencyLevel + 0.34f).coerceIn(0.52f, 0.84f)
                } else {
                    (tokens.transparencyLevel + 0.22f).coerceIn(0.62f, 0.94f)
                }
                paperBg = Color(0xFFFCFDFF).copy(alpha = lightAlpha)
                paperEdge = if (tokens.isCrystalClear) Color.White.copy(alpha = 0.72f) else accent.copy(alpha = 0.10f)
                deskBg = Color.Transparent
                inkPrimary = tokens.textPrimary
                inkSecondary = tokens.textSecondary
                verseInk = if (accent.luminance() > 0.72f) InkVerseNumber else accent
                pageIsDark = false
            }
        }
    }

    val primaryBrand = if (config.preset == ThemePreset.CLASSIC_BIBLE && config.customAccentArgb == null) {
        BurgundyLeather
    } else {
        accent
    }
    val secondaryBrand = if (config.preset == ThemePreset.CLASSIC_BIBLE && config.customAccentArgb == null) {
        AntiqueGold
    } else {
        blendColor(accent, if (pageIsDark) Color.White else Color(0xFFB58938), 0.28f)
    }

    return BibleBookPalette(
        paperBackground = paperBg,
        paperEdge = paperEdge,
        deskBackground = deskBg,
        inkPrimary = inkPrimary,
        inkSecondary = inkSecondary,
        verseNumberInk = verseInk,
        spineShadow = if (pageIsDark) Color.Black else SpineShadowColor,
        goldAccent = secondaryBrand,
        leatherAccent = primaryBrand,
        activeVerseTint = accent.copy(alpha = 0.22f),
        highlightGold = if (pageIsDark) HighlightGoldDark else HighlightGoldLight,
        highlightRose = if (pageIsDark) HighlightRoseDark else HighlightRoseLight,
        highlightSage = if (pageIsDark) HighlightSageDark else HighlightSageLight,
        highlightSky = if (pageIsDark) HighlightSkyDark else HighlightSkyLight,
        isDark = pageIsDark
    )
}

@Composable
fun HolyBibleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeConfig: AppThemeConfig = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE),
    systemLanguage: SystemLanguage = SystemLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val tokens = resolveLiquidGlassTokens(themeConfig)
    val biblePalette = resolveBiblePalette(tokens)
    SystemLocaleManager.currentLanguage = systemLanguage
    val appStrings = SystemLocaleManager.strings(systemLanguage)
    val activeTypography = if (systemLanguage == SystemLanguage.NEPALI) {
        NepaliBibleTypography
    } else {
        HolyBibleTypography
    }

    val colorScheme = if (tokens.isDark) {
        darkColorScheme(
            primary = tokens.accentColor,
            onPrimary = tokens.onAccentColor,
            primaryContainer = tokens.accentColor.copy(alpha = 0.22f),
            onPrimaryContainer = tokens.textPrimary,
            secondary = biblePalette.goldAccent,
            onSecondary = tokens.onAccentColor,
            secondaryContainer = tokens.glassSurfaceElevated,
            onSecondaryContainer = tokens.textPrimary,
            tertiary = BurgundySoft,
            onTertiary = Color.White,
            background = tokens.backdropStart,
            onBackground = tokens.textPrimary,
            surface = tokens.glassSurfaceElevated,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.glassSurface,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.borderTop
        )
    } else {
        lightColorScheme(
            primary = tokens.accentColor,
            onPrimary = tokens.onAccentColor,
            primaryContainer = tokens.accentColor.copy(alpha = 0.14f),
            onPrimaryContainer = tokens.textPrimary,
            secondary = biblePalette.goldAccent,
            onSecondary = Color.White,
            secondaryContainer = tokens.glassSurfaceElevated,
            onSecondaryContainer = tokens.textPrimary,
            tertiary = BurgundySoft,
            onTertiary = Color.White,
            background = tokens.backdropStart,
            onBackground = tokens.textPrimary,
            surface = tokens.glassSurfaceElevated,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.glassSurface,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.borderBottom
        )
    }

    CompositionLocalProvider(
        LocalBiblePalette provides biblePalette,
        LocalLiquidGlass provides tokens,
        LocalAppStrings provides appStrings,
        LocalSystemLanguage provides systemLanguage
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = activeTypography,
            content = content
        )
    }
}

private fun blendColor(a: Color, b: Color, ratio: Float): Color {
    val t = ratio.coerceIn(0f, 1f)
    return Color(
        red = a.red * (1f - t) + b.red * t,
        green = a.green * (1f - t) + b.green * t,
        blue = a.blue * (1f - t) + b.blue * t,
        alpha = 1f
    )
}
