package com.ticketinglab.android.app.navigation

sealed interface AppRoute {
    val route: String

    data object Splash : AppRoute {
        override val route: String = "splash"
    }

    data object Login : AppRoute {
        override val route: String = "login"
    }

    data object Signup : AppRoute {
        override val route: String = "signup"
    }

    data object EventList : AppRoute {
        override val route: String = "events"
    }

    data object EventDetail : AppRoute {
        override val route: String = "events/{eventId}"

        fun create(eventId: Long): String = "events/$eventId"
    }

    data object ShowAvailability : AppRoute {
        override val route: String = "shows/{showId}/availability"

        fun create(showId: Long): String = "shows/$showId/availability"
    }

    data object ComingSoon : AppRoute {
        override val route: String = "coming-soon/{title}"

        fun create(title: String): String = "coming-soon/$title"
    }
}