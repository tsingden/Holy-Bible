package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioPlaybackState
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BiblePreferencesState
import com.example.data.model.DailyVerseData
import com.example.data.model.LanguageDownloadSummary
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.GoldShimmer
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.BibleShareHelper
import com.example.util.DevanagariNumberUtils
import com.example.util.NepaliFormatUtils

@Composable
fun HomeScreen(
    books: List<BibleBook>,
    preferences: BiblePreferencesState,
    dailyVerse: DailyVerseData?,
    audioState: AudioPlaybackState,
    languageSummaries: List<LanguageDownloadSummary>,
    totalStorageBytes: Long,
    onContinueReading: () -> Unit,
    onOpenTestament: (String) -> Unit,
    onOpenReaderAt: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onToggleAudio: () -> Unit,
    onOpenLanguages: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThemeStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    val context = LocalContext.current
    val lastBook = books.firstOrNull { it.id == preferences.lastBookId }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .testTag("home_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.appTitle,
                            fontFamily = headerFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = if (sysLang == SystemLanguage.NEPALI) 0.sp else 1.5.sp,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = strings.appSubtitleLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row {
                        IconButton(
                            onClick = onOpenThemeStudio,
                            modifier = Modifier.testTag("home_theme_studio_icon")
                        ) {
                            Icon(
                                Icons.Filled.Palette,
                                contentDescription = strings.homeThemesDesc,
                                tint = tokens.accentColor
                            )
                        }
                        IconButton(
                            onClick = onOpenLanguages,
                            modifier = Modifier.testTag("home_languages_icon")
                        ) {
                            Icon(
                                Icons.Filled.Translate,
                                contentDescription = strings.homeLanguagesDesc,
                                tint = tokens.textPrimary
                            )
                        }
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier.testTag("home_search_icon")
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = strings.homeSearchDesc,
                                tint = tokens.textPrimary
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("home_settings_icon")
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = strings.homeSettingsDesc,
                                tint = tokens.textPrimary
                            )
                        }
                    }
                }
            }

            // 1. Classic Holy Bible Leather Cover Hero Card + Continue Reading
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bible_cover_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(275.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_bible_cover),
                            contentDescription = strings.homeBibleCoverDesc,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            BurgundyDeep.copy(alpha = 0.55f),
                                            BurgundyDeep.copy(alpha = 0.35f),
                                            BurgundyDeep.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        // Active Language Pair Ribbon Badge
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                            color = GoldShimmer,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 24.dp)
                                .clickable(onClick = onOpenLanguages)
                        ) {
                            Text(
                                text = "${preferences.primaryLanguage.shortBadge} • ${preferences.secondaryLanguage.shortBadge}",
                                color = BurgundyDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.heroTitle,
                                    fontFamily = headerFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    letterSpacing = if (sysLang == SystemLanguage.NEPALI) 0.5.sp else 3.sp,
                                    color = GoldShimmer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.heroNepaliTitle,
                                    fontFamily = if (sysLang == SystemLanguage.NEPALI) PlayfairDisplayFamily else NotoSerifDevanagariFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = Color(0xFFFFF4DC),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.heroEditionFormat(
                                        strings.bibleLanguageName(preferences.primaryLanguage),
                                        strings.bibleLanguageName(preferences.secondaryLanguage)
                                    ),
                                    fontSize = 12.sp,
                                    color = Color(0xFFEAD9BA),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = onContinueReading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldShimmer,
                                    contentColor = BurgundyDeep
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("continue_reading_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val refText = if (lastBook != null) {
                                    val bookDisplay = if (sysLang == SystemLanguage.NEPALI) lastBook.nameNe else lastBook.nameEn
                                    strings.continueReadingPrefix(
                                        bookDisplay,
                                        NepaliFormatUtils.formatNumber(preferences.lastChapter, sysLang),
                                        NepaliFormatUtils.formatNumber(preferences.lastVerse, sysLang)
                                    )
                                } else {
                                    strings.openHolyBible
                                }
                                Text(
                                    text = refText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 2. Daily Bible Verse Devotional Card
            if (dailyVerse != null) {
                item {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(22.dp),
                        elevated = true,
                        tokens = tokens,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_verse_card")
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_daily_verse_banner),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alpha = if (palette.isDark) 0.14f else 0.35f,
                                modifier = Modifier.matchParentSize()
                            )

                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = palette.leatherAccent.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = strings.dailyVerseBadge,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = palette.leatherAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            BibleShareHelper.shareToWhatsApp(
                                                context,
                                                dailyVerse.book,
                                                dailyVerse.verse,
                                                preferences.primaryLanguage,
                                                preferences.secondaryLanguage
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = strings.shareDailyVerseDesc,
                                            tint = palette.leatherAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val enTxt = dailyVerse.verse.textEn
                                val preferOncvDaily = preferences.primaryLanguage == BibleLanguage.ONCV ||
                                    (preferences.secondaryLanguage == BibleLanguage.ONCV && preferences.primaryLanguage != BibleLanguage.NEPALI)
                                val neTxt = if (preferOncvDaily) {
                                    dailyVerse.verse.textOncv.ifBlank { dailyVerse.verse.textNe }
                                } else {
                                    dailyVerse.verse.textNe.ifBlank { dailyVerse.verse.textOncv }
                                }
                                if (enTxt.isNotBlank()) {
                                    Text(
                                        text = "“$enTxt”",
                                        fontFamily = tokens.fontStyle.englishFontFamily(),
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                        color = tokens.textPrimary
                                    )
                                }
                                if (neTxt.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "“$neTxt”",
                                        fontFamily = NotoSerifDevanagariFamily,
                                        fontSize = 15.5.sp,
                                        lineHeight = 23.sp,
                                        color = tokens.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${dailyVerse.book.nameEn} ${dailyVerse.verse.chapter}:${dailyVerse.verse.verse} • ${dailyVerse.book.nameNe} ${DevanagariNumberUtils.toDevanagari(dailyVerse.verse.chapter)}:${DevanagariNumberUtils.toDevanagari(dailyVerse.verse.verse)}",
                                        fontFamily = PlayfairDisplayFamily,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = tokens.accentColor,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            onOpenReaderAt(
                                                dailyVerse.book.id,
                                                dailyVerse.verse.chapter,
                                                dailyVerse.verse.verse
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("daily_verse_read_button")
                                    ) {
                                        Text(strings.readChapter, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. External Bible.com NNRV Nepali Bible Shortcut
            item {
                val openNnrvOnBibleCom: () -> Unit = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(BIBLE_COM_NNRV_URL)
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        Toast.makeText(context, strings.nnrvOpenBrowserError, Toast.LENGTH_SHORT).show()
                    }
                }

                LiquidGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    tokens = tokens,
                    onClick = openNnrvOnBibleCom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_nnrv_bible_com_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = strings.nnrvShortcutTitle,
                                    tint = tokens.onAccentColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.nnrvShortcutTitle,
                                    fontFamily = headerFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = strings.nnrvShortcutSubtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 13.sp,
                                    color = tokens.textSecondary
                                )
                                Text(
                                    text = strings.nnrvShortcutMeta,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = tokens.textSecondary.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = openNnrvOnBibleCom,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.accentColor,
                                contentColor = tokens.onAccentColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_read_nnrv_bible_com_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.nnrvShortcutButton,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 4. Quick Actions Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Filled.Translate,
                        label = strings.quickLanguages,
                        sub = strings.quickLanguagesSub,
                        onClick = onOpenLanguages,
                        testTag = "quick_languages_tile",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Filled.Search,
                        label = strings.quickSearch,
                        sub = strings.quickSearchSub,
                        onClick = onOpenSearch,
                        testTag = "quick_search_tile",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Filled.Bookmarks,
                        label = strings.quickSaved,
                        sub = strings.quickSavedSub,
                        onClick = onOpenLibrary,
                        testTag = "quick_library_tile",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLiquidGlass.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(16.dp),
        tokens = tokens,
        onClick = onClick,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tokens.accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = tokens.textPrimary
            )
            Text(
                text = sub,
                fontSize = 10.5.sp,
                color = tokens.textSecondary
            )
        }
    }
}

const val BIBLE_COM_NNRV_URL = "https://www.bible.com/bible/1483/GEN.1.NNRV"
