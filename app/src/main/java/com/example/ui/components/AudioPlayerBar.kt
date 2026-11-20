package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.model.AudioLanguage
import com.example.data.model.SystemLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalLiquidGlass
import com.example.util.DevanagariNumberUtils
import com.example.util.NepaliFormatUtils

@Composable
fun AudioPlayerBar(
    audioState: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onPrevVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onCycleLanguage: (AudioLanguage) -> Unit,
    onCycleSpeed: (Float) -> Unit,
    onClose: () -> Unit,
    onJumpToPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!audioState.isActiveSession) return
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevated = true,
        tokens = tokens
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onJumpToPlaying() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = strings.audioActiveDesc,
                        tint = tokens.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val neRef = "${audioState.bookNameNe} ${DevanagariNumberUtils.toDevanagari(audioState.chapter)}:${DevanagariNumberUtils.toDevanagari(audioState.currentVerse)}"
                    val enRef = "${audioState.bookNameEn} ${audioState.chapter}:${audioState.currentVerse}"
                    Column {
                        Text(
                            text = if (sysLang == SystemLanguage.NEPALI) neRef else enRef,
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (sysLang == SystemLanguage.NEPALI) enRef else neRef,
                            color = tokens.textSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Speed chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tokens.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val next = when {
                                audioState.speed < 0.9f -> 1.0f
                                audioState.speed < 1.1f -> 1.25f
                                else -> 0.8f
                            }
                            onCycleSpeed(next)
                        }
                        .testTag("audio_speed_chip")
                ) {
                    Text(
                        text = NepaliFormatUtils.formatSpeed(audioState.speed, sysLang),
                        color = tokens.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Language cycle chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tokens.accentColor.copy(alpha = 0.24f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val nextLang = when (audioState.audioLanguage) {
                                AudioLanguage.BILINGUAL -> AudioLanguage.ENGLISH
                                AudioLanguage.ENGLISH -> AudioLanguage.NEPALI
                                AudioLanguage.NEPALI -> AudioLanguage.ONCV
                                AudioLanguage.ONCV -> AudioLanguage.KOREAN
                                AudioLanguage.KOREAN -> AudioLanguage.BILINGUAL
                            }
                            onCycleLanguage(nextLang)
                        }
                        .testTag("audio_lang_chip")
                ) {
                    Text(
                        text = strings.audioLanguageName(audioState.audioLanguage),
                        color = tokens.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevVerse,
                        modifier = Modifier.testTag("audio_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = strings.audioPrevDesc,
                            tint = tokens.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.testTag("audio_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (audioState.isPlaying) strings.audioPauseDesc else strings.audioPlayDesc,
                            tint = tokens.accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(
                        onClick = onNextVerse,
                        modifier = Modifier.testTag("audio_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = strings.audioNextDesc,
                            tint = tokens.textPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("audio_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = strings.audioStopDesc,
                        tint = tokens.textSecondary
                    )
                }
            }

            if (!audioState.statusNote.isNullOrBlank()) {
                Text(
                    text = audioState.statusNote,
                    color = tokens.textSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
