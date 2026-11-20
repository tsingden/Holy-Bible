package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeConfig
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleLanguage
import com.example.data.model.BiblePreferencesState
import com.example.data.model.ButtonStyleOption
import com.example.data.model.NavBarAppearance
import com.example.data.model.PageTurnSoundStyle
import com.example.data.model.ReadingMode
import com.example.data.model.ReadingPageBackground
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.SystemLanguage
import com.example.data.model.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.bibleDataStore: DataStore<Preferences> by preferencesDataStore(name = "bible_settings")

class BiblePreferences(private val context: Context) {

    val stateFlow: Flow<BiblePreferencesState> = context.bibleDataStore.data.map { prefs ->
        val rawMode = prefs[KEY_READING_MODE]
        val mode = when (rawMode) {
            "ENGLISH_ONLY" -> ReadingMode.SINGLE_PRIMARY
            "NEPALI_ONLY" -> ReadingMode.SINGLE_SECONDARY
            else -> ReadingMode.entries.firstOrNull { it.name == rawMode } ?: ReadingMode.BILINGUAL_SPREAD
        }

        val preset = ThemePreset.fromName(prefs[KEY_THEME_PRESET])
        val isDark = prefs[KEY_THEME_IS_DARK] ?: prefs[KEY_DARK_MODE] ?: preset.isDarkDefault

        val themeConfig = AppThemeConfig(
            preset = preset,
            isDarkSurface = isDark,
            customBackgroundArgb = prefs[KEY_THEME_CUSTOM_BG],
            customTextArgb = prefs[KEY_THEME_CUSTOM_TEXT],
            customAccentArgb = prefs[KEY_THEME_CUSTOM_ACCENT],
            transparencyLevel = prefs[KEY_THEME_TRANSPARENCY] ?: preset.defaultTransparency,
            blurIntensity = prefs[KEY_THEME_BLUR] ?: preset.defaultBlurRadius,
            buttonStyle = prefs[KEY_THEME_BUTTON_STYLE]?.let { ButtonStyleOption.fromName(it) }
                ?: preset.defaultButtonStyle,
            navBarAppearance = prefs[KEY_THEME_NAV_STYLE]?.let { NavBarAppearance.fromName(it) }
                ?: preset.defaultNavBarStyle,
            fontStyle = prefs[KEY_THEME_FONT_STYLE]?.let { ScriptureFontStyle.fromName(it) }
                ?: ScriptureFontStyle.CLASSIC_SERIF,
            readingPageBackground = prefs[KEY_THEME_READING_BG]?.let { ReadingPageBackground.fromName(it) }
                ?: preset.defaultReadingPageBg
        )

        BiblePreferencesState(
            darkMode = isDark,
            fontSizeSp = prefs[KEY_FONT_SIZE] ?: 16.5f,
            pageSoundEnabled = prefs[KEY_PAGE_SOUND] ?: true,
            pageSoundStyle = PageTurnSoundStyle.fromName(prefs[KEY_PAGE_SOUND_STYLE]),
            pageSoundVolume = (prefs[KEY_PAGE_SOUND_VOLUME] ?: 0.72f).coerceIn(0f, 1f),
            readingMode = mode,
            primaryLanguage = BibleLanguage.fromCode(prefs[KEY_PRIMARY_LANG], BibleLanguage.ENGLISH),
            secondaryLanguage = BibleLanguage.fromCode(prefs[KEY_SECONDARY_LANG], BibleLanguage.NEPALI),
            audioLanguage = prefs[KEY_AUDIO_LANG]?.let { name ->
                AudioLanguage.entries.firstOrNull { it.name == name }
            } ?: AudioLanguage.BILINGUAL,
            audioSpeed = prefs[KEY_AUDIO_SPEED] ?: 1.0f,
            lastBookId = prefs[KEY_LAST_BOOK_ID] ?: 1,
            lastChapter = prefs[KEY_LAST_CHAPTER] ?: 1,
            lastVerse = prefs[KEY_LAST_VERSE] ?: 1,
            themeConfig = themeConfig,
            systemLanguage = SystemLanguage.fromCode(prefs[KEY_SYSTEM_LANGUAGE], SystemLanguage.ENGLISH)
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.bibleDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
            prefs[KEY_THEME_IS_DARK] = enabled
            val currentPreset = ThemePreset.fromName(prefs[KEY_THEME_PRESET])
            val nextPreset = when {
                enabled && currentPreset == ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS ->
                    ThemePreset.CRYSTAL_CLEAR_DARK_GLASS
                !enabled && currentPreset == ThemePreset.CRYSTAL_CLEAR_DARK_GLASS ->
                    ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS
                enabled && (currentPreset == ThemePreset.IOS_LIQUID_GLASS || currentPreset == ThemePreset.LIGHT_GLASS) ->
                    ThemePreset.DARK_GLASS
                !enabled && currentPreset == ThemePreset.DARK_GLASS ->
                    ThemePreset.IOS_LIQUID_GLASS
                else -> currentPreset
            }
            if (nextPreset != currentPreset) {
                prefs[KEY_THEME_PRESET] = nextPreset.name
                prefs.remove(KEY_THEME_CUSTOM_BG)
                prefs.remove(KEY_THEME_CUSTOM_TEXT)
                prefs.remove(KEY_THEME_CUSTOM_ACCENT)
                prefs[KEY_THEME_TRANSPARENCY] = nextPreset.defaultTransparency
                prefs[KEY_THEME_BLUR] = nextPreset.defaultBlurRadius
                prefs[KEY_THEME_BUTTON_STYLE] = nextPreset.defaultButtonStyle.name
                prefs[KEY_THEME_NAV_STYLE] = nextPreset.defaultNavBarStyle.name
                prefs[KEY_THEME_READING_BG] = nextPreset.defaultReadingPageBg.name
            } else if (enabled && prefs[KEY_THEME_READING_BG] == ReadingPageBackground.CREAM_PARCHMENT.name) {
                prefs[KEY_THEME_READING_BG] = ReadingPageBackground.NIGHT_OBSIDIAN.name
            } else if (!enabled && prefs[KEY_THEME_READING_BG] == ReadingPageBackground.NIGHT_OBSIDIAN.name) {
                prefs[KEY_THEME_READING_BG] = ReadingPageBackground.CREAM_PARCHMENT.name
            }
        }
    }

