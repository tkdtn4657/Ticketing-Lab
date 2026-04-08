package com.ticketinglab.android.feature.event.presentation.eventdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun EventDetailScreen(
    state: EventDetailUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onShowClick: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("이벤트 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent()
            state.errorMessage != null -> ErrorContent(message = state.errorMessage, onRetry = onRefresh)
            state.eventDetail == null -> EmptyContent(message = "이벤트 정보를 찾을 수 없습니다.")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        Text(state.eventDetail.title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = state.eventDetail.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            text = "상태: ${state.eventDetail.status}",
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                items(state.eventDetail.shows, key = { it.showId }) { show ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShowClick(show.showId) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "회차 #${show.showId}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "시작: ${show.startAt}", modifier = Modifier.padding(top = 8.dp))
                            Text(text = "상태: ${show.status}", modifier = Modifier.padding(top = 4.dp))
                            Text(text = "공연장 ID: ${show.venueId}", modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}