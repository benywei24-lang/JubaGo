package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Destinations for Jetpack Navigation Compose in JubaGo.
 * Includes primary bottom navigation items (Home, Orders, Profile)
 * as well as specialized sub-destinations (Live Tracking, Support Chat, Merchant Dashboard).
 */
sealed class JubaGoDestination(
    val route: String,
    val titleKey: String,
    val defaultLabel: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Home : JubaGoDestination(
        route = "home",
        titleKey = "nav_home",
        defaultLabel = "Home",
        icon = Icons.Default.Explore,
        testTag = "nav_item_home"
    )

    object Orders : JubaGoDestination(
        route = "orders",
        titleKey = "nav_orders",
        defaultLabel = "Orders",
        icon = Icons.Default.ReceiptLong,
        testTag = "nav_item_orders"
    )

    object Profile : JubaGoDestination(
        route = "profile",
        titleKey = "nav_profile",
        defaultLabel = "Profile",
        icon = Icons.Default.Person,
        testTag = "nav_item_profile"
    )

    // Secondary / sub-screens accessible via Navigation
    object Tracking : JubaGoDestination(
        route = "tracking",
        titleKey = "nav_tracking",
        defaultLabel = "Live GPS",
        icon = Icons.Default.Navigation,
        testTag = "nav_item_tracking"
    )

    object Chat : JubaGoDestination(
        route = "chat",
        titleKey = "nav_support",
        defaultLabel = "Support",
        icon = Icons.Default.Chat,
        testTag = "nav_item_chat"
    )

    object Merchant : JubaGoDestination(
        route = "merchant",
        titleKey = "nav_merchant",
        defaultLabel = "Merchant",
        icon = Icons.Default.Storefront,
        testTag = "nav_item_merchant"
    )

    companion object {
        /**
         * The primary destinations displayed in the bottom navigation bar.
         */
        val bottomNavDestinations: List<JubaGoDestination> = listOf(
            Home,
            Orders,
            Profile
        )

        fun fromRoute(route: String?): JubaGoDestination {
            return when (route?.substringBefore("/")) {
                Home.route -> Home
                Orders.route -> Orders
                Profile.route -> Profile
                Tracking.route -> Tracking
                Chat.route -> Chat
                Merchant.route -> Merchant
                else -> Home
            }
        }
    }
}

/**
 * Standard Jetpack Navigation single-top navigation extension
 * adhering to official Android Compose navigation patterns with state restoration.
 */
fun NavController.navigateToDestination(route: String) {
    this.navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
