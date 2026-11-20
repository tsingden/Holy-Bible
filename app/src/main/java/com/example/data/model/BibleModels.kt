package com.example.data.model

import androidx.compose.ui.text.font.FontFamily
import com.example.ui.theme.CrimsonProFamily
import com.example.ui.theme.InterFamily
import com.example.ui.theme.LoraFamily
import com.example.ui.theme.MerriweatherFamily
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.NotoSerifKrFamily
import com.example.ui.theme.PlusJakartaSansFamily

enum class BibleLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val translationName: String,
    val licenseNote: String,
    val isRtl: Boolean,
    val otBookRange: IntRange?,
    val ntBookRange: IntRange?
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English (KJV)",
        translationName = "King James Version",
        licenseNote = "Public Domain • 66 Books (OT & NT)",
        isRtl = false,
        otBookRange = 1..39,
        ntBookRange = 40..66
    ),
    NEPALI(
        code = "ne",
        displayName = "Nepali (ULB)",
        nativeName = "नेपाली (ULB)",
        translationName = "Unlocked Literal Bible (ULB)",
        licenseNote = "Door43 World Missions Community © 2019 • CC BY-SA 4.0 • 66 Books (OT & NT)",
        isRtl = false,
        otBookRange = 1..39,
        ntBookRange = 40..66
    ),
    ONCV(
        code = "ne_oncv",
        displayName = "Nepali (ONCV)",
        nativeName = "नेपाली (ONCV)",
        translationName = "Old Nepali Contemporary Version (ONCV)",
        licenseNote = "Biblica, Inc. © 1998, 2006, 2021, 2024 • CC BY-SA 4.0 • 66 Books (OT & NT)",
        isRtl = false,
        otBookRange = 1..39,
        ntBookRange = 40..66
    ),
    KOREAN(
        code = "ko",
        displayName = "Korean",
        nativeName = "한국어 (개역한글)",
        translationName = "Korean Revised Version (KRV)",
        licenseNote = "Public Domain (1961) • 66 Books (OT & NT)",
        isRtl = false,
        otBookRange = 1..39,
        ntBookRange = 40..66
    );

    val usesDevanagari: Boolean
        get() = this == NEPALI || this == ONCV

    val shortBadge: String
        get() = when (this) {
            ENGLISH -> "EN"
            NEPALI -> "ULB"
            ONCV -> "ONCV"
            KOREAN -> "KO"
        }

    fun fontFamily(): FontFamily = fontFamily(ScriptureFontStyle.CLASSIC_SERIF)

    fun fontFamily(style: ScriptureFontStyle): FontFamily = when (this) {
        ENGLISH -> style.englishFontFamily()
        NEPALI, ONCV -> NotoSerifDevanagariFamily
        KOREAN -> NotoSerifKrFamily
    }

    fun supportsBook(bookId: Int): Boolean {
        val inOt = otBookRange?.contains(bookId) == true
        val inNt = ntBookRange?.contains(bookId) == true
        return inOt || inNt
    }

    val supportedBookIds: List<Int>
        get() = buildList {
            otBookRange?.forEach { add(it) }
            ntBookRange?.forEach { add(it) }
        }

    companion object {
        fun fromCode(code: String?, defaultLanguage: BibleLanguage = ENGLISH): BibleLanguage =
            entries.firstOrNull { it.code == code } ?: defaultLanguage
    }
}

enum class SystemLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English"
    ),
    NEPALI(
        code = "ne",
        displayName = "Nepali",
        nativeName = "नेपाली"
    );

    companion object {
        fun fromCode(code: String?, defaultLanguage: SystemLanguage = ENGLISH): SystemLanguage =
            entries.firstOrNull { it.code == code } ?: defaultLanguage
    }
}

