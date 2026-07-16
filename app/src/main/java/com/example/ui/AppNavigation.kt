package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

@Composable
fun AppNavigation(viewModel: DisciplineViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Hide bottom bar on screens that are opened from Settings or other flows if needed
            // But we can just leave it visible for standard tabs
            val bottomBarRoutes = listOf("home", "calendar", "settings")
            if (bottomBarRoutes.contains(currentDestination?.route)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        "home" to "HOME",
                        "calendar" to "CAL",
                        "settings" to "SET"
                    )

                    items.forEach { (route, label) ->
                        NavigationBarItem(
                            icon = { Text(label.first().toString()) }, 
                            label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                indicatorColor = MaterialTheme.colorScheme.surface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("home") { HomeScreen(viewModel, onNavigateToReport = { navController.navigate("report") }) }
            composable("report") { ReportScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable("calendar") { CalendarScreen(viewModel, onNavigateToReport = { navController.navigate("report") }) }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }
}
