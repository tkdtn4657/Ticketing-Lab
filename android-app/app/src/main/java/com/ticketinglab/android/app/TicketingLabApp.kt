package com.ticketinglab.android.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ticketinglab.android.app.navigation.AppNavHost

@Composable
fun TicketingLabApp() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}