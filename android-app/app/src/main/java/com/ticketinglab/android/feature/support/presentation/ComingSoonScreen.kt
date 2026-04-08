package com.ticketinglab.android.feature.support.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ComingSoonScreen(
    title: String,
    onBackClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "이 영역은 다음 단계에서 Hold, Reservation, Payment, Ticket, Admin 화면으로 확장합니다.",
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
            )
            Button(onClick = onBackClick) {
                Text(text = "뒤로")
            }
        }
    }
}