package com.moneyapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneyapp.presentation.navigation.Screen
import com.moneyapp.data.local.datastore.ThemePreferences
import com.moneyapp.presentation.screen.onboarding.OnboardingScreen
import com.moneyapp.presentation.screen.dashboard.DashboardScreen
import com.moneyapp.presentation.screen.transaction.TransactionListScreen
import com.moneyapp.presentation.screen.transaction.TransactionFormScreen
import com.moneyapp.presentation.screen.transaction.TransactionDetailScreen
import com.moneyapp.presentation.screen.investment.InvestmentScreen
import com.moneyapp.presentation.screen.saving.SavingScreen
import com.moneyapp.presentation.screen.report.ReportScreen
import com.moneyapp.presentation.screen.settings.SettingsScreen
import com.moneyapp.data.local.db.AppDatabase

/**
 * Data class untuk item Bottom Navigation.
 */
data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

/** Daftar tab Bottom Navigation: Beranda, Transaksi, Laporan, Pengaturan */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, Icons.Filled.Home, "Beranda"),
    BottomNavItem(Screen.Transaction, Icons.Filled.Receipt, "Transaksi"),
    BottomNavItem(Screen.Report, Icons.Filled.BarChart, "Laporan"),
    BottomNavItem(Screen.Settings, Icons.Filled.Settings, "Pengaturan")
)

/** Route-route yang menampilkan Bottom Navigation */
private val bottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.Transaction.route,
    Screen.Report.route,
    Screen.Settings.route
)

/**
 * Root NavHost aplikasi.
 * - startDestination = "splash"
 * - SplashScreen mengecek user di DB dan meredirect ke onboarding atau dashboard
 * - Main screens dibungkus Scaffold dengan BottomNavigation 4 tab
 */
@Composable
fun AppNavGraph(
    themePreferences: ThemePreferences,
    db: AppDatabase,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Splash ──────────────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            // ── Onboarding ──────────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController = navController)
            }

            // ── Dashboard ───────────────────────────────────────────────────
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            // ── Transaction list ─────────────────────────────────────────────
            composable(Screen.Transaction.route) {
                TransactionListScreen(navController = navController)
            }

            // ── Transaction form (tambah / edit) ─────────────────────────────
            composable(
                route = Screen.TransactionForm.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                TransactionFormScreen(navController = navController, transactionId = id)
            }

            // ── Transaction detail ───────────────────────────────────────────
            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                TransactionDetailScreen(navController = navController, transactionId = id)
            }

            // ── Investment list ──────────────────────────────────────────────
            composable(Screen.Investment.route) {
                InvestmentScreen(navController = navController)
            }

            // ── Investment form ──────────────────────────────────────────────
            composable(
                route = Screen.InvestmentForm.route,
                arguments = listOf(
                    navArgument("investmentId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                PlaceholderScreen(label = "Form Investasi")
            }

            // ── Saving list ──────────────────────────────────────────────────
            composable(Screen.Saving.route) {
                SavingScreen(navController = navController)
            }

            // ── Saving form ──────────────────────────────────────────────────
            composable(
                route = Screen.SavingForm.route,
                arguments = listOf(
                    navArgument("savingId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                PlaceholderScreen(label = "Form Tabungan")
            }

            // ── Report ───────────────────────────────────────────────────────
            composable(Screen.Report.route) {
                ReportScreen(navController = navController, db = db)
            }

            // ── Settings ─────────────────────────────────────────────────────
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}

/**
 * Splash screen: mengecek apakah user sudah ada di DB.
 * - Jika null → navigate ke Onboarding
 * - Jika ada  → navigate ke Dashboard
 *
 * Menggunakan [SplashViewModel] yang diinjeksi via Hilt.
 */
@Composable
private fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState(initial = null)
    val isLoaded by viewModel.isLoaded.collectAsState()

    LaunchedEffect(user, isLoaded) {
        // Tunggu sampai Flow emit nilai pertama (bukan null karena belum load)
        // viewModel.isLoaded memastikan kita sudah mendapat respons dari DB
        if (isLoaded) {
            if (user == null) {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    // Layar splash kosong (bisa diganti dengan logo/animasi)
    Box(modifier = Modifier.fillMaxSize())
}

/**
 * Bottom Navigation Bar dengan 4 tab.
 * Tab aktif ditandai dengan warna Primary dari MaterialTheme.
 */
@Composable
private fun AppBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        // Hindari stack yang menumpuk saat tap tab berulang
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

/**
 * Placeholder composable untuk screen yang belum diimplementasikan.
 */
@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