    suspend fun saveThemeConfig(config: AppThemeConfig) {
        context.bibleDataStore.edit { prefs ->
            prefs[KEY_THEME_PRESET] = config.preset.name
            prefs[KEY_THEME_IS_DARK] = config.isDarkSurface
            prefs[KEY_DARK_MODE] = config.isDarkSurface

            if (config.customBackgroundArgb != null) {
                prefs[KEY_THEME_CUSTOM_BG] = config.customBackgroundArgb
            } else {
                prefs.remove(KEY_THEME_CUSTOM_BG)
            }

            if (config.customTextArgb != null) {
                prefs[KEY_THEME_CUSTOM_TEXT] = config.customTextArgb
            } else {
                prefs.remove(KEY_THEME_CUSTOM_TEXT)
            }

            if (config.customAccentArgb != null) {
                prefs[KEY_THEME_CUSTOM_ACCENT] = config.customAccentArgb
            } else {
                prefs.remove(KEY_THEME_CUSTOM_ACCENT)
            }

            prefs[KEY_THEME_TRANSPARENCY] = config.transparencyLevel.coerceIn(0.15f, 0.96f)
            prefs[KEY_THEME_BLUR] = config.blurIntensity.coerceIn(0f, 32f)
            prefs[KEY_THEME_BUTTON_STYLE] = config.buttonStyle.name
            prefs[KEY_THEME_NAV_STYLE] = config.navBarAppearance.name
            prefs[KEY_THEME_FONT_STYLE] = config.fontStyle.name
            prefs[KEY_THEME_READING_BG] = config.readingPageBackground.name
        }
    }

    suspend fun setFontSize(sp: Float) {
        context.bibleDataStore.edit { it[KEY_FONT_SIZE] = sp.coerceIn(13f, 24f) }
    }

    suspend fun setPageSoundEnabled(enabled: Boolean) {
        context.bibleDataStore.edit { prefs ->
            prefs[KEY_PAGE_SOUND] = enabled
            if (enabled && prefs[KEY_PAGE_SOUND_STYLE] == PageTurnSoundStyle.SILENT.name) {
                prefs[KEY_PAGE_SOUND_STYLE] = PageTurnSoundStyle.REALISTIC_BIBLE.name
            }
        }
    }

    suspend fun setPageSoundStyle(style: PageTurnSoundStyle) {
        context.bibleDataStore.edit { prefs ->
            prefs[KEY_PAGE_SOUND_STYLE] = style.name
            if (style != PageTurnSoundStyle.SILENT) {
                prefs[KEY_PAGE_SOUND] = true
            }
        }
    }

