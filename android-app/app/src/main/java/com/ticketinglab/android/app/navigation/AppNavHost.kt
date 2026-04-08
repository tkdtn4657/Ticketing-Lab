package com.ticketinglab.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ticketinglab.android.feature.auth.presentation.login.LoginEvent
import com.ticketinglab.android.feature.auth.presentation.login.LoginScreen
import com.ticketinglab.android.feature.auth.presentation.login.LoginViewModel
import com.ticketinglab.android.feature.auth.presentation.signup.SignupEvent
import com.ticketinglab.android.feature.auth.presentation.signup.SignupScreen
import com.ticketinglab.android.feature.auth.presentation.signup.SignupViewModel
import com.ticketinglab.android.feature.auth.presentation.splash.SplashEvent
import com.ticketinglab.android.feature.auth.presentation.splash.SplashScreen
import com.ticketinglab.android.feature.auth.presentation.splash.SplashViewModel
import com.ticketinglab.android.feature.event.presentation.eventdetail.EventDetailEvent
import com.ticketinglab.android.feature.event.presentation.eventdetail.EventDetailScreen
import com.ticketinglab.android.feature.event.presentation.eventdetail.EventDetailViewModel
import com.ticketinglab.android.feature.event.presentation.eventlist.EventListEvent
import com.ticketinglab.android.feature.event.presentation.eventlist.EventListScreen
import com.ticketinglab.android.feature.event.presentation.eventlist.EventListViewModel
import com.ticketinglab.android.feature.show.presentation.availability.ShowAvailabilityScreen
import com.ticketinglab.android.feature.show.presentation.availability.ShowAvailabilityViewModel
import com.ticketinglab.android.feature.support.presentation.ComingSoonScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state = viewModel.uiState

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        SplashEvent.NavigateToEvents -> {
                            navController.navigate(AppRoute.EventList.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }

                        SplashEvent.NavigateToLogin -> {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            SplashScreen(state = state)
        }

        composable(AppRoute.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state = viewModel.uiState

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        LoginEvent.NavigateToEvents -> {
                            navController.navigate(AppRoute.EventList.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }

                        LoginEvent.NavigateToSignup -> navController.navigate(AppRoute.Signup.route)
                    }
                }
            }

            LoginScreen(
                state = state,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onLoginClick = viewModel::login,
                onSignupClick = viewModel::openSignup,
            )
        }

        composable(AppRoute.Signup.route) {
            val viewModel: SignupViewModel = hiltViewModel()
            val state = viewModel.uiState

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        SignupEvent.NavigateToLogin -> navController.popBackStack()
                    }
                }
            }

            SignupScreen(
                state = state,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onSignupClick = viewModel::signup,
                onBackClick = viewModel::backToLogin,
            )
        }

        composable(AppRoute.EventList.route) {
            val viewModel: EventListViewModel = hiltViewModel()
            val state = viewModel.uiState

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is EventListEvent.OpenEventDetail -> {
                            navController.navigate(AppRoute.EventDetail.create(event.eventId))
                        }

                        EventListEvent.OpenTicketsComingSoon -> {
                            navController.navigate(AppRoute.ComingSoon.create("tickets"))
                        }
                    }
                }
            }

            EventListScreen(
                state = state,
                onRefresh = viewModel::refresh,
                onEventClick = viewModel::openEvent,
                onOpenTicketsClick = viewModel::openTickets,
            )
        }

        composable(
            route = AppRoute.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
        ) {
            val viewModel: EventDetailViewModel = hiltViewModel()
            val state = viewModel.uiState

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is EventDetailEvent.OpenShowAvailability -> {
                            navController.navigate(AppRoute.ShowAvailability.create(event.showId))
                        }
                    }
                }
            }

            EventDetailScreen(
                state = state,
                onBackClick = { navController.popBackStack() },
                onRefresh = viewModel::refresh,
                onShowClick = viewModel::openShow,
            )
        }

        composable(
            route = AppRoute.ShowAvailability.route,
            arguments = listOf(navArgument("showId") { type = NavType.LongType }),
        ) {
            val viewModel: ShowAvailabilityViewModel = hiltViewModel()
            val state = viewModel.uiState

            ShowAvailabilityScreen(
                state = state,
                onBackClick = { navController.popBackStack() },
                onRefresh = viewModel::refresh,
                onHoldClick = {
                    navController.navigate(AppRoute.ComingSoon.create("hold-reservation-payment"))
                },
            )
        }

        composable(
            route = AppRoute.ComingSoon.route,
            arguments = listOf(navArgument("title") { type = NavType.StringType }),
        ) { entry ->
            ComingSoonScreen(
                title = entry.arguments?.getString("title").orEmpty(),
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}