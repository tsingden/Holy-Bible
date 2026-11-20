package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleLanguage
import com.example.data.model.SearchResult
import com.example.data.model.SystemLanguage
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
fun SearchScreen(
    query: String,
    testamentFilter: String?,
    languageFilter: BibleLanguage?,
    results: List<SearchResult>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onLanguageFilterChange: (BibleLanguage?) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = strings.searchScriptureTitle,
                fontFamily = headerFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = tokens.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = strings.clearSearchDesc)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bible_search_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = testamentFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text(strings.filterAllBible) }
                )
                FilterChip(
                    selected = testamentFilter == "OT",
                    onClick = { onFilterChange("OT") },
                    label = { Text(strings.filterOldShort) }
                )
                FilterChip(
                    selected = testamentFilter == "NT",
                    onClick = { onFilterChange("NT") },
                    label = { Text(strings.filterNewShort) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = languageFilter == null,
                    onClick = { onLanguageFilterChange(null) },
                    label = { Text(strings.filterAllLanguages, fontSize = 12.sp) }
                )
                BibleLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = languageFilter == lang,
                        onClick = { onLanguageFilterChange(lang) },
                        label = { Text(strings.bibleLanguageName(lang), fontSize = 12.sp) },
                        modifier = Modifier.testTag("search_lang_${lang.code}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                query.trim().length < 2 -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = palette.goldAccent,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = strings.searchPromptEmpty,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = palette.leatherAccent)
                    }
                }
                results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.searchNoResults(query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Text(
                        text = strings.searchMatchedCount(results.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results, key = { "${it.bookId}:${it.chapter}:${it.verse}:${it.matchedLanguage.code}" }) { item ->
                            LiquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                tokens = tokens,
                                onClick = { onResultClick(item) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_result_${item.bookId}_${item.chapter}_${item.verse}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val refText = if (sysLang == SystemLanguage.NEPALI) {
                                            "${item.bookNameNe} ${NepaliFormatUtils.formatNumber(item.chapter, sysLang)}:${NepaliFormatUtils.formatNumber(item.verse, sysLang)} • ${item.bookNameEn} ${item.chapter}:${item.verse}"
                                        } else {
                                            "${item.bookNameEn} ${item.chapter}:${item.verse} • ${item.bookNameNe} ${DevanagariNumberUtils.toDevanagari(item.chapter)}:${DevanagariNumberUtils.toDevanagari(item.verse)}"
                                        }
                                        Text(
                                            text = refText,
                                            fontFamily = headerFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = tokens.accentColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = tokens.accentColor.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = strings.bibleLanguageName(item.matchedLanguage),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.accentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val matchedText = when (item.matchedLanguage) {
                                        BibleLanguage.ENGLISH -> item.textEn
                                        BibleLanguage.NEPALI -> item.textNe
                                        BibleLanguage.ONCV -> item.textOncv
                                        BibleLanguage.KOREAN -> item.textKo
                                    }

                                    if (matchedText.isNotBlank()) {
                                        val isRtl = item.matchedLanguage.isRtl
                                        CompositionLocalProvider(
                                            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                                        ) {
                                            Text(
                                                text = matchedText,
                                                fontFamily = item.matchedLanguage.fontFamily(tokens.fontStyle),
                                                fontSize = 15.5.sp,
                                                style = TextStyle(
                                                    textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr,
                                                    textAlign = if (isRtl) TextAlign.Right else TextAlign.Start,
                                                    lineHeight = 22.sp
                                                ),
                                                color = tokens.textPrimary,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    val fallbackNe = item.textNe.ifBlank { item.textOncv }
                                    if (item.matchedLanguage != BibleLanguage.ENGLISH && item.textEn.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.textEn,
                                            fontFamily = tokens.fontStyle.englishFontFamily(),
                                            fontSize = 14.sp,
                                            lineHeight = 19.sp,
                                            color = tokens.textSecondary
                                        )
                                    } else if (item.matchedLanguage == BibleLanguage.ENGLISH && fallbackNe.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = fallbackNe,
                                            fontFamily = NotoSerifDevanagariFamily,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
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
