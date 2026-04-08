package com.ticketinglab.android.feature.show.presentation.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticketinglab.android.core.designsystem.component.EmptyContent
import com.ticketinglab.android.core.designsystem.component.ErrorContent
import com.ticketinglab.android.core.designsystem.component.LoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAvailabilityScreen(
    state: ShowAvailabilityUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onHoldClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회차 가용성") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onHoldClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = state.availability != null,
            ) {
                Text(text = "홀드/예약/결제 흐름 이어가기")
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent()
            state.errorMessage != null -> ErrorContent(message = state.errorMessage, onRetry = onRefresh)
            state.availability == null -> EmptyContent(message = "가용성 정보가 없습니다.")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(text = "좌석", style = MaterialTheme.typography.headlineSmall)
                }
                items(state.availability.seats, key = { it.seatId }) { seat ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = seat.label, style = MaterialTheme.typography.titleMedium)
                            Text(text = "위치: ${seat.rowNo}행 ${seat.colNo}열")
                            Text(text = "가격: ${seat.price}")
                            Text(text = if (seat.available) "예매 가능" else "예매 불가")
                        }
                    }
                }
                item {
                    Text(
                        text = "구역",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                items(state.availability.sections, key = { it.sectionId }) { section ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = section.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "가격: ${section.price}")
                            Text(text = "잔여 수량: ${section.remainingQty}")
                        }
                    }
                }
            }
        }
    }
}