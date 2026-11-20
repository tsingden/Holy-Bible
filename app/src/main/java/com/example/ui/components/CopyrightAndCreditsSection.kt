package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CopyrightLicenseCategory
import com.example.data.model.SystemLanguage
import com.example.ui.localization.BibleSourceAttribution
import com.example.ui.localization.CreditEntryItem
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalSystemLanguage
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NotoSerifDevanagariFamily
import com.example.ui.theme.PlayfairDisplayFamily

@Composable
fun CopyrightAndCreditsSection(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val sysLang = LocalSystemLanguage.current
    val headerFont = if (sysLang == SystemLanguage.NEPALI) NotoSerifDevanagariFamily else PlayfairDisplayFamily

    var isSectionExpanded by rememberSaveable { mutableStateOf(false) }
    var isBibleCopyrightExpanded by rememberSaveable { mutableStateOf(false) }
    var isTranslationCreditsExpanded by rememberSaveable { mutableStateOf(false) }
    var isAudioMediaCreditsExpanded by rememberSaveable { mutableStateOf(false) }
    var isDeveloperTechCreditsExpanded by rememberSaveable { mutableStateOf(false) }

    LiquidGlassSurface(
        shape = RoundedCornerShape(20.dp),
        tokens = tokens,
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_about_copyright_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Single Main Settings Menu Header: Copyright & Credits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isSectionExpanded = !isSectionExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("copyright_credits_header_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = tokens.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.licensesCardTitle,
                            fontFamily = headerFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = tokens.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.authenticEditionsHeading,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isSectionExpanded) strings.collapseSectionDesc else strings.expandSectionDesc,
                    tint = tokens.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isSectionExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = tokens.borderTop.copy(alpha = 0.5f)
                    )

                    // A. Bible Translation Copyright & Sources
                    ExpandableCreditsSubsection(
                        title = strings.subsectionBibleCopyrightTitle,
                        subtitle = strings.subsectionBibleCopyrightSubtitle,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        expanded = isBibleCopyrightExpanded,
                        onToggle = { isBibleCopyrightExpanded = !isBibleCopyrightExpanded },
                        toggleTag = "subsection_toggle_bible_copyright"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            strings.bibleSourceAttributions.forEach { attr ->
                                BibleCopyrightEntryCard(
                                    attribution = attr,
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        }
                    }

                    // B. Bible Translation Credits
                    ExpandableCreditsSubsection(
                        title = strings.subsectionBibleTranslationCreditsTitle,
                        subtitle = strings.subsectionBibleTranslationCreditsSubtitle,
                        icon = Icons.Filled.Groups,
                        expanded = isTranslationCreditsExpanded,
                        onToggle = { isTranslationCreditsExpanded = !isTranslationCreditsExpanded },
                        toggleTag = "subsection_toggle_translation_credits"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            strings.bibleTranslationCreditItems.forEachIndexed { idx, entry ->
                                CreditEntryCard(
                                    entry = entry,
                                    linkTagPrefix = "credit_link_b_$idx",
                                    cardTag = "credit_card_b_$idx",
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        }
                    }

                    // C. Audio & Media Credits
                    ExpandableCreditsSubsection(
                        title = strings.subsectionAudioMediaCreditsTitle,
                        subtitle = strings.subsectionAudioMediaCreditsSubtitle,
                        icon = Icons.Filled.GraphicEq,
                        expanded = isAudioMediaCreditsExpanded,
                        onToggle = { isAudioMediaCreditsExpanded = !isAudioMediaCreditsExpanded },
                        toggleTag = "subsection_toggle_audio_media_credits"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            strings.audioMediaCreditItems.forEachIndexed { idx, entry ->
                                CreditEntryCard(
                                    entry = entry,
                                    linkTagPrefix = "credit_link_c_$idx",
                                    cardTag = "credit_card_c_$idx",
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        }
                    }

                    // D. Developer & Technology Credits
                    ExpandableCreditsSubsection(
                        title = strings.subsectionDeveloperTechCreditsTitle,
                        subtitle = strings.subsectionDeveloperTechCreditsSubtitle,
                        icon = Icons.Filled.Code,
                        expanded = isDeveloperTechCreditsExpanded,
                        onToggle = { isDeveloperTechCreditsExpanded = !isDeveloperTechCreditsExpanded },
                        toggleTag = "subsection_toggle_developer_tech_credits"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            strings.developerTechCreditItems.forEachIndexed { idx, entry ->
                                CreditEntryCard(
                                    entry = entry,
                                    linkTagPrefix = "credit_link_d_$idx",
                                    cardTag = "credit_card_d_$idx",
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableCreditsSubsection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    toggleTag: String,
    content: @Composable () -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.glassSurface.copy(alpha = if (tokens.isDark) 0.44f else 0.62f),
        border = BorderStroke(
            width = if (expanded) 1.2.dp else 0.85.dp,
            color = if (expanded) tokens.accentColor.copy(alpha = 0.65f) else tokens.borderTop
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag(toggleTag),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tokens.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) strings.collapseSectionDesc else strings.expandSectionDesc,
                    tint = tokens.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 10.dp),
                        color = tokens.borderTop.copy(alpha = 0.45f)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun CreditEntryCard(
    entry: CreditEntryItem,
    linkTagPrefix: String,
    cardTag: String,
    onOpenUrl: (String) -> Unit
) {
    val tokens = LocalLiquidGlass.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.glassSurface.copy(alpha = if (tokens.isDark) 0.56f else 0.78f),
        border = BorderStroke(0.8.dp, tokens.borderTop),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(cardTag)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = entry.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = tokens.textPrimary
            )
            if (entry.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.subtitle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                lineHeight = 18.sp
            )

            if (entry.links.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.links.forEachIndexed { linkIdx, link ->
                        ClickableSourceLinkChip(
                            displayText = "${link.label} • ${link.url}",
                            contentDesc = "${link.label}: ${link.url}",
                            testTag = "${linkTagPrefix}_$linkIdx",
                            onClick = { onOpenUrl(link.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleCopyrightEntryCard(
    attribution: BibleSourceAttribution,
    onOpenUrl: (String) -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val strings = LocalAppStrings.current
    val unverified = strings.copyrightNotVerifiedYet

    fun valueOrUnverified(value: String): String = value.trim().ifBlank { unverified }

    val badgeColor = when (attribution.licenseCategory) {
        CopyrightLicenseCategory.OPEN_LICENSE -> Color(0xFF1B8A5A)
        CopyrightLicenseCategory.PUBLIC_DOMAIN -> tokens.accentColor
        CopyrightLicenseCategory.COPYRIGHTED -> Color(0xFFB45309)
        CopyrightLicenseCategory.UNVERIFIED -> tokens.textSecondary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.glassSurface.copy(alpha = if (tokens.isDark) 0.56f else 0.78f),
        border = BorderStroke(0.85.dp, tokens.borderTop),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("about_version_card_${attribution.versionId.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = valueOrUnverified(attribution.title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = tokens.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = if (tokens.isDark) 0.24f else 0.14f),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = strings.copyrightCategoryLabel(attribution.licenseCategory),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tokens.isDark && attribution.licenseCategory == CopyrightLicenseCategory.OPEN_LICENSE) {
                            Color(0xFF6EE7B7)
                        } else if (tokens.isDark && attribution.licenseCategory == CopyrightLicenseCategory.COPYRIGHTED) {
                            Color(0xFFFBBF24)
                        } else {
                            badgeColor
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CopyrightMetaLine(
                label = strings.fullTranslationNameLabel,
                value = valueOrUnverified(attribution.fullTranslationName),
                isUnverified = attribution.fullTranslationName.isBlank()
            )
            CopyrightMetaLine(
                label = strings.copyrightOwnerLabel,
                value = valueOrUnverified(attribution.ownerPublisher),
                isUnverified = attribution.ownerPublisher.isBlank()
            )
            CopyrightMetaLine(
                label = strings.copyrightNoticeLabel,
                value = valueOrUnverified(attribution.copyrightNotice),
                isUnverified = attribution.copyrightNotice.isBlank()
            )
            CopyrightMetaLine(
                label = strings.licenseLabel,
                value = valueOrUnverified(attribution.license),
                isUnverified = attribution.license.isBlank()
            )
            CopyrightMetaLine(
                label = strings.usagePermissionsLabel,
                value = valueOrUnverified(attribution.usagePermissions),
                isUnverified = attribution.usagePermissions.isBlank()
            )
            CopyrightMetaLine(
                label = strings.publisherCreditLabel,
                value = valueOrUnverified(attribution.translationCredits),
                isUnverified = attribution.translationCredits.isBlank()
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${strings.sourceLabel}:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            val validUrls = attribution.sourceUrls.map { it.trim() }.filter { it.isNotEmpty() }
            if (validUrls.isEmpty()) {
                Text(
                    text = unverified,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = tokens.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    validUrls.forEachIndexed { linkIdx, url ->
                        ClickableSourceLinkChip(
                            displayText = url,
                            contentDesc = "${strings.sourceLabel}: $url",
                            testTag = "about_source_link_${attribution.versionId.lowercase()}_$linkIdx",
                            onClick = { onOpenUrl(url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableSourceLinkChip(
    displayText: String,
    contentDesc: String,
    testTag: String,
    onClick: () -> Unit
) {
    val tokens = LocalLiquidGlass.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tokens.accentColor.copy(alpha = if (tokens.isDark) 0.16f else 0.1f),
        border = BorderStroke(0.7.dp, tokens.accentColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                color = tokens.accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = contentDesc,
                tint = tokens.accentColor,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun CopyrightMetaLine(
    label: String,
    value: String,
    isUnverified: Boolean
) {
    val tokens = LocalLiquidGlass.current
    Column(modifier = Modifier.padding(vertical = 2.5.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.textPrimary.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isUnverified) FontStyle.Italic else FontStyle.Normal,
            color = tokens.textSecondary,
            lineHeight = 17.sp
        )
    }
}
