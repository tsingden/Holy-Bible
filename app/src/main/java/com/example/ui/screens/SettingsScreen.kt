package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleLanguage
import com.example.data.model.BiblePreferencesState
import com.example.data.model.PageTurnSoundStyle
import com.example.data.model.ReadingMode
import com.example.data.model.SystemLanguage
import com.example.data.model.ThemePreset
import com.example.ui.components.CopyrightAndCreditsSection
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.NepaliFormatUtils
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    preferences: BiblePreferencesState,
    audioState: AudioPlaybackState,
    totalStorageBytes: Long,
    onSetDarkMode: (Boolean) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onSetPageSound: (Boolean) -> Unit,
    onSetPageSoundStyle: (PageTurnSoundStyle) -> Unit = {},
    onSetPageSoundVolume: (Float) -> Unit = {},
    onPreviewPageSound: (PageTurnSoundStyle) -> Unit = {},
    onTestForwardSound: () -> Unit,
    onTestBackwardSound: () -> Unit,
    onSetReadingMode: (ReadingMode) -> Unit,
    onOpenLanguageSelector: () -> Unit,
    onOpenDownloads: () -> Unit,
    onSetAudioLanguage: (AudioLanguage) -> Unit,
    onSetAudioSpeed: (Float) -> Unit,
    onApplyThemePreset: (ThemePreset) -> Unit = {},
    onOpenThemeStudio: () -> Unit = {},
    onSetSystemLanguage: (SystemLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalBiblePalette.current
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val systemLanguage = LocalSystemLanguage.current
    val headerFont = if (systemLanguage == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily
    val context = LocalContext.current
    val currentTheme = preferences.themeConfig

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .testTag("settings_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = strings.settingsTitle,
                    fontFamily = headerFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    color = tokens.textPrimary
                )
            }

            // 0. System Language Selector (Settings -> System Language)
            item {
                SettingsCard(title = strings.systemLanguageCardTitle, icon = Icons.Filled.Language) {
                    Text(
                        text = strings.systemLanguageSubtitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = strings.systemLanguageHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemLanguage.entries.forEach { lang ->
                            val isSelected = preferences.systemLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    tokens.accentColor.copy(alpha = 0.2f)
                                } else {
                                    tokens.glassSurface.copy(alpha = 0.6f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.6.dp else 0.8.dp,
                                    color = if (isSelected) tokens.accentColor else tokens.borderTop
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSetSystemLanguage(lang) }
                                    .testTag("system_language_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lang.nativeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = if (isSelected) tokens.accentColor else tokens.textPrimary
                                        )
                                        Text(
                                            text = lang.displayName,
                                            fontSize = 11.5.sp,
                                            color = tokens.textSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = if (isSelected) strings.selectedDesc else lang.nativeName,
                                        tint = if (isSelected) tokens.accentColor else tokens.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. iOS Liquid Glass & Custom Theme Studio
            item {
                SettingsCard(title = strings.themesCardTitle, icon = Icons.Filled.Palette) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.themePresetName(currentTheme.preset),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = tokens.accentColor
                            )
                            Text(
                                text = strings.themeSummaryStats(
                                    (currentTheme.transparencyLevel * 100).roundToInt(),
                                    currentTheme.blurIntensity.roundToInt(),
                                    currentTheme.fontStyle.label
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = tokens.accentColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = strings.readingBgName(currentTheme.readingPageBackground),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick 1-Tap Switcher Across All Themes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePreset.entries.forEach { preset ->
                            val isSelected = currentTheme.preset == preset
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    tokens.accentColor.copy(alpha = 0.22f)
                                } else {
                                    tokens.glassSurface
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.6.dp else 0.8.dp,
                                    color = if (isSelected) tokens.accentColor else tokens.borderTop
                                ),
                                modifier = Modifier
                                    .width(114.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onApplyThemePreset(preset) }
                                    .testTag("quick_theme_${preset.name.lowercase()}")
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color(preset.defaultBackgroundArgb),
                                                            Color(preset.defaultSecondaryBgArgb)
                                                        )
                                                    )
                                                )
                                                .border(0.8.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.defaultAccentArgb))
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = strings.themePresetName(preset),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = tokens.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LiquidGlassButton(
                        onClick = onOpenThemeStudio,
                        tokens = tokens,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_theme_studio_button")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.customizeThemeButton, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }

            // 2. Quick Night Study Dark Mode Toggle
            item {
                SettingsCard(title = strings.brightnessCardTitle, icon = Icons.Filled.DarkMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.nightDarkModeLabel,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = if (preferences.darkMode) strings.nightDarkModeOnSub else strings.nightDarkModeOffSub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.textSecondary
                            )
                        }
                        Switch(
                            checked = preferences.darkMode,
                            onCheckedChange = onSetDarkMode,
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }
                }
            }

            // 3. Languages & Offline Downloads
            item {
                SettingsCard(title = strings.languagesCardTitle, icon = Icons.Filled.Translate) {
                    Text(
                        text = strings.activePairLabel(
                            preferences.primaryLanguage.nativeName,
                            preferences.secondaryLanguage.nativeName
                        ),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = strings.offlineStorageUsedLabel(
                            NepaliFormatUtils.formatBytes(totalStorageBytes, systemLanguage)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenLanguageSelector,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_choose_languages_button")
                        ) {
                            Icon(Icons.Filled.Translate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.languagesButton, fontSize = 12.sp)
                        }
                        LiquidGlassButton(
                            onClick = onOpenDownloads,
                            tokens = tokens,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_open_downloads_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.downloadsButton, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4. Layout & Font Size
            item {
                SettingsCard(title = strings.typographyCardTitle, icon = Icons.Filled.MenuBook) {
                    Text(
                        text = strings.readingModeLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadingMode.entries.take(2).forEach { mode ->
                            FilterChip(
                                selected = preferences.readingMode == mode,
                                onClick = { onSetReadingMode(mode) },
                                label = { Text(strings.readingModeName(mode), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadingMode.entries.drop(2).forEach { mode ->
                            val dynamicLabel = when (mode) {
                                ReadingMode.SINGLE_PRIMARY -> strings.singleLanguageOnly(
                                    strings.bibleLanguageName(preferences.primaryLanguage)
                                )
                                ReadingMode.SINGLE_SECONDARY -> strings.singleLanguageOnly(
                                    strings.bibleLanguageName(preferences.secondaryLanguage)
                                )
                                else -> strings.readingModeName(mode)
                            }
                            FilterChip(
                                selected = preferences.readingMode == mode,
                                onClick = { onSetReadingMode(mode) },
                                label = { Text(dynamicLabel, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = tokens.borderTop.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.scriptureFontSizeLabel,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "${NepaliFormatUtils.formatNumber(preferences.fontSizeSp.roundToInt(), systemLanguage)} sp",
                            color = tokens.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = preferences.fontSizeSp,
                        onValueChange = onSetFontSize,
                        valueRange = 13f..24f,
                        steps = 10,
                        modifier = Modifier.testTag("settings_font_slider")
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.paperBackground,
                        border = BorderStroke(0.8.dp, tokens.borderTop),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "In the beginning God created the heaven and the earth.",
                                fontFamily = BibleLanguage.ENGLISH.fontFamily(tokens.fontStyle),
                                fontSize = preferences.fontSizeSp.sp,
                                lineHeight = (preferences.fontSizeSp * 1.38f).sp,
                                color = palette.inkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "आदिमा परमेश्‍वरले आकाशमण्डल र पृथ्वी सृष्‍टि गर्नुभयो ।",
                                fontFamily = BibleLanguage.NEPALI.fontFamily(tokens.fontStyle),
                                fontSize = (preferences.fontSizeSp * 0.95f).sp,
                                lineHeight = (preferences.fontSizeSp * 1.38f).sp,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }
            }

            // 5. Page-Turning Sound Studio & Customization
            item {
                var lastAudibleStyle by remember {
                    mutableStateOf(
                        if (preferences.pageSoundStyle.isAudible && preferences.pageSoundStyle != PageTurnSoundStyle.RANDOM) {
                            preferences.pageSoundStyle
                        } else {
                            PageTurnSoundStyle.REALISTIC_BIBLE
                        }
                    )
                }
                if (preferences.pageSoundStyle.isAudible && preferences.pageSoundStyle != PageTurnSoundStyle.RANDOM) {
                    lastAudibleStyle = preferences.pageSoundStyle
                }

                val effectiveAudible = preferences.pageSoundEnabled && preferences.pageSoundStyle.isAudible
                val volumePercent = (preferences.pageSoundVolume * 100f).roundToInt()

                SettingsCard(title = strings.pageSoundCardTitle, icon = Icons.AutoMirrored.Filled.VolumeUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.pageSoundToggleTitle,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = when {
                                    !preferences.pageSoundEnabled -> strings.pageSoundMuted
                                    !preferences.pageSoundStyle.isAudible -> strings.pageSoundSilentMode
                                    else -> strings.pageSoundActiveStatus(
                                        strings.pageSoundName(preferences.pageSoundStyle),
                                        volumePercent
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.textSecondary
                            )
                        }
                        Switch(
                            checked = preferences.pageSoundEnabled,
                            onCheckedChange = onSetPageSound,
                            modifier = Modifier.testTag("settings_sound_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Random Sound Mode Quick Switch
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (preferences.pageSoundEnabled && preferences.pageSoundStyle == PageTurnSoundStyle.RANDOM) {
                            tokens.accentColor.copy(alpha = 0.16f)
                        } else {
                            tokens.glassSurface.copy(alpha = 0.65f)
                        },
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = if (preferences.pageSoundEnabled && preferences.pageSoundStyle == PageTurnSoundStyle.RANDOM) {
                                tokens.accentColor
                            } else {
                                tokens.borderTop
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = strings.randomSoundModeTitle,
                                    tint = tokens.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.randomSoundModeTitle,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        text = strings.randomSoundModeSub,
                                        fontSize = 11.5.sp,
                                        color = tokens.textSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = preferences.pageSoundEnabled && preferences.pageSoundStyle == PageTurnSoundStyle.RANDOM,
                                onCheckedChange = { useRandom ->
                                    if (useRandom) {
                                        onSetPageSoundStyle(PageTurnSoundStyle.RANDOM)
                                    } else {
                                        onSetPageSoundStyle(lastAudibleStyle)
                                    }
                                },
                                modifier = Modifier.testTag("settings_random_sound_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Volume Control Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.pageSoundVolumeTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = NepaliFormatUtils.formatPercent(volumePercent, systemLanguage),
                            fontWeight = FontWeight.Bold,
                            color = tokens.accentColor
                        )
                    }
                    Slider(
                        value = preferences.pageSoundVolume,
                        onValueChange = onSetPageSoundVolume,
                        valueRange = 0f..1f,
                        steps = 19,
                        enabled = effectiveAudible,
                        modifier = Modifier.testTag("settings_sound_volume_slider")
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = tokens.borderTop.copy(alpha = 0.5f)
                    )

                    Text(
                        text = strings.choosePageSoundTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = strings.choosePageSoundSub,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PageTurnSoundStyle.entries.forEach { style ->
                            val isSelected = preferences.pageSoundStyle == style
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    tokens.accentColor.copy(alpha = 0.18f)
                                } else {
                                    tokens.glassSurface.copy(alpha = 0.55f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) tokens.accentColor else tokens.borderTop
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSetPageSoundStyle(style) }
                                    .testTag("sound_style_${style.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isSelected -> Icons.Filled.CheckCircle
                                                style == PageTurnSoundStyle.RANDOM -> Icons.Filled.Shuffle
                                                !style.isAudible -> Icons.AutoMirrored.Filled.VolumeOff
                                                else -> Icons.Filled.RadioButtonUnchecked
                                            },
                                            contentDescription = if (isSelected) strings.selectedDesc else strings.pageSoundName(style),
                                            tint = if (isSelected) tokens.accentColor else tokens.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = strings.pageSoundName(style),
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = tokens.textPrimary
                                            )
                                            Text(
                                                text = strings.pageSoundDescription(style),
                                                fontSize = 11.5.sp,
                                                color = tokens.textSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (style.isAudible) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = tokens.accentColor.copy(alpha = 0.14f),
                                            border = BorderStroke(0.8.dp, tokens.accentColor.copy(alpha = 0.45f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onPreviewPageSound(style) }
                                                .testTag("preview_sound_${style.name.lowercase()}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "${strings.previewButton} ${strings.pageSoundName(style)}",
                                                    tint = tokens.accentColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = strings.previewButton,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = tokens.accentColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestForwardSound,
                            enabled = effectiveAudible,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_forward_sound_button")
                        ) {
                            Text(strings.testForwardButton, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onTestBackwardSound,
                            enabled = effectiveAudible,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_backward_sound_button")
                        ) {
                            Text(strings.testBackwardButton, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 6. Audio Bible Settings
            item {
                SettingsCard(title = strings.audioNarrationCardTitle, icon = Icons.Filled.Headphones) {
                    Text(
                        text = strings.narrationLanguageLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AudioLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = preferences.audioLanguage == lang,
                                onClick = { onSetAudioLanguage(lang) },
                                label = { Text(strings.audioLanguageName(lang), fontSize = 11.sp, maxLines = 1) },
                                modifier = Modifier.testTag("settings_audio_lang_${lang.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = strings.speechSpeedLabel, fontWeight = FontWeight.SemiBold, color = tokens.textPrimary)
                        Text(
                            text = NepaliFormatUtils.formatSpeed(
                                (preferences.audioSpeed * 100).roundToInt() / 100f,
                                systemLanguage
                            ),
                            color = tokens.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = preferences.audioSpeed,
                        onValueChange = onSetAudioSpeed,
                        valueRange = 0.7f..1.5f,
                        steps = 7
                    )

                    Text(
                        text = strings.deviceVoicesStatus(
                            audioState.isEnglishAvailable,
                            audioState.isNepaliAvailable,
                            audioState.isKoreanAvailable
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                            }
                        }
                    ) {
                        Text(strings.openAndroidTtsSettings)
                    }
                }
            }

            // 7. Copyright & Credits (Single Combined Expandable Section)
            item(key = "copyright_credits_section") {
                CopyrightAndCreditsSection(
                    onOpenUrl = { url ->
                        val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                            url
                        } else {
                            "https://$url"
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(intent)
                        }.onFailure {
                            Toast.makeText(context, strings.openSourceLinkError, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val sysLang = LocalSystemLanguage.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(20.dp),
        tokens = tokens,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.accentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = tokens.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

