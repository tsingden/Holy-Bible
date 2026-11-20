package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ButtonStyleOption
import com.example.data.model.NavBarAppearance

/**
 * Full-screen atmospheric backdrop supporting hardware-accelerated RenderEffect blur on Android 12+
 * and silky multi-stop radial falloffs on older Android devices (< API 31).
 */
@Composable
fun LiquidGlassBackdrop(
    tokens: LiquidGlassTokens = LocalLiquidGlass.current,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val startColor by animateColorAsState(tokens.backdropStart, label = "backdropStart")
    val endColor by animateColorAsState(tokens.backdropEnd, label = "backdropEnd")
    val orb1 by animateColorAsState(tokens.orbPrimary, label = "orb1")
    val orb2 by animateColorAsState(tokens.orbSecondary, label = "orb2")
    val orb3 by animateColorAsState(tokens.orbTertiary, label = "orb3")
    val blurDp by animateDpAsState(tokens.blurIntensityDp.dp, label = "backdropBlur")

    val useHardwareBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && tokens.blurIntensityDp > 0.5f

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
    ) {
        val canvasModifier = if (useHardwareBlur) {
            Modifier
                .matchParentSize()
                .blur(radius = blurDp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        } else {
            Modifier.matchParentSize()
        }

        Canvas(modifier = canvasModifier) {
            val w = size.width
            val h = size.height
            if (orb1.alpha > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb1, orb1.copy(alpha = orb1.alpha * 0.35f), Color.Transparent),
                        center = Offset(w * 0.16f, h * 0.16f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.16f, h * 0.16f),
                    radius = w * 0.65f
                )
            }
            if (orb2.alpha > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb2, orb2.copy(alpha = orb2.alpha * 0.3f), Color.Transparent),
                        center = Offset(w * 0.86f, h * 0.36f),
                        radius = w * 0.72f
                    ),
                    center = Offset(w * 0.86f, h * 0.36f),
                    radius = w * 0.72f
                )
            }
            if (orb3.alpha > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb3, orb3.copy(alpha = orb3.alpha * 0.28f), Color.Transparent),
                        center = Offset(w * 0.32f, h * 0.84f),
                        radius = w * 0.68f
                    ),
                    center = Offset(w * 0.32f, h * 0.84f),
                    radius = w * 0.68f
                )
            }
            if (tokens.isCrystalClear) {
                // Subtle diagonal prismatic refraction sheen behind transparent crystal surfaces
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (tokens.isDark) 0.09f else 0.36f),
                            tokens.accentColor.copy(alpha = if (tokens.isDark) 0.14f else 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(0f, h * 0.18f),
                        end = Offset(w, h * 0.62f)
                    ),
                    start = Offset(0f, h * 0.18f),
                    end = Offset(w, h * 0.62f),
                    strokeWidth = w * 0.22f
                )
            }
        }

        content()
    }
}

