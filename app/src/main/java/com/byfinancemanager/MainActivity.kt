package com.byfinancemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byfinancemanager.ui.screens.AddTransactionScreen
import com.byfinancemanager.ui.screens.DashboardScreen
import com.byfinancemanager.ui.screens.SourcesScreen
import com.byfinancemanager.ui.screens.TransactionsScreen
import com.byfinancemanager.ui.theme.BYFinanceTheme
import com.byfinancemanager.ui.viewmodel.FinanceViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Главная", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Операции", Icons.Default.List)
    object Sources : Screen("sources", "Источники", Icons.Default.Category)
    object AddTransaction : Screen("add_transaction", "Добавить", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as FinanceApp
                return FinanceViewModel(app, app.repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BYFinanceTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavItems = listOf(
                    Screen.Dashboard,
                    Screen.Transactions,
                    Screen.Sources
                )

                val showBottomBar = currentRoute != Screen.AddTransaction.route

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                                        label = { Text(screen.title) },
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onAddTransaction = {
                                    navController.navigate(Screen.AddTransaction.route)
                                }
                            )
                        }
                        composable(Screen.Transactions.route) {
                            TransactionsScreen(viewModel = viewModel)
                        }
                        composable(Screen.Sources.route) {
                            SourcesScreen(viewModel = viewModel)
                        }
                        composable(Screen.AddTransaction.route) {
                            AddTransactionScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