    suspend fun setPageSoundVolume(volume: Float) {
        context.bibleDataStore.edit { prefs ->
            prefs[KEY_PAGE_SOUND_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun setReadingMode(mode: ReadingMode) {
        context.bibleDataStore.edit { it[KEY_READING_MODE] = mode.name }
    }

    suspend fun setPrimaryLanguage(lang: BibleLanguage) {
        context.bibleDataStore.edit { it[KEY_PRIMARY_LANG] = lang.code }
    }

    suspend fun setSecondaryLanguage(lang: BibleLanguage) {
        context.bibleDataStore.edit { it[KEY_SECONDARY_LANG] = lang.code }
    }

    suspend fun swapLanguages() {
        context.bibleDataStore.edit { prefs ->
            val p = prefs[KEY_PRIMARY_LANG] ?: BibleLanguage.ENGLISH.code
            val s = prefs[KEY_SECONDARY_LANG] ?: BibleLanguage.NEPALI.code
            prefs[KEY_PRIMARY_LANG] = s
            prefs[KEY_SECONDARY_LANG] = p
        }
    }

    suspend fun setAudioLanguage(lang: AudioLanguage) {
        context.bibleDataStore.edit { it[KEY_AUDIO_LANG] = lang.name }
    }

    suspend fun setAudioSpeed(speed: Float) {
        context.bibleDataStore.edit { it[KEY_AUDIO_SPEED] = speed.coerceIn(0.6f, 1.6f) }
    }

    suspend fun setSystemLanguage(language: SystemLanguage) {
        context.bibleDataStore.edit { it[KEY_SYSTEM_LANGUAGE] = language.code }
    }

    suspend fun saveLastRead(bookId: Int, chapter: Int, verse: Int) {
        context.bibleDataStore.edit {
            it[KEY_LAST_BOOK_ID] = bookId
            it[KEY_LAST_CHAPTER] = chapter
            it[KEY_LAST_VERSE] = verse
        }
    }

    suspend fun isOncvDefaultSeeded(): Boolean {
        return context.bibleDataStore.data.first()[KEY_ONCV_DEFAULT_SEEDED] == true
    }

    suspend fun markOncvDefaultSeeded() {
        context.bibleDataStore.edit {
            it[KEY_ONCV_DEFAULT_SEEDED] = true
        }
    }

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_FONT_SIZE = floatPreferencesKey("font_size_sp")
        private val KEY_PAGE_SOUND = booleanPreferencesKey("page_sound_enabled")
        private val KEY_PAGE_SOUND_STYLE = stringPreferencesKey("page_sound_style")
        private val KEY_PAGE_SOUND_VOLUME = floatPreferencesKey("page_sound_volume")
        private val KEY_READING_MODE = stringPreferencesKey("reading_mode")
        private val KEY_PRIMARY_LANG = stringPreferencesKey("primary_language")
        private val KEY_SECONDARY_LANG = stringPreferencesKey("secondary_language")
        private val KEY_AUDIO_LANG = stringPreferencesKey("audio_language")
        private val KEY_AUDIO_SPEED = floatPreferencesKey("audio_speed")
        private val KEY_LAST_BOOK_ID = intPreferencesKey("last_book_id")
        private val KEY_LAST_CHAPTER = intPreferencesKey("last_chapter")
        private val KEY_LAST_VERSE = intPreferencesKey("last_verse")
        private val KEY_SYSTEM_LANGUAGE = stringPreferencesKey("system_language")
        private val KEY_ONCV_DEFAULT_SEEDED = booleanPreferencesKey("oncv_default_seeded")

        // Liquid Glass & Custom Theme Keys
        private val KEY_THEME_PRESET = stringPreferencesKey("theme_preset")
        private val KEY_THEME_IS_DARK = booleanPreferencesKey("theme_is_dark")
        private val KEY_THEME_CUSTOM_BG = longPreferencesKey("theme_custom_bg")
        private val KEY_THEME_CUSTOM_TEXT = longPreferencesKey("theme_custom_text")
        private val KEY_THEME_CUSTOM_ACCENT = longPreferencesKey("theme_custom_accent")
        private val KEY_THEME_TRANSPARENCY = floatPreferencesKey("theme_transparency")
        private val KEY_THEME_BLUR = floatPreferencesKey("theme_blur")
        private val KEY_THEME_BUTTON_STYLE = stringPreferencesKey("theme_button_style")
        private val KEY_THEME_NAV_STYLE = stringPreferencesKey("theme_nav_style")
        private val KEY_THEME_FONT_STYLE = stringPreferencesKey("theme_font_style")
        private val KEY_THEME_READING_BG = stringPreferencesKey("theme_reading_bg")
    }
}
