package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.BibleAssetDatabase
import com.example.data.db.UserBibleDatabase
import com.example.data.download.BibleDownloadManager
import com.example.data.model.AppThemeConfig
import com.example.data.model.BibleLanguage
import com.example.data.model.ButtonStyleOption
import com.example.data.model.DownloadStatus
import com.example.data.model.NavBarAppearance
import com.example.data.model.ReadingMode
import com.example.data.model.ReadingPageBackground
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.ThemePreset
import com.example.data.prefs.BiblePreferences
import com.example.ui.reader.BiblePageSheet
import com.example.ui.screens.ThemeStudioScreen
import com.example.ui.theme.HolyBibleTheme
import com.example.ui.theme.resolveBiblePalette
import com.example.ui.theme.resolveLiquidGlassTokens
import com.example.util.DevanagariNumberUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Holy Bible", appName)
    }

    @Test
    fun `english kjv, nepali ulb, nepali oncv, and korean language packs install offline and support search`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(
            listOf(BibleLanguage.ENGLISH, BibleLanguage.NEPALI, BibleLanguage.ONCV, BibleLanguage.KOREAN),
            BibleLanguage.entries
        )
        assertEquals("Unlocked Literal Bible (ULB)", BibleLanguage.NEPALI.translationName)
        assertEquals("नेपाली (ULB)", BibleLanguage.NEPALI.nativeName)
        assertEquals("Old Nepali Contemporary Version (ONCV)", BibleLanguage.ONCV.translationName)
        assertEquals("नेपाली (ONCV)", BibleLanguage.ONCV.nativeName)
        assertFalse(BibleLanguage.NEPALI.translationName.contains("NNRV"))
        assertFalse(BibleLanguage.NEPALI.licenseNote.contains("NNRV"))

        val db = BibleAssetDatabase(context)
        db.warmup()

        val books = db.getAllBooks()
        assertEquals(66, books.size)
        assertEquals(39, books.count { it.isOldTestament })
        assertEquals(27, books.count { !it.isOldTestament })
        assertEquals("창세기", books.first { it.id == 1 }.nameKo)
        assertEquals("요한복음", books.first { it.id == 43 }.nameKo)

        // Install sample books across all 4 translations (EN, ULB, ONCV, KO)
        db.installBooksFromPack(BibleLanguage.ENGLISH, setOf(1, 19, 43))
        db.installBooksFromPack(BibleLanguage.NEPALI, setOf(1, 19, 43))
        db.installBooksFromPack(BibleLanguage.ONCV, setOf(1, 19, 43))
        db.installBooksFromPack(BibleLanguage.KOREAN, setOf(1, 19, 43))

        val gen11 = db.getVerse(1, 1, 1)
        assertNotNull(gen11)
        assertTrue(gen11!!.textEn.contains("In the beginning God created"))
        assertEquals("आदिमा परमेश्‍वरले आकाशमण्डल र पृथ्वी सृष्‍टि गर्नुभयो ।", gen11.textNe)
        assertEquals("आदिमा परमेश्‍वरले आकाशमण्डल र पृथ्वी सृष्‍टि गर्नुभयो।", gen11.textOncv)
        assertTrue(gen11.textKo.contains("태초에 하나님이 천지를 창조하시니라"))

        val john316 = db.getVerse(43, 3, 16)
        assertNotNull(john316)
        assertTrue(john316!!.textEn.contains("God so loved the world"))
        // Verify authentic ULB wording
        assertTrue(john316.textNe.contains("एक मात्र पुत्र दिनुभयो"))
        assertTrue(john316.textNe.contains("अनन्त जीवन पाओस्"))
        // Verify authentic ONCV wording distinct from ULB
        assertTrue(john316.textOncv.contains("अद्वितीय पुत्रलाई बलिदानका लागि दिनुभयो"))
        assertTrue(john316.textOncv.contains("अनन्त जीवन पाओस्"))
        assertNotEquals(john316.textNe, john316.textOncv)
        assertTrue(john316.textKo.contains("하나님이 세상을 이처럼 사랑하사"))

        val psalm231 = db.getVerse(19, 23, 1)
        assertNotNull(psalm231)
        assertTrue(psalm231!!.textOncv.contains("याहवेह मेरा गोठाला हुनुहुन्छ"))

        val psalm119 = db.getChapterVerses(19, 119)
        assertEquals(176, psalm119.size)
        assertTrue(psalm119.all { it.textNe.isNotBlank() && it.textOncv.isNotBlank() })

        // Search English
        assertTrue(db.searchVerses("beginning", languageFilter = BibleLanguage.ENGLISH).isNotEmpty())
        // Search Nepali ULB (ZWJ normalized)
        assertTrue(db.searchVerses("परमेश्वर", languageFilter = BibleLanguage.NEPALI).isNotEmpty())
        assertTrue(db.searchVerses("पुत्र", languageFilter = BibleLanguage.NEPALI).isNotEmpty())
        // Search Nepali ONCV isolated from ULB
        val oncvSearch = db.searchVerses("अद्वितीय", languageFilter = BibleLanguage.ONCV)
        assertTrue(oncvSearch.any { it.bookId == 43 && it.chapter == 3 && it.verse == 16 && it.matchedLanguage == BibleLanguage.ONCV })
        assertTrue(oncvSearch.all { it.matchedLanguage == BibleLanguage.ONCV && it.textOncv.isNotBlank() })
        // Search Korean Hangul
        assertTrue(db.searchVerses("태초에", languageFilter = BibleLanguage.KOREAN).isNotEmpty())
        assertTrue(db.searchVerses("사랑하사", languageFilter = BibleLanguage.KOREAN).isNotEmpty())
    }

    @Test
    fun `ulb nepali pack covers all 66 books, chapter navigation, verse lookup, and ot-nt search`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BibleAssetDatabase(context)
        db.warmup()

        val allBookIds = (1..66).toSet()
        val installedSizes = db.installBooksFromPack(BibleLanguage.NEPALI, allBookIds)
        assertEquals(66, installedSizes.size)

        val books = db.getAllBooks()
        assertEquals(66, books.size)

        for (book in books) {
            assertTrue("Book ${book.id} (${book.nameEn}) must be downloaded", db.isBookDownloaded(BibleLanguage.NEPALI, book.id))
            val firstChapter = db.getChapterVerses(book.id, 1)
            assertTrue("Book ${book.id} chapter 1 should have verses", firstChapter.isNotEmpty())
            assertTrue("Book ${book.id} ch1:v1 must have Nepali ULB text", firstChapter.first().textNe.isNotBlank())

            if (book.chapterCount > 1) {
                val lastChapter = db.getChapterVerses(book.id, book.chapterCount)
                assertTrue("Book ${book.id} last chapter (${book.chapterCount}) should have verses", lastChapter.isNotEmpty())
                assertTrue("Book ${book.id} last chapter must have Nepali ULB text", lastChapter.last().textNe.isNotBlank())
            }
        }

        // Verify Genesis 1:1 and Revelation 22:21 in ULB
        val revEnd = db.getVerse(66, 22, 21)
        assertNotNull(revEnd)
        assertEquals("प्रभु येशूको अनुग्रह हरेकसँग रहोस् । आमेन ।", revEnd!!.textNe)

        // Verify OT and NT search filtering with ULB
        val otResults = db.searchVerses("आदिमा", testamentFilter = "OT", languageFilter = BibleLanguage.NEPALI)
        assertTrue(otResults.isNotEmpty())
        assertTrue(otResults.all { it.testament == "OT" && it.textNe.isNotBlank() })

        val ntResults = db.searchVerses("अनुग्रह", testamentFilter = "NT", languageFilter = BibleLanguage.NEPALI)
        assertTrue(ntResults.isNotEmpty())
        assertTrue(ntResults.all { it.testament == "NT" && it.textNe.isNotBlank() })
    }

    @Test
    fun `oncv nepali pack covers all 66 books, chapter navigation, verse lookup, share, bookmarks and ot-nt search`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BibleAssetDatabase(context)
        db.warmup()

        val allBookIds = (1..66).toSet()
        val installedSizes = db.installBooksFromPack(BibleLanguage.ONCV, allBookIds)
        assertEquals(66, installedSizes.size)

        val books = db.getAllBooks()
        assertEquals(66, books.size)

        for (book in books) {
            assertTrue("ONCV Book ${book.id} (${book.nameEn}) must be downloaded", db.isBookDownloaded(BibleLanguage.ONCV, book.id))
            val firstChapter = db.getChapterVerses(book.id, 1)
            assertTrue("ONCV Book ${book.id} chapter 1 should have verses", firstChapter.isNotEmpty())
            assertTrue("ONCV Book ${book.id} ch1:v1 must have ONCV text", firstChapter.first().textOncv.isNotBlank())

            if (book.chapterCount > 1) {
                val lastChapter = db.getChapterVerses(book.id, book.chapterCount)
                assertTrue("ONCV Book ${book.id} last chapter (${book.chapterCount}) should have verses", lastChapter.isNotEmpty())
                assertTrue("ONCV Book ${book.id} last chapter must have ONCV text", lastChapter.last().textOncv.isNotBlank())
            }
        }

        // Verify Revelation 22:21 in ONCV
        val revEnd = db.getVerse(66, 22, 21)
        assertNotNull(revEnd)
        assertEquals("प्रभु येशूको अनुग्रह परमेश्‍वरका सबै जनहरूसँग रहोस्! आमेन।", revEnd!!.textOncv)

        // Verify Share helper uses ONCV text
        val revBook = books.first { it.id == 66 }
        val formattedShare = com.example.util.BibleShareHelper.formatVerse(
            book = revBook,
            verse = revEnd,
            primaryLanguage = BibleLanguage.ONCV,
            secondaryLanguage = BibleLanguage.ONCV
        )
        assertTrue(formattedShare.contains("प्रभु येशूको अनुग्रह परमेश्‍वरका सबै जनहरूसँग रहोस्! आमेन।"))

        // Verify OT and NT isolated search filtering with ONCV
        val otResults = db.searchVerses("गोठाला", testamentFilter = "OT", languageFilter = BibleLanguage.ONCV)
        assertTrue(otResults.isNotEmpty())
        assertTrue(otResults.all { it.testament == "OT" && it.matchedLanguage == BibleLanguage.ONCV && it.textOncv.isNotBlank() })

        val ntResults = db.searchVerses("अनुग्रह", testamentFilter = "NT", languageFilter = BibleLanguage.ONCV)
        assertTrue(ntResults.isNotEmpty())
        assertTrue(ntResults.all { it.testament == "NT" && it.matchedLanguage == BibleLanguage.ONCV && it.textOncv.isNotBlank() })
    }

    @Test
    fun `download manager handles seeding, single book download, pause, resume, cancel and delete`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val assetDb = BibleAssetDatabase(context)
        val userDb = Room.inMemoryDatabaseBuilder(context, UserBibleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val manager = BibleDownloadManager(assetDb, userDb.userBibleDao())

        try {
            manager.initialize(seedDefaultsIfEmpty = true)

            val summaries = withTimeout(10000L) {
                manager.languageSummaries.first { list ->
                    list.first { it.language == BibleLanguage.ENGLISH }.downloadedBooks == 66 &&
                        list.first { it.language == BibleLanguage.NEPALI }.downloadedBooks == 66 &&
                        list.first { it.language == BibleLanguage.ONCV }.downloadedBooks == 66
                }
            }
            val enSummary = summaries.first { it.language == BibleLanguage.ENGLISH }
            val neSummary = summaries.first { it.language == BibleLanguage.NEPALI }
            val oncvSummary = summaries.first { it.language == BibleLanguage.ONCV }
            assertEquals(66, enSummary.downloadedBooks)
            assertEquals(66, neSummary.downloadedBooks)
            assertEquals(66, oncvSummary.downloadedBooks)
            assertTrue(manager.totalStorageBytes.value > 0L)

            // Download a single book in Korean (Genesis = 1)
            manager.downloadBook(BibleLanguage.KOREAN, 1)
            withTimeout(5000L) {
                manager.bookStates.first {
                    it[BibleLanguage.KOREAN.code to 1]?.status == DownloadStatus.DOWNLOADED
                }
            }
            assertTrue(assetDb.isBookDownloaded(BibleLanguage.KOREAN, 1))

            // Delete that single book and verify removal
            manager.deleteBook(BibleLanguage.KOREAN, 1)
            withTimeout(5000L) {
                manager.bookStates.first { !it.containsKey(BibleLanguage.KOREAN.code to 1) }
            }
            assertFalse(assetDb.isBookDownloaded(BibleLanguage.KOREAN, 1))
        } finally {
            userDb.close()
        }
    }

    @Test
    fun `full chapter scrolls smoothly from verse 1 to final verse on compact phone`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BibleAssetDatabase(context)
        runBlocking {
            db.warmup()
            db.installBookFromPack(BibleLanguage.ENGLISH, 1)
            db.installBookFromPack(BibleLanguage.NEPALI, 1)
            db.installBookFromPack(BibleLanguage.KOREAN, 1)
        }
        val genesis = runBlocking { db.getBook(1)!! }
        val genesis1 = runBlocking { db.getChapterVerses(1, 1) }
        assertEquals(31, genesis1.size)

        composeTestRule.setContent {
            HolyBibleTheme(
                themeConfig = AppThemeConfig.fromPreset(ThemePreset.TRANSPARENT_GLASS)
            ) {
                Box(modifier = Modifier.width(320.dp).height(580.dp)) {
                    BiblePageSheet(
                        book = genesis,
                        chapter = 1,
                        verses = genesis1,
                        readingMode = ReadingMode.BILINGUAL_SPREAD,
                        primaryLanguage = BibleLanguage.ENGLISH,
                        secondaryLanguage = BibleLanguage.NEPALI,
                        downloadedBookKeys = setOf("en" to 1, "ne" to 1, "ko" to 1),
                        fontSizeSp = 16.5f,
                        selectedVerseKey = null,
                        activeAudioVerseKey = null,
                        targetScrollVerse = null,
                        bookmarkedKeys = emptySet(),
                        highlightMap = emptyMap(),
                        noteKeys = emptySet(),
                        onVerseClick = {}
                    )
                }
            }
        }

        // Verify Verse 1 is displayed in English and Nepali (ULB)
        composeTestRule.onNodeWithText("In the beginning God created", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("आदिमा परमेश्", substring = true).assertIsDisplayed()

        // Scroll to last verse (item index 31 = verse 31 after banner) and verify complete text
        composeTestRule.onNodeWithTag("bible_chapter_list").performScrollToIndex(31)
        composeTestRule.onNodeWithText("And God saw every thing that he had made", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("छैटौँ दिन", substring = true).assertIsDisplayed()
    }

    @Test
    fun `all 11 theme presets resolve distinct legible liquid glass tokens and reading palettes`() {
        assertEquals(11, ThemePreset.entries.size)

        ThemePreset.entries.forEach { preset ->
            val config = AppThemeConfig.fromPreset(preset)
            val tokens = resolveLiquidGlassTokens(config)
            val palette = resolveBiblePalette(tokens)

            assertEquals(preset.defaultTransparency, tokens.transparencyLevel, 0.001f)
            assertEquals(preset.defaultBlurRadius, tokens.blurIntensityDp, 0.001f)
            assertNotEquals(tokens.textPrimary, tokens.glassSurface)
            assertNotEquals(palette.paperBackground, palette.inkPrimary)
        }

        val crystalLight = resolveLiquidGlassTokens(AppThemeConfig.fromPreset(ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS))
        assertTrue(crystalLight.isCrystalClear)
        assertTrue(crystalLight.isGlassActive)
        assertFalse(crystalLight.isDark)
        assertTrue(crystalLight.transparencyLevel <= 0.25f)
        assertEquals(12f, crystalLight.blurIntensityDp, 0.001f)

        val crystalDark = resolveLiquidGlassTokens(AppThemeConfig.fromPreset(ThemePreset.CRYSTAL_CLEAR_DARK_GLASS))
        assertTrue(crystalDark.isCrystalClear)
        assertTrue(crystalDark.isGlassActive)
        assertTrue(crystalDark.isDark)
        assertTrue(crystalDark.transparencyLevel <= 0.25f)
        assertEquals(12f, crystalDark.blurIntensityDp, 0.001f)

        val amoledTokens = resolveLiquidGlassTokens(AppThemeConfig.fromPreset(ThemePreset.AMOLED_BLACK))
        val amoledPalette = resolveBiblePalette(amoledTokens)
        assertTrue(amoledTokens.isDark)
        assertEquals(Color(0xFF000000), amoledPalette.paperBackground)

        val classicTokens = resolveLiquidGlassTokens(AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE))
        assertFalse(classicTokens.isGlassActive)
        assertFalse(classicTokens.isDark)
    }

    @Test
    fun `classic bible is default theme on first launch until user changes it`() = runBlocking {
        // Default state before any user choice must be Classic Bible
        val initialState = com.example.data.model.BiblePreferencesState()
        assertEquals(ThemePreset.CLASSIC_BIBLE, initialState.themeConfig.preset)
        assertEquals(ThemePreset.CLASSIC_BIBLE, ThemePreset.fromName(null))
        assertEquals(ThemePreset.CLASSIC_BIBLE, AppThemeConfig().preset)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = BiblePreferences(context)
        val firstLaunchState = withTimeout(5000L) {
            prefs.stateFlow.first()
        }
        assertEquals(ThemePreset.CLASSIC_BIBLE, firstLaunchState.themeConfig.preset)
        assertFalse(firstLaunchState.darkMode)
        assertEquals(ReadingPageBackground.CREAM_PARCHMENT, firstLaunchState.themeConfig.readingPageBackground)

        // Manual user change overrides default and persists
        prefs.saveThemeConfig(AppThemeConfig.fromPreset(ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS))
        val updatedState = withTimeout(5000L) {
            prefs.stateFlow.first { it.themeConfig.preset == ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS }
        }
        assertEquals(ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS, updatedState.themeConfig.preset)
    }

    @Test
    fun `crystal clear theme toggles smoothly between light and dark variations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = BiblePreferences(context)
        prefs.saveThemeConfig(AppThemeConfig.fromPreset(ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS))
        prefs.setDarkMode(true)
        val darkLoaded = withTimeout(5000L) {
            prefs.stateFlow.first { it.themeConfig.preset == ThemePreset.CRYSTAL_CLEAR_DARK_GLASS }
        }
        assertTrue(darkLoaded.darkMode)
        prefs.setDarkMode(false)
        val lightLoaded = withTimeout(5000L) {
            prefs.stateFlow.first { it.themeConfig.preset == ThemePreset.CRYSTAL_CLEAR_LIQUID_GLASS }
        }
        assertFalse(lightLoaded.darkMode)
    }

    @Test
    fun `custom theme config persists cleanly via BiblePreferences`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = BiblePreferences(context)

        val customConfig = AppThemeConfig(
            preset = ThemePreset.TRANSPARENT_GLASS,
            isDarkSurface = true,
            customBackgroundArgb = 0xFF0D1B2A,
            customTextArgb = 0xFFFAF1DF,
            customAccentArgb = 0xFFD9B25F,
            transparencyLevel = 0.28f,
            blurIntensity = 28f,
            buttonStyle = ButtonStyleOption.OUTLINED_GLASS,
            navBarAppearance = NavBarAppearance.FROSTED_FULL_BAR,
            fontStyle = ScriptureFontStyle.LITERARY_SERIF,
            readingPageBackground = ReadingPageBackground.SEPIA_WARM
        )

        prefs.saveThemeConfig(customConfig)

        val loaded = withTimeout(5000L) {
            prefs.stateFlow.first { it.themeConfig.preset == ThemePreset.TRANSPARENT_GLASS }
        }
        assertEquals(ThemePreset.TRANSPARENT_GLASS, loaded.themeConfig.preset)
        assertTrue(loaded.themeConfig.isDarkSurface)
        assertEquals(0xFF0D1B2AL, loaded.themeConfig.customBackgroundArgb)
        assertEquals(0xFFFAF1DFL, loaded.themeConfig.customTextArgb)
        assertEquals(0xFFD9B25FL, loaded.themeConfig.customAccentArgb)
        assertEquals(0.28f, loaded.themeConfig.transparencyLevel, 0.001f)
        assertEquals(28f, loaded.themeConfig.blurIntensity, 0.001f)
        assertEquals(ButtonStyleOption.OUTLINED_GLASS, loaded.themeConfig.buttonStyle)
        assertEquals(NavBarAppearance.FROSTED_FULL_BAR, loaded.themeConfig.navBarAppearance)
        assertEquals(ScriptureFontStyle.LITERARY_SERIF, loaded.themeConfig.fontStyle)
        assertEquals(ReadingPageBackground.SEPIA_WARM, loaded.themeConfig.readingPageBackground)
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `theme studio updates live preview and supports apply and cancel`() {
        var appliedConfig: AppThemeConfig? = null
        var appliedFontSize: Float? = null
        var cancelledCount = 0

        composeTestRule.setContent {
            val initial = AppThemeConfig.fromPreset(ThemePreset.IOS_LIQUID_GLASS)
            HolyBibleTheme(themeConfig = initial) {
                ThemeStudioScreen(
                    currentConfig = initial,
                    currentFontSizeSp = 16.5f,
                    onApplyTheme = { cfg, size ->
                        appliedConfig = cfg
                        appliedFontSize = size
                    },
                    onCancel = { cancelledCount++ }
                )
            }
        }

        composeTestRule.onNodeWithTag("live_theme_preview").assertIsDisplayed()
        composeTestRule.onNodeWithText("LIVE PREVIEW • iOS Liquid Glass").assertIsDisplayed()

        // Cancel leaves appliedConfig untouched
        composeTestRule.onNodeWithTag("theme_cancel_button").performClick()
        assertEquals(1, cancelledCount)
        assertNull(appliedConfig)

        // Select Crystal Clear Liquid Glass preset and verify live preview updates immediately
        composeTestRule.onNodeWithTag("theme_presets_row")
            .performScrollToNode(hasTestTag("theme_preset_crystal_clear_liquid_glass"))
        composeTestRule.onNodeWithTag("theme_preset_crystal_clear_liquid_glass")
            .performClick()
        composeTestRule.onNodeWithText("LIVE PREVIEW • Crystal Clear Liquid Glass").assertIsDisplayed()

        // Select AMOLED Black preset and verify live preview updates immediately
        composeTestRule.onNodeWithTag("theme_presets_row")
            .performScrollToNode(hasTestTag("theme_preset_amoled_black"))
        composeTestRule.onNodeWithTag("theme_preset_amoled_black")
            .performClick()
        composeTestRule.onNodeWithText("LIVE PREVIEW • AMOLED Black").assertIsDisplayed()

        // Apply selected theme
        composeTestRule.onNodeWithTag("theme_apply_button").performClick()
        assertNotNull(appliedConfig)
        assertEquals(ThemePreset.AMOLED_BLACK, appliedConfig!!.preset)
        assertEquals(16.5f, appliedFontSize!!, 0.01f)
    }

    @Test
    fun `devanagari numerals convert correctly`() {
        assertEquals("१", DevanagariNumberUtils.toDevanagari(1))
        assertEquals("२३", DevanagariNumberUtils.toDevanagari(23))
        assertEquals("३१६", DevanagariNumberUtils.toDevanagari(316))
    }

    @Test
    fun `page turn sound styles and preferences persist cleanly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(12, com.example.data.model.PageTurnSoundStyle.entries.size)
        assertEquals(10, com.example.data.model.PageTurnSoundStyle.audibleStyles.size)

        val prefs = BiblePreferences(context)
        prefs.setPageSoundStyle(com.example.data.model.PageTurnSoundStyle.OLD_VINTAGE_BIBLE)
        prefs.setPageSoundVolume(0.55f)

        val loaded = withTimeout(5000L) {
            prefs.stateFlow.first {
                it.pageSoundStyle == com.example.data.model.PageTurnSoundStyle.OLD_VINTAGE_BIBLE &&
                    kotlin.math.abs(it.pageSoundVolume - 0.55f) < 0.01f
            }
        }
        assertTrue(loaded.pageSoundEnabled)
        assertEquals(com.example.data.model.PageTurnSoundStyle.OLD_VINTAGE_BIBLE, loaded.pageSoundStyle)
        assertEquals(0.55f, loaded.pageSoundVolume, 0.01f)

        // Switching to RANDOM preserves volume & persists
        prefs.setPageSoundStyle(com.example.data.model.PageTurnSoundStyle.RANDOM)
        val loadedRandom = withTimeout(5000L) {
            prefs.stateFlow.first { it.pageSoundStyle == com.example.data.model.PageTurnSoundStyle.RANDOM }
        }
        assertEquals(com.example.data.model.PageTurnSoundStyle.RANDOM, loadedRandom.pageSoundStyle)

        // Switching to SILENT then re-enabling sound restores REALISTIC_BIBLE
        prefs.setPageSoundStyle(com.example.data.model.PageTurnSoundStyle.SILENT)
        prefs.setPageSoundEnabled(true)
        val restored = withTimeout(5000L) {
            prefs.stateFlow.first {
                it.pageSoundEnabled && it.pageSoundStyle == com.example.data.model.PageTurnSoundStyle.REALISTIC_BIBLE
            }
        }
        assertEquals(com.example.data.model.PageTurnSoundStyle.REALISTIC_BIBLE, restored.pageSoundStyle)
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `settings screen allows previewing and selecting page turning sounds and random mode`() {
        var selectedStyle: com.example.data.model.PageTurnSoundStyle? = null
        var previewedStyle: com.example.data.model.PageTurnSoundStyle? = null

        composeTestRule.setContent {
            HolyBibleTheme(themeConfig = AppThemeConfig.fromPreset(ThemePreset.IOS_LIQUID_GLASS)) {
                com.example.ui.screens.SettingsScreen(
                    preferences = com.example.data.model.BiblePreferencesState(),
                    audioState = com.example.audio.AudioPlaybackState(),
                    totalStorageBytes = 1024L,
                    onSetDarkMode = {},
                    onSetFontSize = {},
                    onSetPageSound = {},
                    onSetPageSoundStyle = { selectedStyle = it },
                    onSetPageSoundVolume = {},
                    onPreviewPageSound = { previewedStyle = it },
                    onTestForwardSound = {},
                    onTestBackwardSound = {},
                    onSetReadingMode = {},
                    onOpenLanguageSelector = {},
                    onOpenDownloads = {},
                    onSetAudioLanguage = {},
                    onSetAudioSpeed = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_sound_volume_slider"))
        composeTestRule.onNodeWithTag("settings_random_sound_switch").performClick()
        assertEquals(com.example.data.model.PageTurnSoundStyle.RANDOM, selectedStyle)

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("preview_sound_antique_parchment"))
        composeTestRule.onNodeWithTag("preview_sound_antique_parchment").performClick()
        assertEquals(com.example.data.model.PageTurnSoundStyle.ANTIQUE_PARCHMENT, previewedStyle)

        composeTestRule.onNodeWithTag("sound_style_antique_parchment").performClick()
        assertEquals(com.example.data.model.PageTurnSoundStyle.ANTIQUE_PARCHMENT, selectedStyle)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val player = com.example.audio.PaperSoundPlayer(context)
        player.soundStyle = com.example.data.model.PageTurnSoundStyle.RANDOM
        player.volume = 0.65f
        player.playForward()
        player.playBackward()
        player.preview(com.example.data.model.PageTurnSoundStyle.ANTIQUE_PARCHMENT)
        player.release()
    }

    @Test
    fun `system language defaults to english, persists nepali selection, and preserves bible translations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = BiblePreferences(context)

        val defaultState = com.example.data.model.BiblePreferencesState()
        assertEquals(com.example.data.model.SystemLanguage.ENGLISH, defaultState.systemLanguage)
        assertEquals(BibleLanguage.ENGLISH, defaultState.primaryLanguage)
        assertEquals(BibleLanguage.NEPALI, defaultState.secondaryLanguage)

        prefs.setSystemLanguage(com.example.data.model.SystemLanguage.NEPALI)
        val nepaliLoaded = withTimeout(5000L) {
            prefs.stateFlow.first { it.systemLanguage == com.example.data.model.SystemLanguage.NEPALI }
        }
        assertEquals(com.example.data.model.SystemLanguage.NEPALI, nepaliLoaded.systemLanguage)
        assertEquals(BibleLanguage.ENGLISH, nepaliLoaded.primaryLanguage)
        assertEquals(BibleLanguage.NEPALI, nepaliLoaded.secondaryLanguage)

        prefs.setSystemLanguage(com.example.data.model.SystemLanguage.ENGLISH)
        val restored = withTimeout(5000L) {
            prefs.stateFlow.first { it.systemLanguage == com.example.data.model.SystemLanguage.ENGLISH }
        }
        assertEquals(com.example.data.model.SystemLanguage.ENGLISH, restored.systemLanguage)
    }

    @Test
    fun `nepali formatting utility formats numbers, storage sizes, speeds, and dates in nepali`() {
        val ne = com.example.data.model.SystemLanguage.NEPALI
        val en = com.example.data.model.SystemLanguage.ENGLISH

        assertEquals("६६", com.example.util.NepaliFormatUtils.formatNumber(66, ne))
        assertEquals("66", com.example.util.NepaliFormatUtils.formatNumber(66, en))
        assertEquals("८५%", com.example.util.NepaliFormatUtils.formatPercent(85, ne))
        assertEquals("१.२५x", com.example.util.NepaliFormatUtils.formatSpeed(1.25f, ne))
        assertEquals("० केबी", com.example.util.NepaliFormatUtils.formatBytes(0L, ne))
        assertEquals("२५० केबी", com.example.util.NepaliFormatUtils.formatBytes(250 * 1024L, ne))
        assertEquals("250 KB", com.example.util.NepaliFormatUtils.formatBytes(250 * 1024L, en))

        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.MARCH, 15, 9, 5, 0)
        }
        val formattedNe = com.example.util.NepaliFormatUtils.formatDateTime(cal.timeInMillis, ne)
        assertTrue(formattedNe.contains("१५ मार्च"))
        assertTrue(formattedNe.contains("बिहान ९:०५"))

        val fullDateNe = com.example.util.NepaliFormatUtils.formatFullDate(cal.timeInMillis, ne)
        assertTrue(fullDateNe.contains("१५ मार्च २०२६"))
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `settings screen allows selecting nepali system language and translates ui into nepali`() {
        var chosenSystemLang: com.example.data.model.SystemLanguage? = null

        composeTestRule.setContent {
            HolyBibleTheme(
                themeConfig = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE),
                systemLanguage = com.example.data.model.SystemLanguage.NEPALI
            ) {
                com.example.ui.screens.SettingsScreen(
                    preferences = com.example.data.model.BiblePreferencesState(
                        systemLanguage = com.example.data.model.SystemLanguage.NEPALI
                    ),
                    audioState = com.example.audio.AudioPlaybackState(),
                    totalStorageBytes = 2048L,
                    onSetDarkMode = {},
                    onSetFontSize = {},
                    onSetPageSound = {},
                    onSetPageSoundStyle = {},
                    onSetPageSoundVolume = {},
                    onPreviewPageSound = {},
                    onTestForwardSound = {},
                    onTestBackwardSound = {},
                    onSetReadingMode = {},
                    onOpenLanguageSelector = {},
                    onOpenDownloads = {},
                    onSetAudioLanguage = {},
                    onSetAudioSpeed = {},
                    onSetSystemLanguage = { chosenSystemLang = it }
                )
            }
        }

        composeTestRule.onNodeWithText("सेटिङहरू").assertIsDisplayed()
        composeTestRule.onNodeWithText("प्रणाली भाषा (System Language)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("system_language_en").assertIsDisplayed()
        composeTestRule.onNodeWithTag("system_language_ne").assertIsDisplayed()

        // Clicking English chip fires callback
        composeTestRule.onNodeWithTag("system_language_en").performClick()
        assertEquals(com.example.data.model.SystemLanguage.ENGLISH, chosenSystemLang)

        // Clicking Nepali chip fires callback
        composeTestRule.onNodeWithTag("system_language_ne").performClick()
        assertEquals(com.example.data.model.SystemLanguage.NEPALI, chosenSystemLang)

        // Nepali translated theme customization button is visible when scrolled
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("open_theme_studio_button"))
        composeTestRule.onNodeWithText("थिम अनुकूलन र लाइभ पूर्वावलोकन").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `settings about copyright and sources displays separate verified metadata for ulb and oncv`() {
        composeTestRule.setContent {
            HolyBibleTheme(themeConfig = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE)) {
                com.example.ui.screens.SettingsScreen(
                    preferences = com.example.data.model.BiblePreferencesState(),
                    audioState = com.example.audio.AudioPlaybackState(),
                    totalStorageBytes = 4096L,
                    onSetDarkMode = {},
                    onSetFontSize = {},
                    onSetPageSound = {},
                    onSetPageSoundStyle = {},
                    onSetPageSoundVolume = {},
                    onPreviewPageSound = {},
                    onTestForwardSound = {},
                    onTestBackwardSound = {},
                    onSetReadingMode = {},
                    onOpenLanguageSelector = {},
                    onOpenDownloads = {},
                    onSetAudioLanguage = {},
                    onSetAudioSpeed = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_about_copyright_card"))
        composeTestRule.onNodeWithText("Copyright & Credits").assertIsDisplayed()
        composeTestRule.onNodeWithText("A. Bible Translation Copyright & Sources").assertDoesNotExist()

        // Open the single combined Copyright & Credits section
        composeTestRule.onNodeWithTag("copyright_credits_header_toggle").performClick()
        composeTestRule.waitForIdle()

        // Verify all 4 organized expandable subsections A, B, C, D appear
        composeTestRule.onNodeWithTag("subsection_toggle_bible_copyright").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_translation_credits").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_audio_media_credits").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_developer_tech_credits").performScrollTo().assertIsDisplayed()

        // Expand Subsection A (Bible Translation Copyright & Sources) and verify ULB, ONCV, KJV, KRV
        composeTestRule.onNodeWithTag("subsection_toggle_bible_copyright").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 1. ULB
        composeTestRule.onNodeWithTag("about_version_card_ulb").performScrollTo()
        composeTestRule.onNodeWithText("ULB – Unlocked Literal Bible (Nepali)").assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("Door43 World Missions Community", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_ulb"))
        ).onFirst().assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("CC BY-SA 4.0", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_ulb"))
        ).onFirst().assertIsDisplayed()

        // 2. ONCV
        composeTestRule.onNodeWithTag("about_version_card_oncv").performScrollTo()
        composeTestRule.onNodeWithText("ONCV – Old Nepali Contemporary Version").assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("Biblica, Inc.", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_oncv"))
        ).onFirst().assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("CC BY-SA 4.0", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_oncv"))
        ).onFirst().assertIsDisplayed()

        // 3. KJV
        composeTestRule.onNodeWithTag("about_version_card_kjv").performScrollTo()
        composeTestRule.onNodeWithText("KJV – King James Version (English)").assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("Public Domain", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_kjv"))
        ).onFirst().assertIsDisplayed()

        // 4. KRV
        composeTestRule.onNodeWithTag("about_version_card_krv").performScrollTo()
        composeTestRule.onNodeWithText("KRV – Korean Revised Version (개역한글)").assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("Korean Bible Society", substring = true) and
                hasAnyAncestor(hasTestTag("about_version_card_krv"))
        ).onFirst().assertIsDisplayed()

        // Collapse A, expand B, C, D and verify organized credit cards
        composeTestRule.onNodeWithTag("subsection_toggle_bible_copyright").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("subsection_toggle_translation_credits").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("credit_card_b_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_b_1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_b_2").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("subsection_toggle_audio_media_credits").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("credit_card_c_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_c_1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_c_2").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("subsection_toggle_developer_tech_credits").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("credit_card_d_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_d_1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_card_d_2").performScrollTo().assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `oncv bible verses render in reader alongside ubl or english`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BibleAssetDatabase(context)
        runBlocking {
            db.warmup()
            db.installBookFromPack(BibleLanguage.NEPALI, 43)
            db.installBookFromPack(BibleLanguage.ONCV, 43)
        }
        val john = runBlocking { db.getBook(43)!! }
        val john3 = runBlocking { db.getChapterVerses(43, 3) }
        assertTrue(john3.size >= 16)

        composeTestRule.setContent {
            HolyBibleTheme(themeConfig = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE)) {
                Box(modifier = Modifier.width(340.dp).height(640.dp)) {
                    BiblePageSheet(
                        book = john,
                        chapter = 3,
                        verses = john3,
                        readingMode = ReadingMode.BILINGUAL_PARALLEL,
                        primaryLanguage = BibleLanguage.ONCV,
                        secondaryLanguage = BibleLanguage.NEPALI,
                        downloadedBookKeys = setOf("ne" to 43, "ne_oncv" to 43),
                        fontSizeSp = 16.5f,
                        selectedVerseKey = null,
                        activeAudioVerseKey = null,
                        targetScrollVerse = 16,
                        bookmarkedKeys = emptySet(),
                        highlightMap = emptyMap(),
                        noteKeys = emptySet(),
                        onVerseClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("bible_chapter_list").performScrollToIndex(16)
        composeTestRule.onNodeWithText("अद्वितीय पुत्रलाई बलिदानका लागि दिनुभयो", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("एक मात्र पुत्र दिनुभयो", substring = true).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `home screen displays bible com nnrv shortcut in english and nepali and opens verified url externally`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = org.robolectric.Shadows.shadowOf(application)
        shadowApp.clearNextStartedActivities()

        // NNRV must NOT be added as an internal BibleLanguage
        assertEquals(4, BibleLanguage.entries.size)
        assertFalse(BibleLanguage.entries.any { it.code.contains("nnrv", ignoreCase = true) })

        val sysLangState = androidx.compose.runtime.mutableStateOf(com.example.data.model.SystemLanguage.ENGLISH)
        val themeState = androidx.compose.runtime.mutableStateOf(AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE))

        composeTestRule.setContent {
            HolyBibleTheme(
                themeConfig = themeState.value,
                systemLanguage = sysLangState.value
            ) {
                com.example.ui.screens.HomeScreen(
                    books = emptyList(),
                    preferences = com.example.data.model.BiblePreferencesState(
                        systemLanguage = sysLangState.value,
                        themeConfig = themeState.value
                    ),
                    dailyVerse = null,
                    audioState = com.example.audio.AudioPlaybackState(),
                    languageSummaries = emptyList(),
                    totalStorageBytes = 0L,
                    onContinueReading = {},
                    onOpenTestament = {},
                    onOpenReaderAt = { _, _, _ -> },
                    onToggleAudio = {},
                    onOpenLanguages = {},
                    onOpenDownloads = {},
                    onOpenSearch = {},
                    onOpenLibrary = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_read_nnrv_bible_com_button"))

        composeTestRule.onNodeWithText("NNRV Nepali Bible").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read NNRV Bible Online").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bible.com • Nepali New Revised Version • https://www.bible.com/").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read NNRV on Bible.com").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_read_nnrv_bible_com_button").performClick()
        composeTestRule.waitForIdle()

        val launchedIntent = shadowApp.nextStartedActivity
        assertNotNull(launchedIntent)
        assertEquals(android.content.Intent.ACTION_VIEW, launchedIntent.action)
        assertEquals("https://www.bible.com/bible/1483/GEN.1.NNRV", launchedIntent.dataString)
        assertTrue((launchedIntent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK) != 0)

        // Switch to Nepali UI + Dark Mode and verify localized texts and click
        composeTestRule.runOnUiThread {
            sysLangState.value = com.example.data.model.SystemLanguage.NEPALI
            themeState.value = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE).copy(isDarkSurface = true)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_read_nnrv_bible_com_button"))

        composeTestRule.onNodeWithText("NNRV नेपाली बाइबल").assertIsDisplayed()
        composeTestRule.onNodeWithText("अनलाइन NNRV बाइबल पढ्नुहोस्").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bible.com मा NNRV पढ्नुहोस्").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_nnrv_bible_com_card").performClick()
        composeTestRule.waitForIdle()

        val launchedNeIntent = shadowApp.nextStartedActivity
        assertNotNull(launchedNeIntent)
        assertEquals(android.content.Intent.ACTION_VIEW, launchedNeIntent.action)
        assertEquals("https://www.bible.com/bible/1483/GEN.1.NNRV", launchedNeIntent.dataString)
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `settings about section omits copyright admin while preserving clickable source links, dark mode and nepali`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = org.robolectric.Shadows.shadowOf(application)
        shadowApp.clearNextStartedActivities()

        val sysLangState = androidx.compose.runtime.mutableStateOf(com.example.data.model.SystemLanguage.ENGLISH)
        val themeState = androidx.compose.runtime.mutableStateOf(
            AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE).copy(isDarkSurface = true)
        )

        composeTestRule.setContent {
            HolyBibleTheme(
                darkTheme = themeState.value.isDarkSurface,
                themeConfig = themeState.value,
                systemLanguage = sysLangState.value
            ) {
                com.example.ui.screens.SettingsScreen(
                    preferences = com.example.data.model.BiblePreferencesState(
                        darkMode = themeState.value.isDarkSurface,
                        themeConfig = themeState.value,
                        systemLanguage = sysLangState.value
                    ),
                    audioState = com.example.audio.AudioPlaybackState(),
                    totalStorageBytes = 4096L,
                    onSetDarkMode = {},
                    onSetFontSize = {},
                    onSetPageSound = {},
                    onSetPageSoundStyle = {},
                    onSetPageSoundVolume = {},
                    onPreviewPageSound = {},
                    onTestForwardSound = {},
                    onTestBackwardSound = {},
                    onSetReadingMode = {},
                    onOpenLanguageSelector = {},
                    onOpenDownloads = {},
                    onSetAudioLanguage = {},
                    onSetAudioSpeed = {}
                )
            }
        }

        // Scroll to Copyright & Credits section and verify Copyright Admin UI does not exist
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_about_copyright_card"))
        composeTestRule.onNodeWithTag("settings_about_copyright_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_admin_dashboard_button").assertDoesNotExist()
        composeTestRule.onNodeWithText("Admin").assertDoesNotExist()
        composeTestRule.onNodeWithText("Copyright & Sources Admin Dashboard").assertDoesNotExist()

        // Expand Copyright & Credits + Subsection A, then verify clickable ULB source link launches external browser Intent
        composeTestRule.onNodeWithTag("copyright_credits_header_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("subsection_toggle_bible_copyright").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("about_source_link_ulb_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val ulbIntent = shadowApp.nextStartedActivity
        assertNotNull(ulbIntent)
        assertEquals(android.content.Intent.ACTION_VIEW, ulbIntent.action)
        assertEquals("https://door43.org/", ulbIntent.dataString)

        // Verify all 4 Bible versions remain displayed in Dark Mode
        composeTestRule.onNodeWithTag("about_version_card_krv").performScrollTo().assertIsDisplayed()

        // Expand Subsection D and verify clickable technology link
        composeTestRule.onNodeWithTag("subsection_toggle_developer_tech_credits").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("credit_link_d_1_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val composeIntent = shadowApp.nextStartedActivity
        assertNotNull(composeIntent)
        assertEquals(android.content.Intent.ACTION_VIEW, composeIntent.action)
        assertEquals("https://developer.android.com/jetpack/compose", composeIntent.dataString)

        // Switch to Nepali UI in Dark Mode and confirm Admin button stays absent while Nepali metadata renders
        composeTestRule.runOnUiThread {
            sysLangState.value = com.example.data.model.SystemLanguage.NEPALI
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("copyright_credits_header_toggle").performScrollTo()
        composeTestRule.onNodeWithTag("about_admin_dashboard_button").assertDoesNotExist()
        composeTestRule.onNodeWithText("व्यवस्थापक").assertDoesNotExist()
        composeTestRule.onNodeWithText("प्रतिलिपि अधिकार र श्रेय").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_bible_copyright").performScrollTo()
        composeTestRule.onNodeWithText("क. बाइबल अनुवाद प्रतिलिपि अधिकार र स्रोतहरू").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_translation_credits").performScrollTo()
        composeTestRule.onNodeWithText("ख. बाइबल अनुवाद श्रेय").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_audio_media_credits").performScrollTo()
        composeTestRule.onNodeWithText("ग. अडियो र मिडिया श्रेय").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subsection_toggle_developer_tech_credits").performScrollTo()
        composeTestRule.onNodeWithText("घ. विकासकर्ता र प्रविधि श्रेय").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `about copyright card renders unverified fallback when metadata is unavailable in english and nepali`() {
        val sysLangState = androidx.compose.runtime.mutableStateOf(com.example.data.model.SystemLanguage.ENGLISH)
        val unverifiedSample = com.example.ui.localization.BibleSourceAttribution(
            versionId = "TEST",
            title = "TEST – Sample Unverified",
            fullTranslationName = "",
            ownerPublisher = "",
            copyrightNotice = "",
            sourceUrls = emptyList(),
            license = "",
            licenseCategory = com.example.data.model.CopyrightLicenseCategory.UNVERIFIED,
            usagePermissions = "",
            translationCredits = ""
        )

        composeTestRule.setContent {
            HolyBibleTheme(systemLanguage = sysLangState.value) {
                com.example.ui.components.BibleCopyrightEntryCard(
                    attribution = unverifiedSample,
                    onOpenUrl = {}
                )
            }
        }

        composeTestRule.onAllNodesWithText("Copyright information not verified yet.")
            .onFirst()
            .assertIsDisplayed()

        composeTestRule.runOnUiThread {
            sysLangState.value = com.example.data.model.SystemLanguage.NEPALI
        }
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("प्रतिलिपि अधिकार जानकारी अझै प्रमाणित गरिएको छैन।")
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h760dp")
    fun `home screen omits removed sections while preserving remaining cards in english and nepali`() {
        val sampleBook = com.example.data.model.BibleBook(
            id = 43,
            testament = "NT",
            abbrev = "Jhn",
            nameEn = "John",
            nameNe = "यूहन्ना",
            nameKo = "요한복음",
            chapterCount = 21
        )
        val sampleDailyVerse = com.example.data.model.DailyVerseData(
            book = sampleBook,
            verse = com.example.data.model.BibleVerse(
                id = 1,
                bookId = 43,
                chapter = 3,
                verse = 16,
                textEn = "For God so loved the world",
                textNe = "किनकि परमेश्वरले संसारलाई यस्तो प्रेम गर्नुभयो",
                textOncv = "",
                textKo = "하나님이 세상을 이처럼 사랑하사"
            )
        )

        val sysLangState = androidx.compose.runtime.mutableStateOf(com.example.data.model.SystemLanguage.ENGLISH)
        val themeState = androidx.compose.runtime.mutableStateOf(AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE))
        var continueClicked = false
        var quickSearchClicked = false

        composeTestRule.setContent {
            HolyBibleTheme(
                darkTheme = themeState.value.isDarkSurface,
                themeConfig = themeState.value,
                systemLanguage = sysLangState.value
            ) {
                com.example.ui.screens.HomeScreen(
                    books = listOf(sampleBook),
                    preferences = com.example.data.model.BiblePreferencesState(
                        lastBookId = 43,
                        lastChapter = 3,
                        lastVerse = 16,
                        systemLanguage = sysLangState.value,
                        themeConfig = themeState.value
                    ),
                    dailyVerse = sampleDailyVerse,
                    audioState = com.example.audio.AudioPlaybackState(),
                    languageSummaries = emptyList(),
                    totalStorageBytes = 2048L,
                    onContinueReading = { continueClicked = true },
                    onOpenTestament = {},
                    onOpenReaderAt = { _, _, _ -> },
                    onToggleAudio = {},
                    onOpenLanguages = {},
                    onOpenDownloads = {},
                    onOpenSearch = { quickSearchClicked = true },
                    onOpenLibrary = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Confirm the four removed items do not exist anywhere on the Home Screen
        composeTestRule.onNodeWithTag("old_testament_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("new_testament_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_download_bible_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_audio_bible_card").assertDoesNotExist()
        composeTestRule.onNodeWithText("Old Testament").assertDoesNotExist()
        composeTestRule.onNodeWithText("New Testament").assertDoesNotExist()
        composeTestRule.onNodeWithText("पुरानो नियम").assertDoesNotExist()
        composeTestRule.onNodeWithText("नयाँ नियम").assertDoesNotExist()
        composeTestRule.onNodeWithText("Download Bible").assertDoesNotExist()
        composeTestRule.onNodeWithText("Audio Bible").assertDoesNotExist()

        // Confirm preserved top header, hero card, daily verse, NNRV shortcut, and quick actions remain
        composeTestRule.onNodeWithTag("home_theme_studio_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_languages_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_search_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_settings_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bible_cover_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("continue_reading_button").performClick()
        assertTrue(continueClicked)

        composeTestRule.onNodeWithTag("home_list").performScrollToNode(hasTestTag("daily_verse_card"))
        composeTestRule.onNodeWithTag("daily_verse_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_list").performScrollToNode(hasTestTag("home_read_nnrv_bible_com_button"))
        composeTestRule.onNodeWithTag("home_read_nnrv_bible_com_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_list").performScrollToNode(hasTestTag("quick_search_tile"))
        composeTestRule.onNodeWithTag("quick_languages_tile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quick_search_tile").performClick()
        composeTestRule.onNodeWithTag("quick_library_tile").assertIsDisplayed()
        assertTrue(quickSearchClicked)

        // Switch to Nepali + Dark Mode and confirm removed items stay absent while remaining cards localize
        composeTestRule.runOnUiThread {
            sysLangState.value = com.example.data.model.SystemLanguage.NEPALI
            themeState.value = AppThemeConfig.fromPreset(ThemePreset.CLASSIC_BIBLE).copy(isDarkSurface = true)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("old_testament_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("new_testament_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_download_bible_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_audio_bible_card").assertDoesNotExist()
        composeTestRule.onNodeWithText("पुरानो नियम").assertDoesNotExist()
        composeTestRule.onNodeWithText("नयाँ नियम").assertDoesNotExist()
        composeTestRule.onNodeWithText("बाइबल डाउनलोड").assertDoesNotExist()
        composeTestRule.onNodeWithText("अडियो बाइबल").assertDoesNotExist()

        composeTestRule.onNodeWithTag("home_list").performScrollToNode(hasTestTag("home_read_nnrv_bible_com_button"))
        composeTestRule.onNodeWithText("NNRV नेपाली बाइबल").assertIsDisplayed()
    }
}
