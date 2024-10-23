package com.ancient.flow.game.presentation.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

fun NavHostController.navigateSingleTop(
    route: String,  // The complete route string, with arguments included
    builder: NavOptionsBuilder.() -> Unit = {}  // Optional navigation options
) {
    val currentRoute = currentBackStackEntry?.destination?.route ?: return
    navigate(route) {
        builder(this)
        popUpTo(currentRoute) {
            inclusive = true

        }// Ensure single-top behavior
        launchSingleTop = true
    }
}

fun NavHostController.navigatePopUpInclusive(
    screen: Screen,
    builder: NavOptionsBuilder.() -> Unit = {}  // Optional navigation options
) {
    val currentRoute = currentBackStackEntry?.destination?.route ?: return
    navigate(screen.route) {
        popUpTo(currentRoute) {
            inclusive = true
        }
        builder(this)
    }
}

