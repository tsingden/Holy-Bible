package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BibleVerse
import com.example.data.model.HighlightColor
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LiquidGlassDialog
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.BibleShareHelper
import com.example.util.NepaliFormatUtils

@Composable
fun VerseActionBar(
    book: BibleBook,
    verse: BibleVerse,
    primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
    secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI,
    isBookmarked: Boolean,
    currentHighlight: HighlightColor,
    existingNote: String?,
    onToggleBookmark: () -> Unit,
    onSelectHighlight: (HighlightColor) -> Unit,
    onOpenNoteDialog: () -> Unit,
    onListenFromHere: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevated = true,
        tokens = tokens
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val chP = NepaliFormatUtils.formatNumber(verse.chapter, if (primaryLanguage.usesDevanagari) SystemLanguage.NEPALI else sysLang)
                    val vP = NepaliFormatUtils.formatNumber(verse.verse, if (primaryLanguage.usesDevanagari) SystemLanguage.NEPALI else sysLang)
                    val chS = NepaliFormatUtils.formatNumber(verse.chapter, if (secondaryLanguage.usesDevanagari) SystemLanguage.NEPALI else sysLang)
                    val vS = NepaliFormatUtils.formatNumber(verse.verse, if (secondaryLanguage.usesDevanagari) SystemLanguage.NEPALI else sysLang)
                    Text(
                        text = "${book.nameFor(primaryLanguage)} $chP:$vP • ${book.nameFor(secondaryLanguage)} $chS:$vS",
                        fontFamily = headerFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = palette.leatherAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!existingNote.isNullOrBlank()) {
                        Text(
                            text = strings.notePrefix(existingNote),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = strings.closeVerseBarDesc)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.highlightLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HighlightSwatch(
                    color = Color(0xFFF6D56A),
                    selected = currentHighlight == HighlightColor.GOLD,
                    label = "Gold",
                    onClick = {
                        onSelectHighlight(
                            if (currentHighlight == HighlightColor.GOLD) HighlightColor.NONE else HighlightColor.GOLD
                        )
                    }
                )
                HighlightSwatch(
                    color = Color(0xFFF2A7B5),
                    selected = currentHighlight == HighlightColor.ROSE,
                    label = "Rose",
                    onClick = {
                        onSelectHighlight(
                            if (currentHighlight == HighlightColor.ROSE) HighlightColor.NONE else HighlightColor.ROSE
                        )
                    }
                )
                HighlightSwatch(
                    color = Color(0xFF9FD69B),
                    selected = currentHighlight == HighlightColor.SAGE,
                    label = "Sage",
                    onClick = {
                        onSelectHighlight(
                            if (currentHighlight == HighlightColor.SAGE) HighlightColor.NONE else HighlightColor.SAGE
                        )
                    }
                )
                HighlightSwatch(
                    color = Color(0xFF8AC4EB),
                    selected = currentHighlight == HighlightColor.SKY,
                    label = "Sky",
                    onClick = {
                        onSelectHighlight(
                            if (currentHighlight == HighlightColor.SKY) HighlightColor.NONE else HighlightColor.SKY
                        )
                    }
                )
                if (currentHighlight != HighlightColor.NONE) {
                    TextButton(onClick = { onSelectHighlight(HighlightColor.NONE) }) {
                        Text(strings.clear, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChipButton(
                    icon = Icons.Filled.PlayCircle,
                    label = strings.listenAction,
                    testTag = "verse_listen_button",
                    onClick = onListenFromHere
                )
                ActionChipButton(
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (isBookmarked) strings.bookmarkedAction else strings.bookmarkAction,
                    testTag = "verse_bookmark_button",
                    onClick = onToggleBookmark
                )
                ActionChipButton(
                    icon = Icons.Filled.EditNote,
                    label = if (existingNote.isNullOrBlank()) strings.addNoteAction else strings.editNoteAction,
                    testTag = "verse_note_button",
                    onClick = onOpenNoteDialog
                )
                ActionChipButton(
                    icon = Icons.Filled.Share,
                    label = strings.whatsappAction,
                    testTag = "verse_whatsapp_button",
                    onClick = {
                        BibleShareHelper.shareToWhatsApp(context, book, verse, primaryLanguage, secondaryLanguage)
                    }
                )
                ActionChipButton(
                    icon = Icons.Filled.Share,
                    label = strings.facebookAction,
                    testTag = "verse_facebook_button",
                    onClick = {
                        BibleShareHelper.shareToFacebook(context, book, verse, primaryLanguage, secondaryLanguage)
                    }
                )
                ActionChipButton(
                    icon = Icons.Filled.ContentCopy,
                    label = strings.copyAction,
                    testTag = "verse_copy_button",
                    onClick = {
                        BibleShareHelper.copyVerse(context, book, verse, primaryLanguage, secondaryLanguage)
                    }
                )
                ActionChipButton(
                    icon = Icons.Filled.Share,
                    label = strings.moreAction,
                    testTag = "verse_share_more_button",
                    onClick = {
                        BibleShareHelper.shareGeneric(context, book, verse, primaryLanguage, secondaryLanguage)
                    }
                )
            }
        }
    }
}

@Composable
private fun HighlightSwatch(
    color: Color,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("highlight_${label.lowercase()}")
    )
}

@Composable
private fun ActionChipButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    val tokens = LocalLiquidGlass.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (tokens.isGlassActive) tokens.glassSurface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = if (tokens.isGlassActive) BorderStroke(0.9.dp, tokens.borderTop) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tokens.accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = tokens.textPrimary
            )
        }
    }
}

@Composable
fun NoteEditorDialog(
    referenceTitle: String,
    initialNote: String,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var noteText by remember(initialNote) { mutableStateOf(initialNote) }
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current

    LiquidGlassDialog(
        onDismissRequest = onDismiss,
        tokens = tokens
    ) {
        Text(
            text = strings.personalNoteTitle(referenceTitle),
            fontFamily = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = tokens.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = {
                Text(
                    strings.notePlaceholder,
                    color = tokens.textSecondary
                )
            },
            minLines = 4,
            maxLines = 8,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = tokens.textPrimary,
                unfocusedTextColor = tokens.textPrimary,
                focusedBorderColor = tokens.accentColor,
                unfocusedBorderColor = tokens.borderTop
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_editor_input")
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onDelete != null && initialNote.isNotBlank()) {
                OutlinedButton(onClick = onDelete) {
                    Text(strings.delete)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = tokens.textSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onSave(noteText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accentColor,
                    contentColor = tokens.onAccentColor
                ),
                modifier = Modifier.testTag("note_save_button")
            ) {
                Text(strings.save)
            }
        }
    }
}
