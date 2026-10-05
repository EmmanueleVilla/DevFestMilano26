package com.shadowings.devfestmilano26.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shadowings.devfestmilano26.apod.presentation.detail.DetailScreen
import com.shadowings.devfestmilano26.apod.presentation.favorites.FavoritesScreen
import com.shadowings.devfestmilano26.apod.presentation.login.LoginScreen
import com.shadowings.devfestmilano26.apod.presentation.today.TodayScreen

private data class BottomTab(
    val route: Any,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

// Persistent bottom navigation per the approved Stitch screens (research.md "Navigation"
// addendum): Today, Log/Detail, Favorites, Access -- present identically on all four screens.
private val bottomTabs = listOf(
    BottomTab(Today, "Today", Icons.Filled.WbTwilight),
    BottomTab(Detail(), "Log / Detail", Icons.AutoMirrored.Filled.MenuBook),
    BottomTab(Favorites, "Favorites", Icons.Filled.StarRate),
    BottomTab(Login, "Access", Icons.Filled.Tune),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination.isTab(tab),
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Today,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Today> {
                TodayScreen(onOpenDetail = { date -> navController.navigate(Detail(date)) })
            }
            composable<Detail> { backStackEntry ->
                val detail: Detail = backStackEntry.toRoute()
                DetailScreen(date = detail.date)
            }
            composable<Favorites> {
                FavoritesScreen(onOpenFavorite = { date -> navController.navigate(Detail(date)) })
            }
            composable<Login> {
                LoginScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

private fun androidx.navigation.NavDestination?.isTab(tab: BottomTab): Boolean = when (tab.route) {
    is Today -> this?.hasRoute(Today::class) == true
    is Detail -> this?.hasRoute(Detail::class) == true
    is Favorites -> this?.hasRoute(Favorites::class) == true
    is Login -> this?.hasRoute(Login::class) == true
    else -> false
}