/**
 * Reusable iOS Liquid Glass translucent container with top specular reflection,
 * hairline gradient rim border, and soft ambient shadow.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevated: Boolean = false,
    tokens: LiquidGlassTokens = LocalLiquidGlass.current,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (elevated) tokens.glassSurfaceElevated else tokens.glassSurface,
        label = "glassBg"
    )
    val borderTop by animateColorAsState(tokens.borderTop, label = "borderTop")
    val borderBottom by animateColorAsState(tokens.borderBottom, label = "borderBottom")

    val baseMod = modifier
        .shadow(
            elevation = when {
                tokens.isCrystalClear -> 10.dp
                tokens.isGlassActive -> 8.dp
                else -> 3.dp
            },
            shape = shape,
            ambientColor = tokens.accentColor.copy(alpha = if (tokens.isCrystalClear) 0.24f else 0.16f),
            spotColor = Color.Black.copy(alpha = if (tokens.isDark) 0.45f else 0.12f)
        )
        .clip(shape)
        .background(bgColor)
        .then(
            when {
                tokens.isCrystalClear -> {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                tokens.specularTop.copy(alpha = if (tokens.isDark) 0.20f else 0.62f),
                                Color.White.copy(alpha = if (tokens.isDark) 0.03f else 0.10f),
                                tokens.accentColor.copy(alpha = if (tokens.isDark) 0.08f else 0.06f),
                                Color.White.copy(alpha = if (tokens.isDark) 0.07f else 0.24f)
                            )
                        )
                    )
                }
                tokens.isGlassActive -> {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                tokens.specularTop.copy(alpha = if (tokens.isDark) 0.12f else 0.45f),
                                Color.Transparent,
                                tokens.accentColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }
                else -> Modifier
            }
        )
        .border(
            width = when {
                tokens.isCrystalClear -> 1.15.dp
                tokens.isGlassActive -> 1.dp
                else -> 0.7.dp
            },
            brush = if (tokens.isCrystalClear) {
                Brush.linearGradient(
                    colors = listOf(borderTop, Color.White.copy(alpha = if (tokens.isDark) 0.14f else 0.55f), borderBottom)
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(borderTop, borderBottom)
                )
            },
            shape = shape
        )
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(modifier = baseMod, content = content)
}

/**
 * Adaptive primary CTA button respecting the user's ButtonStyleOption customization.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokens: LiquidGlassTokens = LocalLiquidGlass.current,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val shape = when (tokens.buttonStyle) {
        ButtonStyleOption.LIQUID_GLASS, ButtonStyleOption.PILL_FILLED -> RoundedCornerShape(50)
        ButtonStyleOption.OUTLINED_GLASS -> RoundedCornerShape(16.dp)
        ButtonStyleOption.CLASSIC_ELEVATED -> RoundedCornerShape(12.dp)
    }

    when (tokens.buttonStyle) {
        ButtonStyleOption.OUTLINED_GLASS -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(1.2.dp, tokens.accentColor.copy(alpha = 0.78f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = tokens.accentColor.copy(alpha = 0.16f),
                    contentColor = tokens.textPrimary
                ),
                contentPadding = contentPadding,
                modifier = modifier.heightIn(min = 48.dp)
            ) {
                content()
            }
        }
        ButtonStyleOption.LIQUID_GLASS -> {
            val containerAlpha = if (tokens.isCrystalClear) 0.76f else 0.86f
            val borderCol = if (tokens.isCrystalClear) {
                Color.White.copy(alpha = if (tokens.isDark) 0.52f else 0.82f)
            } else {
                Color.White.copy(alpha = 0.45f)
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(if (tokens.isCrystalClear) 1.1.dp else 0.9.dp, borderCol),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accentColor.copy(alpha = containerAlpha),
                    contentColor = tokens.onAccentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (tokens.isCrystalClear) 6.dp else 4.dp),
                contentPadding = contentPadding,
                modifier = modifier.heightIn(min = 48.dp)
            ) {
                content()
            }
        }
        ButtonStyleOption.PILL_FILLED -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accentColor,
                    contentColor = tokens.onAccentColor
                ),
                contentPadding = contentPadding,
                modifier = modifier.heightIn(min = 48.dp)
            ) {
                content()
            }
        }
        ButtonStyleOption.CLASSIC_ELEVATED -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accentColor,
                    contentColor = tokens.onAccentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                contentPadding = contentPadding,
                modifier = modifier.heightIn(min = 48.dp)
            ) {
                content()
            }
        }
    }
}

data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * Adaptive Bottom Navigation supporting iOS Floating Glass Dock, Full-Width Frosted Bar,
 * and Classic Solid Bar. Always preserves navigationBars insets.
 */
@Composable
fun LiquidGlassBottomNav(
    items: List<NavDestinationItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    tokens: LiquidGlassTokens = LocalLiquidGlass.current,
    modifier: Modifier = Modifier
) {
    when (tokens.navBarAppearance) {
        NavBarAppearance.FLOATING_GLASS_DOCK -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(28.dp),
                    elevated = true,
                    tokens = tokens,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            val pillColor by animateColorAsState(
                                targetValue = if (selected) {
                                    tokens.accentColor.copy(alpha = if (tokens.isDark) 0.28f else 0.18f)
                                } else {
                                    Color.Transparent
                                },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "dockPill"
                            )
                            val tintColor by animateColorAsState(
                                targetValue = if (selected) tokens.accentColor else tokens.textSecondary,
                                label = "dockTint"
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(pillColor)
                                    .clickable { onNavigate(item.route) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                                    .testTag(item.testTag),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = tintColor,
                                    modifier = Modifier.size(21.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = tintColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        NavBarAppearance.FROSTED_FULL_BAR -> {
            Surface(
                color = tokens.glassSurfaceElevated,
                tonalElevation = 0.dp,
                modifier = modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.7.dp,
                        color = tokens.borderTop
                    )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, maxLines = 1) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
        NavBarAppearance.SOLID_MINIMAL -> {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = modifier
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, maxLines = 1) },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSwatchCircle(
    color: Color,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("theme_swatch_$label"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.2.dp,
                    color = if (selected) Color.White else Color.Gray.copy(alpha = 0.45f),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Reusable Liquid Glass dialog wrapper with smooth scale+fade entrance for all popups/dialogs.
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    tokens: LiquidGlassTokens = LocalLiquidGlass.current,
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(180)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(200)
            )
        ) {
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                elevated = true,
                tokens = tokens,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 460.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    content()
                }
            }
        }
    }
}
