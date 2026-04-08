package com.ticketinglab.android.feature.auth.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignupScreen(
    state: SignupUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "회원가입", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "백엔드 Swagger 기준 Auth API를 바로 연동할 수 있도록 구성했습니다.",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            when {
                state.completed -> Text(
                    text = "회원가입이 완료되었습니다. 로그인으로 돌아가세요.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp),
                )

                state.errorMessage != null -> Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSignupClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "회원가입")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBackClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "로그인으로 돌아가기")
            }
        }
    }
}