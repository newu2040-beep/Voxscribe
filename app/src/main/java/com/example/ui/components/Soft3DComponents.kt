package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Atmospheric background that replicates the multi-layered glass radial gradients
 * specified in the design instructions.
 */
@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) GlassDarkBackground else GlassLightBackground

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .drawBehind {
                // Top-Left Light Leak (Vibrant Sky Blue Glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) Color(0x3D1E40AF) else Color(0x523B82F6),
                            Color.Transparent
                        ),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.8f
                    )
                )
                // Bottom-Right Light Leak (Lighter Blue Ambient Glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) Color(0x333B82F6) else Color(0x4C60A5FA),
                            Color.Transparent
                        ),
                        center = Offset(size.width, size.height),
                        radius = size.width * 0.8f
                    )
                )
                // Center-Middle Light Leak (Soft Lavender/Sky Blur)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) Color(0x1F312E81) else Color(0x3FBFDBFE),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.width * 0.9f
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Glassmorphic Card Container that replaces the old heavy 3D cards.
 * Implements a semi-transparent blurred panel style with light highlights.
 */
@Composable
fun Soft3DCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.Unspecified,
    shadowColor: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.Unspecified,
    depth: Dp = 0.dp, // Retained for compatibility but set to 0 to align with flat Glassmorphism
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Frosted internal gradient fill
    val glassFillBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.45f)
            )
        )
    }

    // High-refraction double highlighted border brush
    val glassBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    }

    // Shadow profile suitable for floating glass panes
    val glassShadowAlpha = if (isDark) 0.1f else 0.04f
    val shadowColorToUse = Color.Black.copy(alpha = glassShadowAlpha)

    Box(
        modifier = modifier
            .drawBehind {
                // Smooth drop shadow glow around corners
                drawRoundRect(
                    color = shadowColorToUse,
                    topLeft = Offset(0f, 4f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .clip(shape)
            .background(glassFillBrush)
            .border(borderWidth, glassBorderBrush, shape)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}

/**
 * Super responsive tactile glassmorphic and gradient rounded button.
 */
@Composable
fun Soft3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shadowColor: Color = Color.Unspecified, // Compatibility param
    depth: Dp = 0.dp,                      // Compatibility param
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val isPressedInteractionSource = remember { MutableInteractionSource() }
    val isPressed by isPressedInteractionSource.collectIsPressedAsState()

    // Pristine mechanical responsive scale
    val clickScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1.0f,
        label = "glass_button_scale"
    )

    val isDark = isSystemInDarkTheme()

    // Determine custom visual style of button
    val isPrimaryButton = containerColor == MaterialTheme.colorScheme.primary
    
    val buttonBrush = if (isPrimaryButton) {
        // High quality VoxScribe primary gradient matching the design spec
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0061A4),
                Color(0xFF3B82F6)
            )
        )
    } else {
        // Frosted secondary button
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.70f)
                )
            )
        }
    }

    val finalContentColor = if (isPrimaryButton) {
        Color.White
    } else {
        if (isDark) Color(0xFF8EB2FF) else Color(0xFF0061A4)
    }

    val strokeBorderBrush = if (isPrimaryButton) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.40f),
                Color.Transparent
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.20f)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(clickScale)
            .testTag(testTag ?: "")
            .clip(shape)
            .background(buttonBrush)
            .border(1.dp, strokeBorderBrush, shape)
            .clickable(
                interactionSource = isPressedInteractionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides finalContentColor
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

/**
 * Sleek glass capsule chip for selecting language modes.
 */
@Composable
fun GlassmorphicChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val isLight = !isSystemInDarkTheme()

    val containerColor = if (isSelected) {
        if (isLight) Color(0xFFD1E4FF) else Color(0xFF1E3A8A)
    } else {
        if (isLight) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f)
    }

    val contentColor = if (isSelected) {
        if (isLight) Color(0xFF001D44) else Color(0xFFE2ECFA)
    } else {
        if (isLight) Color(0xFF001D44).copy(alpha = 0.6f) else Color(0xFFE2ECFA).copy(alpha = 0.5f)
    }

    val borderColor = if (isSelected) {
        if (isLight) Color(0xFF0061A4).copy(alpha = 0.3f) else Color(0xFF6F9CFF).copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Sunken glass slot container for text fields or status bars.
 */
@Composable
fun Soft3DTextFieldSlot(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgFill = if (isDark) {
        Color.Black.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }
    val borderBrush = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgFill)
            .border(borderWidth, borderBrush, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}
