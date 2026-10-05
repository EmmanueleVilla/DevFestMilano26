package com.shadowings.devfestmilano26.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Observatory Logbook is a deliberately dark-only scheme (research.md "Design tokens") -- there
// is no light variant, matching the ratified Stitch design system.
private val ObservatoryLogbookColorScheme = darkColorScheme(
    primary = PrimaryCopper,
    onPrimary = SurfaceBase,
    primaryContainer = PrimaryCopperPressed,
    onPrimaryContainer = SurfaceBase,
    secondary = SyncedGreen,
    onSecondary = SurfaceBase,
    secondaryContainer = SurfaceRaised,
    onSecondaryContainer = SyncedGreen,
    tertiary = OnSurfaceMuted,
    onTertiary = SurfaceBase,
    background = SurfaceBase,
    onBackground = OnSurfacePaper,
    surface = SurfaceBase,
    onSurface = OnSurfacePaper,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = OnSurfaceMuted,
    surfaceContainerLowest = SurfaceBase,
    surfaceContainerLow = SurfaceRaised,
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = SurfaceRaised,
    surfaceContainerHighest = SurfaceRaised,
    outline = Hairline,
    outlineVariant = Hairline,
    error = ErrorRust,
    onError = OnSurfacePaper,
    errorContainer = SurfaceRaised,
    onErrorContainer = ErrorRust,
    inverseSurface = OnSurfacePaper,
    inverseOnSurface = SurfaceBase,
)

// "Shapes are strictly Sharp (roundedness: 0)" (research.md) -- hairlines replace shadows/rounding.
private val ObservatoryLogbookShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun DevFestMilano26Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ObservatoryLogbookColorScheme,
        typography = Typography,
        shapes = ObservatoryLogbookShapes,
        content = content,
    )
}
