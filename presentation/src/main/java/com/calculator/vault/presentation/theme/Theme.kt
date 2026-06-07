package com.calculator.vault.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VaultAccent,
    onPrimary = VaultTextPrimary,
    primaryContainer = CalcButtonOperator,
    onPrimaryContainer = CalcButtonText,
    secondary = VaultAccentSecondary,
    onSecondary = VaultBackground,
    tertiary = PinSuccessGlow,
    background = VaultBackground,
    onBackground = VaultTextPrimary,
    surface = GlassSurface,
    onSurface = VaultTextPrimary,
    surfaceVariant = CalcButtonNumber,
    onSurfaceVariant = VaultTextSecondary,
    error = VaultError,
    onError = CalcButtonText,
)

@Composable
fun CalculatorVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
