package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeConfig
import com.example.data.model.ButtonStyleOption
import com.example.data.model.NavBarAppearance
import com.example.data.model.ReadingPageBackground
import com.example.data.model.ScriptureFontStyle
import com.example.data.model.SystemLanguage
import com.example.data.model.ThemePreset
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.GlassSwatchCircle
import com.example.ui.theme.HolyBibleTheme
import com.example.ui.theme.LiquidGlassBackdrop
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalBiblePalette
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.util.NepaliFormatUtils
import kotlin.math.roundToInt

private val BACKGROUND_SWATCHES = listOf(
    "Crystal Sky" to 0xFFE6EFFA,
    "Deep Slate" to 0xFF12151E,
    "True Black" to 0xFF000000,
    "Royal Navy" to 0xFF0B1F3A,
    "Pearl Frost" to 0xFFF4F7FB,
    "Cream Paper" to 0xFFFAF4E6,
    "Forest Mist" to 0xFFEAF4EE,
    "Velvet Espresso" to 0xFF1A140E,
    "Deep Ocean" to 0xFF0D1B2A
)

private val TEXT_SWATCHES = listOf(
    "Obsidian" to 0xFF0F172A,
    "Crisp White" to 0xFFFFFFFF,
    "Soft Ivory" to 0xFFFAF1DF,
    "Warm Sepia" to 0xFF231B15,
    "Slate Ink" to 0xFF334155,
    "Champagne Gold" to 0xFFF5D07A
)

private val ACCENT_SWATCHES = listOf(
    "Apple Blue" to 0xFF0A84FF,
    "Cyan Frost" to 0xFF5AC8FA,
    "Burgundy" to 0xFF5B1826,
    "Antique Gold" to 0xFFD9B25F,
    "Emerald" to 0xFF1E7A55,
    "Royal Indigo" to 0xFF5E5CE6,
    "Rose Coral" to 0xFFE11D48,
    "Warm Amber" to 0xFFD97706
)