enum class ThemePreset(
    val displayName: String,
    val subtitle: String,
    val isDarkDefault: Boolean,
    val isGlassDefault: Boolean,
    val defaultBackgroundArgb: Long,
    val defaultSecondaryBgArgb: Long,
    val defaultAccentArgb: Long,
    val defaultTextArgb: Long,
    val defaultTransparency: Float,
    val defaultBlurRadius: Float,
    val defaultButtonStyle: ButtonStyleOption,
    val defaultNavBarStyle: NavBarAppearance,
    val defaultReadingPageBg: ReadingPageBackground
) {
    IOS_LIQUID_GLASS(
        displayName = "iOS Liquid Glass",
        subtitle = "Frosted crystal translucency & luminous depth",
        isDarkDefault = false,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFFE6EFFA,
        defaultSecondaryBgArgb = 0xFFD0E3FA,
        defaultAccentArgb = 0xFF0A84FF,
        defaultTextArgb = 0xFF0F172A,
        defaultTransparency = 0.58f,
        defaultBlurRadius = 22f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    CRYSTAL_CLEAR_LIQUID_GLASS(
        displayName = "Crystal Clear Liquid Glass",
        subtitle = "Ultra-transparent light glass & prism refraction",
        isDarkDefault = false,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFFD9EEFF,
        defaultSecondaryBgArgb = 0xFFEEE8FF,
        defaultAccentArgb = 0xFF0077FF,
        defaultTextArgb = 0xFF091326,
        defaultTransparency = 0.22f,
        defaultBlurRadius = 12f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    CRYSTAL_CLEAR_DARK_GLASS(
        displayName = "Crystal Clear Dark Glass",
        subtitle = "Dark crystal glass with sapphire refraction",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF07111F,
        defaultSecondaryBgArgb = 0xFF102542,
        defaultAccentArgb = 0xFF6AE2FF,
        defaultTextArgb = 0xFFF8FBFF,
        defaultTransparency = 0.20f,
        defaultBlurRadius = 12f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    TRANSPARENT_GLASS(
        displayName = "Transparent Glass",
        subtitle = "Ultra-clear crystal layers & specular edges",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF0D1B2A,
        defaultSecondaryBgArgb = 0xFF1B3A57,
        defaultAccentArgb = 0xFF5AC8FA,
        defaultTextArgb = 0xFFF8FAFC,
        defaultTransparency = 0.32f,
        defaultBlurRadius = 26f,
        defaultButtonStyle = ButtonStyleOption.OUTLINED_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    DARK_GLASS(
        displayName = "Dark Glass",
        subtitle = "Smoked obsidian frost & midnight shimmer",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF12151E,
        defaultSecondaryBgArgb = 0xFF1E2436,
        defaultAccentArgb = 0xFF64D2FF,
        defaultTextArgb = 0xFFF1F5F9,
        defaultTransparency = 0.56f,
        defaultBlurRadius = 20f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.NIGHT_OBSIDIAN
    ),
    LIGHT_GLASS(
        displayName = "Light Glass",
        subtitle = "Airy pearl frost over soft alabaster sky",
        isDarkDefault = false,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFFF4F7FB,
        defaultSecondaryBgArgb = 0xFFE3ECF7,
        defaultAccentArgb = 0xFF007AFF,
        defaultTextArgb = 0xFF1C2430,
        defaultTransparency = 0.72f,
        defaultBlurRadius = 18f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FROSTED_FULL_BAR,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    AMOLED_BLACK(
        displayName = "AMOLED Black",
        subtitle = "True pitch black OLED contrast & subtle frost",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF000000,
        defaultSecondaryBgArgb = 0xFF090B10,
        defaultAccentArgb = 0xFFFFD166,
        defaultTextArgb = 0xFFF5F5F7,
        defaultTransparency = 0.45f,
        defaultBlurRadius = 12f,
        defaultButtonStyle = ButtonStyleOption.OUTLINED_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.AMOLED_BLACK
    ),
    CLASSIC_BIBLE(
        displayName = "Classic Bible",
        subtitle = "Burgundy leather, antique gold & cream paper",
        isDarkDefault = false,
        isGlassDefault = false,
        defaultBackgroundArgb = 0xFFFAF4E6,
        defaultSecondaryBgArgb = 0xFFEDE2C8,
        defaultAccentArgb = 0xFF5B1826,
        defaultTextArgb = 0xFF231B15,
        defaultTransparency = 0.94f,
        defaultBlurRadius = 0f,
        defaultButtonStyle = ButtonStyleOption.CLASSIC_ELEVATED,
        defaultNavBarStyle = NavBarAppearance.SOLID_MINIMAL,
        defaultReadingPageBg = ReadingPageBackground.CREAM_PARCHMENT
    ),
    ELEGANT_BLUE(
        displayName = "Elegant Blue",
        subtitle = "Royal sapphire depth & crisp silver frost",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF0B1F3A,
        defaultSecondaryBgArgb = 0xFF153866,
        defaultAccentArgb = 0xFF4DA3FF,
        defaultTextArgb = 0xFFF0F6FF,
        defaultTransparency = 0.54f,
        defaultBlurRadius = 22f,
        defaultButtonStyle = ButtonStyleOption.PILL_FILLED,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.FROSTED_GLASS
    ),
    NATURE_GREEN(
        displayName = "Nature Green",
        subtitle = "Serene botanical emerald & warm sage calm",
        isDarkDefault = false,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFFEAF4EE,
        defaultSecondaryBgArgb = 0xFFD2E8DA,
        defaultAccentArgb = 0xFF1E7A55,
        defaultTextArgb = 0xFF15291E,
        defaultTransparency = 0.66f,
        defaultBlurRadius = 18f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.CREAM_PARCHMENT
    ),
    GOLDEN_PREMIUM(
        displayName = "Golden Premium",
        subtitle = "Burnished champagne gold & velvet espresso",
        isDarkDefault = true,
        isGlassDefault = true,
        defaultBackgroundArgb = 0xFF1A140E,
        defaultSecondaryBgArgb = 0xFF2F2316,
        defaultAccentArgb = 0xFFD9B25F,
        defaultTextArgb = 0xFFFAF1DF,
        defaultTransparency = 0.58f,
        defaultBlurRadius = 20f,
        defaultButtonStyle = ButtonStyleOption.LIQUID_GLASS,
        defaultNavBarStyle = NavBarAppearance.FLOATING_GLASS_DOCK,
        defaultReadingPageBg = ReadingPageBackground.SEPIA_WARM
    );

    companion object {
        fun fromName(name: String?): ThemePreset =
            entries.firstOrNull { it.name == name } ?: CLASSIC_BIBLE
    }
}

enum class ButtonStyleOption(val label: String) {
    LIQUID_GLASS("Liquid Glass"),
    PILL_FILLED("Filled Pill"),
    OUTLINED_GLASS("Crystal Outline"),
    CLASSIC_ELEVATED("Classic Book");

    companion object {
        fun fromName(name: String?): ButtonStyleOption =
            entries.firstOrNull { it.name == name } ?: LIQUID_GLASS
    }
}

enum class NavBarAppearance(val label: String) {
    FLOATING_GLASS_DOCK("Floating Glass Dock"),
    FROSTED_FULL_BAR("Frosted Glass Bar"),
    SOLID_MINIMAL("Classic Solid Bar");

    companion object {
        fun fromName(name: String?): NavBarAppearance =
            entries.firstOrNull { it.name == name } ?: FLOATING_GLASS_DOCK
    }
}

enum class ScriptureFontStyle(val label: String, val sampleTag: String) {
    CLASSIC_SERIF("Crimson Pro", "Classic Serif"),
    LITERARY_SERIF("Lora", "Literary Serif"),
    BOOK_SERIF("Merriweather", "Editorial Book"),
    APPLE_CLEAN_SANS("Inter", "Clean Apple Sans"),
    MODERN_SANS("Plus Jakarta Sans", "Modern Geometric");

    fun englishFontFamily(): FontFamily = when (this) {
        CLASSIC_SERIF -> CrimsonProFamily
        LITERARY_SERIF -> LoraFamily
        BOOK_SERIF -> MerriweatherFamily
        APPLE_CLEAN_SANS -> InterFamily
        MODERN_SANS -> PlusJakartaSansFamily
    }

    companion object {
        fun fromName(name: String?): ScriptureFontStyle =
            entries.firstOrNull { it.name == name } ?: CLASSIC_SERIF
    }
}

enum class ReadingPageBackground(val label: String) {
    MATCH_THEME("Match Theme"),
    FROSTED_GLASS("Frosted Glass"),
    CREAM_PARCHMENT("Cream Parchment"),
    PURE_WHITE("Crisp White"),
    SEPIA_WARM("Warm Sepia"),
    NIGHT_OBSIDIAN("Night Obsidian"),
    AMOLED_BLACK("AMOLED Black");

    companion object {
        fun fromName(name: String?): ReadingPageBackground =
            entries.firstOrNull { it.name == name } ?: MATCH_THEME
    }
}

data class AppThemeConfig(
    val preset: ThemePreset = ThemePreset.CLASSIC_BIBLE,
    val isDarkSurface: Boolean = preset.isDarkDefault,
    val customBackgroundArgb: Long? = null,
    val customTextArgb: Long? = null,
    val customAccentArgb: Long? = null,
    val transparencyLevel: Float = preset.defaultTransparency,
    val blurIntensity: Float = preset.defaultBlurRadius,
    val buttonStyle: ButtonStyleOption = preset.defaultButtonStyle,
    val navBarAppearance: NavBarAppearance = preset.defaultNavBarStyle,
    val fontStyle: ScriptureFontStyle = ScriptureFontStyle.CLASSIC_SERIF,
    val readingPageBackground: ReadingPageBackground = preset.defaultReadingPageBg
) {
    val effectiveBackgroundArgb: Long get() = customBackgroundArgb ?: preset.defaultBackgroundArgb
    val effectiveTextArgb: Long get() = customTextArgb ?: preset.defaultTextArgb
    val effectiveAccentArgb: Long get() = customAccentArgb ?: preset.defaultAccentArgb

    companion object {
        fun fromPreset(preset: ThemePreset): AppThemeConfig = AppThemeConfig(
            preset = preset,
            isDarkSurface = preset.isDarkDefault,
            customBackgroundArgb = null,
            customTextArgb = null,
            customAccentArgb = null,
            transparencyLevel = preset.defaultTransparency,
            blurIntensity = preset.defaultBlurRadius,
            buttonStyle = preset.defaultButtonStyle,
            navBarAppearance = preset.defaultNavBarStyle,
            fontStyle = ScriptureFontStyle.CLASSIC_SERIF,
            readingPageBackground = preset.defaultReadingPageBg
        )
    }
}

enum class ReadingMode(val label: String, val nepaliLabel: String) {
    BILINGUAL_SPREAD("Book Spread", "पुस्तक पाना"),
    BILINGUAL_PARALLEL("Parallel", "समानान्तर"),
    SINGLE_PRIMARY("Single Primary", "एक भाषा"),
    SINGLE_SECONDARY("Single Secondary", "दोस्रो भाषा")
}

enum class AudioLanguage(val label: String) {
    BILINGUAL("EN + NE"),
    ENGLISH("English"),
    NEPALI("नेपाली (ULB)"),
    ONCV("नेपाली (ONCV)"),
    KOREAN("한국어")
}

enum class HighlightColor(val key: String, val label: String) {
    NONE("none", "Clear"),
    GOLD("gold", "Gold"),
    ROSE("rose", "Rose"),
    SAGE("sage", "Sage"),
    SKY("sky", "Sky");

    companion object {
        fun fromKey(key: String?): HighlightColor =
            entries.firstOrNull { it.key == key } ?: NONE
    }
}

enum class DownloadStatus {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    PAUSED,
    DOWNLOADED
}

data class BookDownloadState(
    val languageCode: String,
    val bookId: Int,
    val status: DownloadStatus,
    val bytesSize: Long
)

data class LanguageDownloadSummary(
    val language: BibleLanguage,
    val totalBooks: Int,
    val downloadedBooks: Int,
    val queuedOrActiveBooks: Int,
    val isDownloading: Boolean,
    val isPaused: Boolean,
    val totalBytesOnDisk: Long
) {
    val progressFraction: Float
        get() = if (totalBooks == 0) 0f else downloadedBooks.toFloat() / totalBooks.toFloat()

    val isFullyDownloaded: Boolean
        get() = downloadedBooks >= totalBooks && totalBooks > 0
}

data class BibleBook(
    val id: Int,
    val testament: String, // "OT" or "NT"
    val abbrev: String,
    val nameEn: String,
    val nameNe: String,
    val nameKo: String = "",
    val chapterCount: Int
) {
    val isOldTestament: Boolean get() = testament == "OT"

    fun nameFor(language: BibleLanguage): String = when (language) {
        BibleLanguage.ENGLISH -> nameEn
        BibleLanguage.NEPALI, BibleLanguage.ONCV -> nameNe
        BibleLanguage.KOREAN -> nameKo.ifBlank { nameEn }
    }
}

data class BibleVerse(
    val id: Int,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val textEn: String = "",
    val textNe: String = "",
    val textOncv: String = "",
    val textKo: String = ""
) {
    val key: String get() = "$bookId:$chapter:$verse"

    fun textFor(language: BibleLanguage): String = when (language) {
        BibleLanguage.ENGLISH -> textEn
        BibleLanguage.NEPALI -> textNe
        BibleLanguage.ONCV -> textOncv
        BibleLanguage.KOREAN -> textKo
    }
}

data class SearchResult(
    val bookId: Int,
    val bookNameEn: String,
    val bookNameNe: String,
    val testament: String,
    val chapter: Int,
    val verse: Int,
    val matchedLanguage: BibleLanguage,
    val textEn: String,
    val textNe: String,
    val textOncv: String = "",
    val textKo: String
)

data class DailyVerseData(
    val book: BibleBook,
    val verse: BibleVerse
)

enum class PageTurnSoundStyle(
    val displayName: String,
    val description: String,
    val isAudible: Boolean = true
) {
    REALISTIC_BIBLE(
        displayName = "Realistic Bible Page",
        description = "Crisp thin India-paper curl & gentle settle"
    ),
    SOFT_PAPER(
        displayName = "Soft Paper Flip",
        description = "Gentle brushed cream paper turn"
    ),
    OLD_VINTAGE_BIBLE(
        displayName = "Old & Vintage Bible",
        description = "Warm fibrous aged Bible leaf texture"
    ),
    GENTLE_RUSTLE(
        displayName = "Gentle Paper Rustling",
        description = "Airy, peaceful dual-wave leaf rustle"
    ),
    CLASSIC_HARDCOVER(
        displayName = "Classic Hardcover Book",
        description = "Traditional bound book flip with subtle weight"
    ),
    FAST_FLIP(
        displayName = "Fast Page Flip",
        description = "Brisk light flick for quick browsing"
    ),
    SLOW_SOFT(
        displayName = "Slow & Soft Turn",
        description = "Unhurried, meditative page glide"
    ),
    ANTIQUE_PARCHMENT(
        displayName = "Thick & Antique Parchment",
        description = "Richer parchment grain & heavier page settle"
    ),
    NATURAL_MOVEMENT(
        displayName = "Subtle Paper Movement",
        description = "Whisper-quiet minimal paper touch"
    ),
    SOFT_DIGITAL(
        displayName = "Soft Digital Page",
        description = "Feather-light modern satin breeze"
    ),
    RANDOM(
        displayName = "Random Sound Mode",
        description = "Automatically varies paper sounds each turn"
    ),
    SILENT(
        displayName = "Silent Mode (No Sound)",
        description = "Complete silence while flipping pages",
        isAudible = false
    );

    companion object {
        val audibleStyles: List<PageTurnSoundStyle> =
            entries.filter { it.isAudible && it != RANDOM }

        fun fromName(name: String?): PageTurnSoundStyle =
            entries.firstOrNull { it.name == name } ?: REALISTIC_BIBLE
    }
}

enum class CopyrightLicenseCategory(val key: String) {
    OPEN_LICENSE("open_license"),
    PUBLIC_DOMAIN("public_domain"),
    COPYRIGHTED("copyrighted"),
    UNVERIFIED("unverified");

    companion object {
        fun fromKey(key: String?): CopyrightLicenseCategory =
            entries.firstOrNull { it.key == key } ?: UNVERIFIED
    }
}

data class BiblePreferencesState(
    val darkMode: Boolean = false,
    val fontSizeSp: Float = 16.5f,
    val pageSoundEnabled: Boolean = true,
    val pageSoundStyle: PageTurnSoundStyle = PageTurnSoundStyle.REALISTIC_BIBLE,
    val pageSoundVolume: Float = 0.72f,
    val readingMode: ReadingMode = ReadingMode.BILINGUAL_SPREAD,
    val primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
    val secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI,
    val audioLanguage: AudioLanguage = AudioLanguage.BILINGUAL,
    val audioSpeed: Float = 1.0f,
    val lastBookId: Int = 1,
    val lastChapter: Int = 1,
    val lastVerse: Int = 1,
    val themeConfig: AppThemeConfig = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE),
    val systemLanguage: SystemLanguage = SystemLanguage.ENGLISH
)

