package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleLanguage
import com.example.data.model.LanguageDownloadSummary
import com.example.data.model.ReadingMode
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.NepaliFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorSheet(
    primaryLanguage: BibleLanguage,
    secondaryLanguage: BibleLanguage,
    readingMode: ReadingMode,
    languageSummaries: List<LanguageDownloadSummary>,
    onSelectPrimary: (BibleLanguage) -> Unit,
    onSelectSecondary: (BibleLanguage) -> Unit,
    onSwapLanguages: () -> Unit,
    onSetReadingMode: (ReadingMode) -> Unit,
    onDownloadLanguage: (BibleLanguage) -> Unit,
    onOpenDownloadsManager: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingSlot by remember { mutableIntStateOf(0) } // 0 = Primary (Left), 1 = Secondary (Right)

    val summaryMap = remember(languageSummaries) {
        languageSummaries.associateBy { it.language }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (tokens.isGlassActive) tokens.glassSurfaceElevated else MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = if (tokens.isCrystalClear) 0.24f else 0.36f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = null,
                        tint = tokens.accentColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = strings.bibleLanguagesTitle,
                            fontFamily = headerFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = strings.bibleLanguagesSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.textSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = strings.closeLanguageSelectorDesc, tint = tokens.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Side-by-Side Pair Preview with Swap Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (tokens.isGlassActive) tokens.glassSurface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = if (tokens.isGlassActive) BorderStroke(1.dp, tokens.borderTop) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.primaryLeftPageLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = primaryLanguage.nativeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tokens.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onSwapLanguages,
                        modifier = Modifier.testTag("swap_languages_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = strings.swapLanguagesDesc,
                            tint = tokens.accentColor
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = strings.compareRightPageLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = secondaryLanguage.nativeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tokens.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Reading Layout Mode Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReadingMode.entries.forEach { mode ->
                    val shortLabel = when (mode) {
                        ReadingMode.BILINGUAL_SPREAD -> strings.spreadModeShort
                        ReadingMode.BILINGUAL_PARALLEL -> strings.parallelModeShort
                        ReadingMode.SINGLE_PRIMARY -> strings.bibleLanguageName(primaryLanguage)
                        ReadingMode.SINGLE_SECONDARY -> strings.bibleLanguageName(secondaryLanguage)
                    }
                    FilterChip(
                        selected = readingMode == mode,
                        onClick = { onSetReadingMode(mode) },
                        label = { Text(shortLabel, fontSize = 11.5.sp, maxLines = 1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_sheet_mode_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            TabRow(
                selectedTabIndex = editingSlot,
                containerColor = Color.Transparent,
                contentColor = tokens.accentColor
            ) {
                Tab(
                    selected = editingSlot == 0,
                    onClick = { editingSlot = 0 },
                    text = { Text(strings.tabPrimaryPrefix(strings.bibleLanguageName(primaryLanguage)), maxLines = 1) },
                    modifier = Modifier.testTag("tab_primary_lang")
                )
                Tab(
                    selected = editingSlot == 1,
                    onClick = { editingSlot = 1 },
                    text = { Text(strings.tabComparePrefix(strings.bibleLanguageName(secondaryLanguage)), maxLines = 1) },
                    modifier = Modifier.testTag("tab_secondary_lang")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val activeLang = if (editingSlot == 0) primaryLanguage else secondaryLanguage

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BibleLanguage.entries, key = { it.code }) { lang ->
                    val isSelected = lang == activeLang
                    val summary = summaryMap[lang]
                    val isReady = summary?.isDownloading == false && (summary.downloadedBooks > 0)
                    val isFull = summary?.isFullyDownloaded == true

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isSelected -> tokens.accentColor.copy(alpha = 0.22f)
                            tokens.isGlassActive -> tokens.glassSurface
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        border = when {
                            isSelected -> BorderStroke(1.5.dp, tokens.accentColor)
                            tokens.isGlassActive -> BorderStroke(0.9.dp, tokens.borderTop)
                            else -> null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (editingSlot == 0) onSelectPrimary(lang) else onSelectSecondary(lang)
                            }
                            .testTag("select_lang_${lang.code}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${strings.bibleLanguageName(lang)} — ${lang.nativeName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        text = "${lang.translationName} • ${strings.booksCountWord(lang.supportedBookIds.size)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = tokens.textSecondary
                                    )
                                }

                                when {
                                    isFull -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = strings.offlineReadyLabel,
                                                tint = palette.leatherAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSelected) strings.activeLabel else strings.offlineLabel,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.leatherAccent
                                            )
                                        }
                                    }
                                    summary?.isDownloading == true -> {
                                        Text(
                                            text = "${NepaliFormatUtils.formatNumber(summary.downloadedBooks, sysLang)}/${NepaliFormatUtils.formatNumber(summary.totalBooks, sysLang)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.goldAccent
                                        )
                                    }
                                    else -> {
                                        OutlinedButton(
                                            onClick = { onDownloadLanguage(lang) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("sheet_download_${lang.code}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Download,
                                                contentDescription = "${strings.download} ${strings.bibleLanguageName(lang)}",
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.download, fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }

                            if (summary != null && summary.isDownloading) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { summary.progressFraction },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (isReady && !isFull && summary != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.booksDownloadedProgress(summary.downloadedBooks, summary.totalBooks),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onOpenDownloadsManager()
                    },
                    modifier = Modifier.testTag("open_downloads_from_sheet")
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.manageOfflineDownloads)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.accentColor,
                        contentColor = tokens.onAccentColor
                    )
                ) {
                    Text(strings.done)
                }
            }
        }
    }
}
