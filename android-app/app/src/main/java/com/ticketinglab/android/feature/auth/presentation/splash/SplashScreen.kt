package com.ticketinglab.android.feature.auth.presentation.splash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ticketinglab.android.core.designsystem.component.LoadingContent

@Composable
fun SplashScreen(state: SplashUiState) {
    Surface(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingContent(message = "세션을 확인하는 중입니다.")
        }
    }
}