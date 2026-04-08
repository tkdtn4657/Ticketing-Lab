package com.ticketinglab.android.feature.event.presentation.eventlist

import androidx.compose.foundation.clickable
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
fun EventListScreen(
    state: EventListUiState,
    onRefresh: () -> Unit,
    onEventClick: (Long) -> Unit,
    onOpenTicketsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("이벤트") },
                actions = {
                    Button(onClick = onOpenTicketsClick) {
                        Text("티켓")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent()
            state.errorMessage != null -> ErrorContent(message = state.errorMessage, onRetry = onRefresh)
            state.events.isEmpty() -> EmptyContent(message = "공개된 이벤트가 없습니다.")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.events, key = { it.eventId }) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventClick(event.eventId) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = event.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(
                                text = "상태: ${event.status}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}