package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AncientFloralColorScheme = darkColorScheme(
    primary = AntiqueGold,
    onPrimary = ObsidianPlum,
    primaryContainer = BurnishedBronze,
    onPrimaryContainer = ParchmentIvory,
    secondary = BotanicalSage,
    onSecondary = ObsidianPlum,
    secondaryContainer = DeepMoss,
    onSecondaryContainer = MintLichen,
    tertiary = PeonyCrimson,
    onTertiary = ParchmentIvory,
    tertiaryContainer = DeepBordeaux,
    onTertiaryContainer = RoseMadder,
    background = ObsidianPlum,
    onBackground = ParchmentIvory,
    surface = VelvetNight,
    onSurface = ParchmentIvory,
    surfaceVariant = DamaskSurface,
    onSurfaceVariant = MutedLinen,
    error = DangerScarlet,
    onError = ObsidianPlum
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AncientFloralColorScheme,
        typography = Typography,
        content = content
    )
}
