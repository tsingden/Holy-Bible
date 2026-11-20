package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.db.BookmarkEntity
import com.example.data.db.HighlightEntity
import com.example.data.db.NoteEntity
import com.example.data.db.ReadingHistoryEntity
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.model.HighlightColor
import com.example.data.model.SystemLanguage
import com.example.ui.components.NoteEditorDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.DevanagariNumberUtils
import com.example.util.NepaliFormatUtils

@Composable
fun LibraryScreen(
    bookmarks: List<BookmarkEntity>,
    highlights: List<HighlightEntity>,
    notes: List<NoteEntity>,
    history: List<ReadingHistoryEntity>,
    onOpenVerse: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onClearHighlight: (HighlightEntity) -> Unit,
    onUpdateNote: (NoteEntity, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
        ) {
            Text(
                text = strings.studyLibraryTitle,
                fontFamily = headerFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = tokens.textPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = tokens.accentColor
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(strings.tabBookmarks(bookmarks.size)) },
                    modifier = Modifier.testTag("tab_bookmarks")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(strings.tabHighlights(highlights.size)) },
                    modifier = Modifier.testTag("tab_highlights")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(strings.tabNotes(notes.size)) },
                    modifier = Modifier.testTag("tab_notes")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(strings.tabHistory(history.size)) },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            when (selectedTab) {
                0 -> {
                    if (bookmarks.isEmpty()) {
                        EmptyLibraryState(
                            title = strings.noBookmarksTitle,
                            subtitle = strings.noBookmarksSub
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(bookmarks, key = { it.verseKey }) { item ->
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    tokens = tokens,
                                    onClick = { onOpenVerse(item.bookId, item.chapter, item.verse) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Bookmark,
                                                    contentDescription = null,
                                                    tint = tokens.accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                val refTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                    "${item.bookNameNe} ${NepaliFormatUtils.formatNumber(item.chapter, sysLang)}:${NepaliFormatUtils.formatNumber(item.verse, sysLang)} • ${item.bookNameEn}"
                                                } else {
                                                    "${item.bookNameEn} ${item.chapter}:${item.verse} • ${item.bookNameNe}"
                                                }
                                                Text(
                                                    text = refTitle,
                                                    fontFamily = headerFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = tokens.accentColor
                                                )
                                            }
                                            IconButton(
                                                onClick = { onRemoveBookmark(item.verseKey) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = strings.removeBookmarkDesc,
                                                    tint = tokens.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.previewEn,
                                            fontFamily = tokens.fontStyle.englishFontFamily(),
                                            fontSize = 15.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = tokens.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.previewNe,
                                            fontFamily = NotoSerifDevanagariFamily,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = tokens.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (highlights.isEmpty()) {
                        EmptyLibraryState(
                            title = strings.noHighlightsTitle,
                            subtitle = strings.noHighlightsSub
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(highlights, key = { it.verseKey }) { item ->
                                val swatchColor = when (HighlightColor.fromKey(item.colorKey)) {
                                    HighlightColor.GOLD -> Color(0xFFF6D56A)
                                    HighlightColor.ROSE -> Color(0xFFF2A7B5)
                                    HighlightColor.SAGE -> Color(0xFF9FD69B)
                                    HighlightColor.SKY -> Color(0xFF8AC4EB)
                                    HighlightColor.NONE -> Color.Gray
                                }
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    tokens = tokens,
                                    onClick = { onOpenVerse(item.bookId, item.chapter, item.verse) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(swatchColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val refTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                    "${item.bookNameNe} ${NepaliFormatUtils.formatNumber(item.chapter, sysLang)}:${NepaliFormatUtils.formatNumber(item.verse, sysLang)}"
                                                } else {
                                                    "${item.bookNameEn} ${item.chapter}:${item.verse}"
                                                }
                                                Text(
                                                    text = refTitle,
                                                    fontFamily = headerFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = tokens.accentColor
                                                )
                                            }
                                            IconButton(
                                                onClick = { onClearHighlight(item) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = strings.clearHighlightDesc,
                                                    tint = tokens.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.previewEn,
                                            fontFamily = tokens.fontStyle.englishFontFamily(),
                                            fontSize = 15.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = tokens.textPrimary
                                        )
                                        Text(
                                            text = item.previewNe,
                                            fontFamily = NotoSerifDevanagariFamily,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = tokens.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (notes.isEmpty()) {
                        EmptyLibraryState(
                            title = strings.noNotesTitle,
                            subtitle = strings.noNotesSub
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notes, key = { it.verseKey }) { item ->
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    tokens = tokens,
                                    onClick = { onOpenVerse(item.bookId, item.chapter, item.verse) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val refTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                "${item.bookNameNe} ${NepaliFormatUtils.formatNumber(item.chapter, sysLang)}:${NepaliFormatUtils.formatNumber(item.verse, sysLang)} • ${item.bookNameEn}"
                                            } else {
                                                "${item.bookNameEn} ${item.chapter}:${item.verse} • ${item.bookNameNe}"
                                            }
                                            Text(
                                                text = refTitle,
                                                fontFamily = headerFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = tokens.accentColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row {
                                                IconButton(
                                                    onClick = { editingNote = item },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = strings.editNoteDesc,
                                                        tint = tokens.textSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteNote(item.verseKey) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = strings.deleteNoteDesc,
                                                        tint = tokens.textSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.noteText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = tokens.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "“${item.versePreviewEn}”",
                                            fontFamily = tokens.fontStyle.englishFontFamily(),
                                            fontSize = 13.5.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = tokens.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    if (history.isEmpty()) {
                        EmptyLibraryState(
                            title = strings.noHistoryTitle,
                            subtitle = strings.noHistorySub
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = onClearHistory) {
                                    Text(strings.clearHistory)
                                }
                            }
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(history, key = { it.chapterKey }) { item ->
                                    LiquidGlassSurface(
                                        shape = RoundedCornerShape(14.dp),
                                        tokens = tokens,
                                        onClick = { onOpenVerse(item.bookId, item.chapter, item.verse) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.History,
                                                    contentDescription = null,
                                                    tint = palette.goldAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    val primaryTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                        "${item.bookNameNe} ${DevanagariNumberUtils.toDevanagari(item.chapter)}"
                                                    } else {
                                                        "${item.bookNameEn} ${item.chapter}"
                                                    }
                                                    val secondaryTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                        "${item.bookNameEn} ${item.chapter}"
                                                    } else {
                                                        "${item.bookNameNe} ${DevanagariNumberUtils.toDevanagari(item.chapter)}"
                                                    }
                                                    Text(
                                                        text = primaryTitle,
                                                        fontFamily = headerFont,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.5.sp,
                                                        color = tokens.textPrimary
                                                    )
                                                    Text(
                                                        text = secondaryTitle,
                                                        fontFamily = if (sysLang == SystemLanguage.NEPALI) PlayfairDisplayFamily else NotoSerifDevanagariFamily,
                                                        fontSize = 13.5.sp,
                                                        color = tokens.textSecondary
                                                    )
                                                }
                                            }
                                            Text(
                                                text = NepaliFormatUtils.formatDateTime(item.visitedAt, sysLang),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.textSecondary
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

    val noteToEdit = editingNote
    if (noteToEdit != null) {
        val refBookName = if (sysLang == SystemLanguage.NEPALI) noteToEdit.bookNameNe else noteToEdit.bookNameEn
        NoteEditorDialog(
            referenceTitle = "$refBookName ${NepaliFormatUtils.formatNumber(noteToEdit.chapter, sysLang)}:${NepaliFormatUtils.formatNumber(noteToEdit.verse, sysLang)}",
            initialNote = noteToEdit.noteText,
            onSave = { updated ->
                onUpdateNote(noteToEdit, updated)
                editingNote = null
            },
            onDelete = {
                onDeleteNote(noteToEdit.verseKey)
                editingNote = null
            },
            onDismiss = { editingNote = null }
        )
    }
}

@Composable
private fun EmptyLibraryState(title: String, subtitle: String) {
    val palette = LocalBiblePalette.current
    val sysLang = LocalSystemLanguage.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = palette.goldAccent,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontFamily = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
