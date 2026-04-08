package com.ticketinglab.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    surface = BrandSurface,
    background = BrandBackground,
    error = BrandError,
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    error = BrandError,
)

@Composable
fun TicketingLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TicketingLabTypography,
        content = content,
    )
}