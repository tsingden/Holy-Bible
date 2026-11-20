package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BookDownloadState
import com.example.data.model.DownloadStatus
import com.example.data.model.LanguageDownloadSummary
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.localization.SystemLocaleManager
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.GoldShimmer
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassDialog
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.NepaliFormatUtils

@Composable
fun DownloadsScreen(
    books: List<BibleBook>,
    languageSummaries: List<LanguageDownloadSummary>,
    bookDownloadStates: Map<Pair<String, Int>, BookDownloadState>,
    totalStorageBytes: Long,
    onBack: () -> Unit,
    onDownloadCompleteBible: () -> Unit,
    onDownloadLanguage: (BibleLanguage) -> Unit,
    onDownloadBook: (BibleLanguage, Int) -> Unit,
    onPauseLanguage: (BibleLanguage) -> Unit,
    onPauseAll: () -> Unit,
    onResumeLanguage: (BibleLanguage) -> Unit,
    onResumeAll: () -> Unit,
    onCancelLanguage: (BibleLanguage) -> Unit,
    onCancelBook: (BibleLanguage, Int) -> Unit,
    onDeleteLanguage: (BibleLanguage) -> Unit,
    onDeleteBook: (BibleLanguage, Int) -> Unit,
    onReadLanguage: (BibleLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    var confirmDeleteLang by remember { mutableStateOf<BibleLanguage?>(null) }

    val totalSupportedAcrossAll = remember(languageSummaries) {
        languageSummaries.sumOf { it.totalBooks }
    }
    val totalDownloadedAcrossAll = remember(languageSummaries) {
        languageSummaries.sumOf { it.downloadedBooks }
    }
    val anyDownloading = remember(languageSummaries) {
        languageSummaries.any { it.isDownloading }
    }
    val anyPaused = remember(languageSummaries) {
        languageSummaries.any { it.isPaused }
    }
    val allDownloaded = totalSupportedAcrossAll > 0 && totalDownloadedAcrossAll >= totalSupportedAcrossAll

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("downloads_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = tokens.textPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.offlineDownloadsTitle,
                            fontFamily = headerFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = strings.offlineDownloadsSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.textSecondary
                        )
                    }
                }
            }

            // Master Offline Storage & Download Complete Bible Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.leatherAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = GoldShimmer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.storageUsedLabel(
                                            NepaliFormatUtils.formatBytes(totalStorageBytes, sysLang)
                                        ),
                                        color = GoldShimmer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.testTag("total_storage_label")
                                    )
                                    Text(
                                        text = strings.languageBooksOfflineCount(
                                            totalDownloadedAcrossAll,
                                            totalSupportedAcrossAll
                                        ),
                                        color = GoldShimmer.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (allDownloaded) {
                                Icon(
                                    imageVector = Icons.Filled.CloudDone,
                                    contentDescription = strings.completeBibleOffline,
                                    tint = GoldShimmer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (totalSupportedAcrossAll == 0) 0f
                                else totalDownloadedAcrossAll.toFloat() / totalSupportedAcrossAll.toFloat()
                            },
                            color = GoldShimmer,
                            trackColor = BurgundyDeep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onDownloadCompleteBible,
                                enabled = !allDownloaded,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldShimmer,
                                    contentColor = BurgundyDeep
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_complete_bible_button")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (allDownloaded) strings.completeBibleOffline else strings.downloadBibleButton,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (anyDownloading) {
                                OutlinedButton(
                                    onClick = onPauseAll,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldShimmer),
                                    modifier = Modifier.testTag("pause_all_downloads_button")
                                ) {
                                    Icon(Icons.Filled.Pause, contentDescription = strings.pauseAllDesc, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.pause)
                                }
                            } else if (anyPaused) {
                                OutlinedButton(
                                    onClick = onResumeAll,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldShimmer),
                                    modifier = Modifier.testTag("resume_all_downloads_button")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = strings.resumeAllDesc, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.resume)
                                }
                            }
                        }
                    }
                }
            }

            // Per-Language Download Cards + Expandable Individual Book Management
            items(languageSummaries, key = { it.language.code }) { summary ->
                val lang = summary.language
                val isExpanded = expandedMap[lang.code] == true
                val supportedBooks = remember(books, lang) {
                    books.filter { lang.supportsBook(it.id) }
                }

                LiquidGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    tokens = tokens,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_card_${lang.code}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${strings.bibleLanguageName(lang)} • ${lang.nativeName}",
                                    fontFamily = headerFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = tokens.accentColor
                                )
                                Text(
                                    text = lang.translationName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = lang.licenseNote,
                                    fontSize = 11.5.sp,
                                    color = tokens.textSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (summary.isFullyDownloaded) {
                                    tokens.accentColor.copy(alpha = 0.16f)
                                } else {
                                    palette.goldAccent.copy(alpha = 0.16f)
                                }
                            ) {
                                Text(
                                    text = when {
                                        summary.isFullyDownloaded -> strings.statusOfflineReady
                                        summary.isDownloading -> strings.statusDownloading
                                        summary.isPaused -> strings.statusPaused
                                        summary.downloadedBooks > 0 -> strings.statusPartial
                                        else -> strings.statusNotDownloaded
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.isFullyDownloaded) tokens.accentColor else tokens.textPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.booksSlashCount(summary.downloadedBooks, summary.totalBooks),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textPrimary,
                                modifier = Modifier.testTag("download_count_${lang.code}")
                            )
                            Text(
                                text = NepaliFormatUtils.formatBytes(summary.totalBytesOnDisk, sysLang),
                                fontSize = 12.5.sp,
                                color = tokens.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { summary.progressFraction },
                            color = tokens.accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Row (Download / Pause / Resume / Cancel / Delete / Read)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when {
                                summary.isDownloading -> {
                                    OutlinedButton(
                                        onClick = { onPauseLanguage(lang) },
                                        modifier = Modifier.testTag("pause_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.pause, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onCancelLanguage(lang) },
                                        modifier = Modifier.testTag("cancel_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.cancel, fontSize = 12.sp)
                                    }
                                }
                                summary.isPaused -> {
                                    LiquidGlassButton(
                                        onClick = { onResumeLanguage(lang) },
                                        tokens = tokens,
                                        modifier = Modifier.testTag("resume_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.resume, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onCancelLanguage(lang) },
                                        modifier = Modifier.testTag("cancel_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.cancel, fontSize = 12.sp)
                                    }
                                }
                                !summary.isFullyDownloaded -> {
                                    LiquidGlassButton(
                                        onClick = { onDownloadLanguage(lang) },
                                        tokens = tokens,
                                        modifier = Modifier.testTag("download_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (summary.downloadedBooks > 0) strings.downloadRemaining else strings.downloadAll,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                else -> {
                                    OutlinedButton(
                                        onClick = { onReadLanguage(lang) },
                                        modifier = Modifier.testTag("read_lang_${lang.code}")
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.readOffline, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (summary.downloadedBooks > 0) {
                                IconButton(
                                    onClick = { confirmDeleteLang = lang },
                                    modifier = Modifier.testTag("delete_lang_${lang.code}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = strings.deletePackDesc(strings.bibleLanguageName(lang)),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = palette.goldAccent.copy(alpha = 0.25f)
                        )

                        // Individual Books Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expandedMap[lang.code] = !isExpanded }
                                .padding(vertical = 4.dp)
                                .testTag("expand_books_${lang.code}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.individualBooksHeader(supportedBooks.size),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = tokens.accentColor
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) strings.hideBooksDesc else strings.showBooksDesc,
                                tint = tokens.accentColor
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                supportedBooks.forEach { book ->
                                    val st = bookDownloadStates[lang.code to book.id]
                                    val status = st?.status ?: DownloadStatus.NOT_DOWNLOADED
                                    val primaryBookTitle = if (sysLang == SystemLanguage.NEPALI) book.nameNe else book.nameEn
                                    val localized = book.nameFor(lang)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tokens.glassSurface.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${NepaliFormatUtils.formatNumber(book.id, sysLang)}. $primaryBookTitle",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = tokens.textPrimary
                                            )
                                            val subtitle = buildString {
                                                if (localized != primaryBookTitle) {
                                                    append(localized)
                                                    append(" • ")
                                                }
                                                append(
                                                    when (status) {
                                                        DownloadStatus.DOWNLOADED -> strings.bookOfflineSize(
                                                            NepaliFormatUtils.formatBytes(st?.bytesSize ?: 0L, sysLang)
                                                        )
                                                        DownloadStatus.DOWNLOADING -> strings.bookDownloading
                                                        DownloadStatus.QUEUED -> strings.bookQueued
                                                        DownloadStatus.PAUSED -> strings.bookPaused
                                                        DownloadStatus.NOT_DOWNLOADED -> strings.bookNotDownloaded
                                                    }
                                                )
                                            }
                                            Text(
                                                text = subtitle,
                                                fontSize = 11.sp,
                                                color = tokens.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        when (status) {
                                            DownloadStatus.DOWNLOADED -> {
                                                IconButton(
                                                    onClick = { onDeleteBook(lang, book.id) },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .testTag("delete_book_${lang.code}_${book.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "${strings.delete} $primaryBookTitle",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING, DownloadStatus.PAUSED -> {
                                                IconButton(
                                                    onClick = { onCancelBook(lang, book.id) },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .testTag("cancel_book_${lang.code}_${book.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Cancel,
                                                        contentDescription = "${strings.cancel} $primaryBookTitle",
                                                        tint = palette.goldAccent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            DownloadStatus.NOT_DOWNLOADED -> {
                                                IconButton(
                                                    onClick = { onDownloadBook(lang, book.id) },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .testTag("download_book_${lang.code}_${book.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Download,
                                                        contentDescription = "${strings.download} $primaryBookTitle",
                                                        tint = palette.leatherAccent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val pendingDelete = confirmDeleteLang
    if (pendingDelete != null) {
        val pendingName = strings.bibleLanguageName(pendingDelete)
        LiquidGlassDialog(
            onDismissRequest = { confirmDeleteLang = null },
            tokens = tokens
        ) {
            Text(
                text = strings.deletePackDialogTitle(pendingName),
                fontFamily = headerFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = tokens.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = strings.deletePackDialogBody(pendingName),
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.textSecondary
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { confirmDeleteLang = null }) {
                    Text(strings.keepButton, color = tokens.textSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onDeleteLanguage(pendingDelete)
                        confirmDeleteLang = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    return NepaliFormatUtils.formatBytes(bytes, SystemLocaleManager.currentLanguage)
}