@Composable
fun ThemeStudioScreen(
    currentConfig: AppThemeConfig,
    currentFontSizeSp: Float,
    onApplyTheme: (AppThemeConfig, Float) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draftConfig by remember(currentConfig) { mutableStateOf(currentConfig) }
    var draftFontSize by remember(currentFontSizeSp) { mutableFloatStateOf(currentFontSizeSp) }

    BackHandler {
        onCancel()
    }

    val liveTokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Header
        LiquidGlassSurface(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevated = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("theme_studio_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.themeStudioTitle,
                        fontFamily = headerFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = liveTokens.textPrimary
                    )
                    Text(
                        text = strings.themeStudioSubtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = liveTokens.textSecondary
                    )
                }
                TextButton(
                    onClick = {
                        draftConfig = AppThemeConfig.fromPreset(draftConfig.preset)
                        draftFontSize = 16.5f
                    },
                    modifier = Modifier.testTag("theme_reset_preset")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.reset, fontSize = 12.sp)
                }
            }
        }

        // Live Interactive Theme Preview (uses draftConfig directly!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            LiveThemePreviewCard(
                draftConfig = draftConfig,
                draftFontSize = draftFontSize,
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
            )
        }

        // Scrollable Customization Controls
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // All Curated Theme Presets
                item {
                    StudioSectionCard(
                        title = strings.themePresetsHeading(ThemePreset.entries.size),
                        subtitle = strings.activePresetLabel(strings.themePresetName(draftConfig.preset))
                    ) {
                        LazyRow(
                            modifier = Modifier.testTag("theme_presets_row"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(ThemePreset.entries, key = { it.name }) { preset ->
                                val isSelected = draftConfig.preset == preset
                                val bgColor = Color(preset.defaultBackgroundArgb)
                                val secColor = Color(preset.defaultSecondaryBgArgb)
                                val accentColor = Color(preset.defaultAccentArgb)
                                val txtColor = Color(preset.defaultTextArgb)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) liveTokens.accentColor.copy(alpha = 0.16f) else liveTokens.glassSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) liveTokens.accentColor else liveTokens.borderTop
                                    ),
                                    modifier = Modifier
                                        .width(154.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            draftConfig = AppThemeConfig.fromPreset(preset).copy(
                                                fontStyle = draftConfig.fontStyle
                                            )
                                        }
                                        .testTag("theme_preset_${preset.name.lowercase()}")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    Brush.linearGradient(listOf(bgColor, secColor))
                                                )
                                                .border(0.8.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Aa",
                                                    color = txtColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(accentColor)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = strings.themePresetName(preset),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = liveTokens.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = strings.themePresetSubtitle(preset),
                                            fontSize = 10.5.sp,
                                            lineHeight = 14.sp,
                                            color = liveTokens.textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Transparency & Glass Blur Intensity
                item {
                    StudioSectionCard(
                        title = strings.liquidGlassOpticsHeading,
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            strings.hardwareBlurSub
                        } else {
                            strings.compatFrostSub
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.glassOpacityLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text(
                                NepaliFormatUtils.formatPercent((draftConfig.transparencyLevel * 100).roundToInt(), sysLang),
                                fontWeight = FontWeight.Bold,
                                color = liveTokens.accentColor
                            )
                        }
                        Slider(
                            value = draftConfig.transparencyLevel,
                            onValueChange = { draftConfig = draftConfig.copy(transparencyLevel = it) },
                            valueRange = 0.18f..0.95f,
                            modifier = Modifier.testTag("theme_transparency_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.glassBlurLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text(
                                "${NepaliFormatUtils.formatNumber(draftConfig.blurIntensity.roundToInt(), sysLang)} dp",
                                fontWeight = FontWeight.Bold,
                                color = liveTokens.accentColor
                            )
                        }
                        Slider(
                            value = draftConfig.blurIntensity,
                            onValueChange = { draftConfig = draftConfig.copy(blurIntensity = it) },
                            valueRange = 0f..32f,
                            modifier = Modifier.testTag("theme_blur_slider")
                        )
                    }
                }

                // 3. Colors (Background, Accent, Text)
                item {
                    StudioSectionCard(
                        title = strings.colorsHeading,
                        subtitle = strings.colorsSub
                    ) {
                        Text(strings.backgroundColorLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            BACKGROUND_SWATCHES.forEach { (name, argb) ->
                                GlassSwatchCircle(
                                    color = Color(argb),
                                    selected = draftConfig.effectiveBackgroundArgb == argb,
                                    label = "bg_$name",
                                    onClick = {
                                        val isDark = Color(argb).red * 0.299f + Color(argb).green * 0.587f + Color(argb).blue * 0.114f < 0.5f
                                        draftConfig = draftConfig.copy(
                                            customBackgroundArgb = argb,
                                            isDarkSurface = isDark
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(strings.accentColorLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ACCENT_SWATCHES.forEach { (name, argb) ->
                                GlassSwatchCircle(
                                    color = Color(argb),
                                    selected = draftConfig.effectiveAccentArgb == argb,
                                    label = "accent_$name",
                                    onClick = {
                                        draftConfig = draftConfig.copy(customAccentArgb = argb)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.textColorLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextButton(onClick = { draftConfig = draftConfig.copy(customTextArgb = null) }) {
                                Text(strings.autoContrastButton, fontSize = 11.5.sp)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TEXT_SWATCHES.forEach { (name, argb) ->
                                GlassSwatchCircle(
                                    color = Color(argb),
                                    selected = draftConfig.customTextArgb == argb,
                                    label = "text_$name",
                                    onClick = {
                                        draftConfig = draftConfig.copy(customTextArgb = argb)
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Button Style & Navigation Bar Appearance
                item {
                    StudioSectionCard(
                        title = strings.controlsNavHeading,
                        subtitle = strings.controlsNavSub
                    ) {
                        Text(strings.buttonStyleLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ButtonStyleOption.entries.forEach { style ->
                                FilterChip(
                                    selected = draftConfig.buttonStyle == style,
                                    onClick = { draftConfig = draftConfig.copy(buttonStyle = style) },
                                    label = { Text(strings.buttonStyleName(style), fontSize = 12.sp) },
                                    modifier = Modifier.testTag("button_style_${style.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(strings.navBarAppearanceLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NavBarAppearance.entries.forEach { nav ->
                                FilterChip(
                                    selected = draftConfig.navBarAppearance == nav,
                                    onClick = { draftConfig = draftConfig.copy(navBarAppearance = nav) },
                                    label = { Text(strings.navBarAppearanceName(nav), fontSize = 12.sp) },
                                    modifier = Modifier.testTag("nav_style_${nav.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // 5. Font Style, Font Size & Reading Page Background
                item {
                    StudioSectionCard(
                        title = strings.readingPageTypographyHeading,
                        subtitle = strings.readingPageTypographySub
                    ) {
                        Text(strings.readingPageBgLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReadingPageBackground.entries.forEach { bg ->
                                FilterChip(
                                    selected = draftConfig.readingPageBackground == bg,
                                    onClick = { draftConfig = draftConfig.copy(readingPageBackground = bg) },
                                    label = { Text(strings.readingBgName(bg), fontSize = 12.sp) },
                                    modifier = Modifier.testTag("reading_bg_${bg.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(strings.scriptureFontStyleLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScriptureFontStyle.entries.forEach { font ->
                                FilterChip(
                                    selected = draftConfig.fontStyle == font,
                                    onClick = { draftConfig = draftConfig.copy(fontStyle = font) },
                                    label = {
                                        Text(
                                            text = font.label,
                                            fontFamily = font.englishFontFamily(),
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("font_style_${font.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.scriptureFontSizeLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                "${NepaliFormatUtils.formatNumber(draftFontSize.roundToInt(), sysLang)} sp",
                                fontWeight = FontWeight.Bold,
                                color = liveTokens.accentColor
                            )
                        }
                        Slider(
                            value = draftFontSize,
                            onValueChange = { draftFontSize = it },
                            valueRange = 13f..24f,
                            steps = 10,
                            modifier = Modifier.testTag("theme_font_size_slider")
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }

        // Bottom Apply / Cancel Bar
        LiquidGlassSurface(
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            elevated = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("theme_cancel_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.cancel, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onApplyTheme(draftConfig, draftFontSize) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = liveTokens.accentColor,
                        contentColor = liveTokens.onAccentColor
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("theme_apply_button")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.applyThemeButton, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LiveThemePreviewCard(
    draftConfig: AppThemeConfig,
    draftFontSize: Float,
    modifier: Modifier = Modifier
) {
    val sysLang = LocalSystemLanguage.current
    HolyBibleTheme(
        darkTheme = draftConfig.isDarkSurface,
        themeConfig = draftConfig,
        systemLanguage = sysLang
    ) {
        val previewTokens = LocalLiquidGlass.current
        val previewPalette = LocalBiblePalette.current
        val previewStrings = LocalAppStrings.current

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(22.dp))
                .border(1.2.dp, previewTokens.borderTop, RoundedCornerShape(22.dp))
                .testTag("live_theme_preview")
        ) {
            LiquidGlassBackdrop(
                tokens = previewTokens,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mini Glass Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = previewTokens.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = previewStrings.livePreviewPrefix(previewStrings.themePresetName(draftConfig.preset)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = previewTokens.textPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = previewTokens.glassSurfaceElevated
                        ) {
                            Text(
                                text = "EN • NE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = previewTokens.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Mini Reading Page Surface Preview
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = previewPalette.paperBackground,
                        border = BorderStroke(0.8.dp, previewTokens.borderTop),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "John 3:16 • यूहन्ना ३:१६",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = previewPalette.verseNumberInk
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "For God so loved the world, that he gave his only begotten Son...",
                                fontFamily = draftConfig.fontStyle.englishFontFamily(),
                                fontSize = (draftFontSize * 0.78f).sp,
                                lineHeight = (draftFontSize * 1.08f).sp,
                                color = previewPalette.inkPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "किनभने परमेश्‍वरले संसारलाई यस्तो प्रेम गर्नुभयो...",
                                fontFamily = NotoSerifDevanagariFamily,
                                fontSize = (draftFontSize * 0.75f).sp,
                                color = previewPalette.inkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Mini Button + Navigation Bar Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassButton(
                            onClick = {},
                            tokens = previewTokens,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(previewStrings.livePreviewReadButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Mini Nav Bar Preview
                        val navCorner = if (draftConfig.navBarAppearance == NavBarAppearance.FLOATING_GLASS_DOCK) 18.dp else 8.dp
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(navCorner),
                            elevated = true,
                            tokens = previewTokens,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Home, contentDescription = null, tint = previewTokens.accentColor, modifier = Modifier.size(16.dp))
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = previewTokens.textSecondary, modifier = Modifier.size(16.dp))
                                Icon(Icons.Filled.Search, contentDescription = null, tint = previewTokens.textSecondary, modifier = Modifier.size(16.dp))
                                Icon(Icons.Filled.Bookmarks, contentDescription = null, tint = previewTokens.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    subtitle: String,
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
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = tokens.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontFamily = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = tokens.textSecondary
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = tokens.borderTop.copy(alpha = 0.5f)
            )
            content()
        }
    }
}
