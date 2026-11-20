package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.DevanagariNumberUtils
import com.example.util.NepaliFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScripturePickerSheet(
    books: List<BibleBook>,
    initialBookId: Int,
    initialChapter: Int,
    initialTestamentFilter: String? = null,
    primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
    loadChapterVerseCount: suspend (Int, Int) -> Int,
    onSelectLocation: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember { mutableIntStateOf(0) } // 0 = Book, 1 = Chapter, 2 = Verse
    var testamentFilter by remember { mutableStateOf(initialTestamentFilter) }
    var bookSearch by remember { mutableStateOf("") }
    var chosenBook by remember {
        mutableStateOf(books.firstOrNull { it.id == initialBookId } ?: books.firstOrNull())
    }
    var chosenChapter by remember { mutableIntStateOf(initialChapter) }
    var verseCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(step, chosenBook?.id, chosenChapter) {
        if (step == 2 && chosenBook != null) {
            verseCount = loadChapterVerseCount(chosenBook!!.id, chosenChapter)
        }
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
                .fillMaxHeight(0.84f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (step > 0) {
                        IconButton(
                            onClick = { step -= 1 },
                            modifier = Modifier.testTag("picker_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = tokens.textPrimary)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = tokens.accentColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Column {
                        val bookLabel = chosenBook?.let { b ->
                            if (sysLang == SystemLanguage.NEPALI) b.nameNe else b.nameEn
                        } ?: ""
                        Text(
                            text = when (step) {
                                0 -> strings.selectBookTitle
                                1 -> if (sysLang == SystemLanguage.NEPALI) {
                                    "${chosenBook?.nameNe ?: ""} • ${chosenBook?.nameFor(primaryLanguage) ?: ""}"
                                } else {
                                    "${chosenBook?.nameEn ?: ""} • ${chosenBook?.nameFor(primaryLanguage) ?: ""}"
                                }
                                else -> strings.selectVerseTitle(bookLabel, chosenChapter)
                            },
                            fontFamily = headerFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = tokens.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (step) {
                                0 -> strings.booksCountSubtitle
                                1 -> strings.chooseChapterSubtitle(chosenBook?.chapterCount ?: 0)
                                else -> strings.tapVerseOrChapterSubtitle
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.textSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = strings.closePickerDesc, tint = tokens.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (step) {
                0 -> {
                    OutlinedTextField(
                        value = bookSearch,
                        onValueChange = { bookSearch = it },
                        placeholder = { Text(strings.searchBookPlaceholder, color = tokens.textSecondary) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = tokens.accentColor) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.textPrimary,
                            unfocusedTextColor = tokens.textPrimary,
                            focusedBorderColor = tokens.accentColor,
                            unfocusedBorderColor = tokens.borderTop
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = testamentFilter == null,
                            onClick = { testamentFilter = null },
                            label = { Text(strings.filterAll66) }
                        )
                        FilterChip(
                            selected = testamentFilter == "OT",
                            onClick = { testamentFilter = "OT" },
                            label = { Text(strings.filterOld39) }
                        )
                        FilterChip(
                            selected = testamentFilter == "NT",
                            onClick = { testamentFilter = "NT" },
                            label = { Text(strings.filterNew27) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = remember(books, testamentFilter, bookSearch) {
                        val q = bookSearch.trim().lowercase()
                        books.filter { b ->
                            val matchesTestament = testamentFilter == null || b.testament == testamentFilter
                            val matchesQuery = q.isEmpty() ||
                                b.nameEn.lowercase().contains(q) ||
                                b.nameNe.contains(q) ||
                                b.nameKo.contains(q) ||
                                b.abbrev.lowercase().contains(q)
                            matchesTestament && matchesQuery
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { it.id }) { book ->
                            val isCurrent = book.id == initialBookId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isCurrent -> tokens.accentColor.copy(alpha = 0.22f)
                                    tokens.isGlassActive -> tokens.glassSurface
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                },
                                border = if (tokens.isGlassActive) {
                                    BorderStroke(
                                        width = if (isCurrent) 1.3.dp else 0.8.dp,
                                        color = if (isCurrent) tokens.accentColor else tokens.borderTop
                                    )
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        chosenBook = book
                                        step = 1
                                    }
                                    .testTag("book_item_${book.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(tokens.accentColor.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = NepaliFormatUtils.formatNumber(book.id, sysLang),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.accentColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (sysLang == SystemLanguage.NEPALI) book.nameNe else book.nameEn,
                                                fontFamily = headerFont,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                color = tokens.textPrimary
                                            )
                                            val secondaryTitle = if (sysLang == SystemLanguage.NEPALI) {
                                                if (!primaryLanguage.usesDevanagari && primaryLanguage.supportsBook(book.id)) {
                                                    book.nameFor(primaryLanguage)
                                                } else {
                                                    book.nameEn
                                                }
                                            } else {
                                                if (primaryLanguage != BibleLanguage.ENGLISH && primaryLanguage.supportsBook(book.id)) {
                                                    book.nameFor(primaryLanguage)
                                                } else {
                                                    book.nameNe
                                                }
                                            }
                                            val secondaryFont = if (sysLang == SystemLanguage.NEPALI) {
                                                if (!primaryLanguage.usesDevanagari && primaryLanguage.supportsBook(book.id)) {
                                                    primaryLanguage.fontFamily()
                                                } else {
                                                    PlayfairDisplayFamily
                                                }
                                            } else {
                                                if (primaryLanguage != BibleLanguage.ENGLISH && primaryLanguage.supportsBook(book.id)) {
                                                    primaryLanguage.fontFamily()
                                                } else {
                                                    NotoSerifDevanagariFamily
                                                }
                                            }
                                            Text(
                                                text = secondaryTitle,
                                                fontFamily = secondaryFont,
                                                fontSize = 14.sp,
                                                color = tokens.textSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = strings.chaptersShort(book.chapterCount),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = tokens.accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val book = chosenBook
                    if (book != null) {
                        val chapters = (1..book.chapterCount).toList()
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 62.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chapters) { ch ->
                                val isSelected = book.id == initialBookId && ch == initialChapter
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> tokens.accentColor
                                                tokens.isGlassActive -> tokens.glassSurface
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                            }
                                        )
                                        .border(
                                            width = if (tokens.isGlassActive) 0.9.dp else 0.7.dp,
                                            color = if (tokens.isGlassActive) tokens.borderTop else palette.goldAccent.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            chosenChapter = ch
                                            step = 2
                                        }
                                        .testTag("chapter_cell_$ch"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (sysLang == SystemLanguage.NEPALI) {
                                                DevanagariNumberUtils.toDevanagari(ch)
                                            } else {
                                                "$ch"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) tokens.onAccentColor else tokens.textPrimary
                                        )
                                        Text(
                                            text = if (sysLang == SystemLanguage.NEPALI) {
                                                "$ch"
                                            } else {
                                                DevanagariNumberUtils.toDevanagari(ch)
                                            },
                                            fontSize = 11.sp,
                                            color = if (isSelected) {
                                                tokens.onAccentColor.copy(alpha = 0.85f)
                                            } else {
                                                tokens.textSecondary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    val book = chosenBook
                    if (book != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    onSelectLocation(book.id, chosenChapter, null)
                                },
                                modifier = Modifier.testTag("open_whole_chapter_button")
                            ) {
                                Text(strings.openChapterFromVerse1(chosenChapter), color = tokens.accentColor)
                            }
                        }
                        val verses = (1..verseCount.coerceAtLeast(1)).toList()
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 56.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(verses) { v ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (tokens.isGlassActive) tokens.glassSurface
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                        )
                                        .border(
                                            width = if (tokens.isGlassActive) 0.9.dp else 0.5.dp,
                                            color = tokens.borderTop,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            onSelectLocation(book.id, chosenChapter, v)
                                        }
                                        .testTag("verse_cell_$v"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = NepaliFormatUtils.formatNumber(v, sysLang),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = tokens.textPrimary
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
