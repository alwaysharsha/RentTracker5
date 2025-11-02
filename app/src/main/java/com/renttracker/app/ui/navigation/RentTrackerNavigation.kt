package com.renttracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renttracker.app.MainActivity
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.ui.screens.*
import com.renttracker.app.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentTrackerApp(
    ownerViewModel: OwnerViewModel,
    buildingViewModel: BuildingViewModel,
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel,
    documentViewModel: DocumentViewModel,
    vendorViewModel: VendorViewModel,
    expenseViewModel: ExpenseViewModel,
    exportImportViewModel: ExportImportViewModel,
    mainActivity: MainActivity,
    database: RentTrackerDatabase,
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard screen
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    tenantViewModel = tenantViewModel,
                    paymentViewModel = paymentViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToScreen = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            // Owner screens
            composable(Screen.Owners.route) {
                OwnerScreen(
                    viewModel = ownerViewModel,
                    onNavigateToDetail = { ownerId ->
                        navController.navigate(
                            if (ownerId == null) "owner_detail/0"
                            else Screen.OwnerDetail.createRoute(ownerId)
                        )
                    }
                )
            }
            composable(
                route = Screen.OwnerDetail.route,
                arguments = listOf(navArgument("ownerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val ownerId = backStackEntry.arguments?.getLong("ownerId")
                OwnerDetailScreen(
                    viewModel = ownerViewModel,
                    settingsViewModel = settingsViewModel,
                    ownerId = if (ownerId == 0L) null else ownerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Building screens
            composable(Screen.Buildings.route) {
                BuildingScreen(
                    viewModel = buildingViewModel,
                    onNavigateToDetail = { buildingId ->
                        navController.navigate(
                            if (buildingId == null) "building_detail/0"
                            else Screen.BuildingDetail.createRoute(buildingId)
                        )
                    }
                )
            }
            composable(
                route = Screen.BuildingDetail.route,
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val buildingId = backStackEntry.arguments?.getLong("buildingId")
                BuildingDetailScreen(
                    viewModel = buildingViewModel,
                    ownerViewModel = ownerViewModel,
                    buildingId = if (buildingId == 0L) null else buildingId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tenant screens
            composable(Screen.Tenants.route) {
                TenantScreen(
                    viewModel = tenantViewModel,
                    onNavigateToDetail = { tenantId ->
                        navController.navigate(
                            if (tenantId == null) "tenant_detail/0"
                            else Screen.TenantDetail.createRoute(tenantId)
                        )
                    }
                )
            }
            composable(
                route = Screen.TenantDetail.route,
                arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("tenantId")
                TenantDetailScreen(
                    viewModel = tenantViewModel,
                    buildingViewModel = buildingViewModel,
                    settingsViewModel = settingsViewModel,
                    tenantId = if (tenantId == 0L) null else tenantId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            // Payment screens
            composable(Screen.Payments.route) {
                PaymentScreen(
                    paymentViewModel = paymentViewModel,
                    tenantViewModel = tenantViewModel,
                    settingsViewModel = settingsViewModel,
                    onTenantClick = { tenantId ->
                        navController.navigate(Screen.TenantPaymentHistory.createRoute(tenantId))
                    }
                )
            }
            composable(
                route = Screen.TenantPaymentHistory.route,
                arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
                TenantPaymentHistoryScreen(
                    tenantViewModel = tenantViewModel,
                    paymentViewModel = paymentViewModel,
                    settingsViewModel = settingsViewModel,
                    tenantId = tenantId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddPayment = { tId ->
                        navController.navigate(Screen.PaymentDetail.createRoute(tId))
                    },
                    onPaymentClick = { paymentId ->
                        navController.navigate(Screen.PaymentEdit.createRoute(paymentId))
                    }
                )
            }
            composable(
                route = Screen.PaymentDetail.route,
                arguments = listOf(navArgument("leaseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("leaseId") ?: 0L
                AddPaymentScreen(
                    viewModel = paymentViewModel,
                    tenantViewModel = tenantViewModel,
                    settingsViewModel = settingsViewModel,
                    tenantId = tenantId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.PaymentEdit.route,
                arguments = listOf(navArgument("paymentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val paymentId = backStackEntry.arguments?.getLong("paymentId") ?: 0L
                PaymentEditScreen(
                    viewModel = paymentViewModel,
                    settingsViewModel = settingsViewModel,
                    paymentId = paymentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Documents screen
            composable(Screen.Documents.route) {
                DocumentsScreen(
                    documentViewModel = documentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    mainActivity = mainActivity
                )
            }

            // Vendor screens
            composable(Screen.Vendors.route) {
                VendorScreen(
                    viewModel = vendorViewModel,
                    onNavigateToDetail = { vendorId ->
                        navController.navigate(
                            if (vendorId == null) "vendor_detail/0"
                            else Screen.VendorDetail.createRoute(vendorId)
                        )
                    }
                )
            }
            composable(
                route = Screen.VendorDetail.route,
                arguments = listOf(navArgument("vendorId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vendorId = backStackEntry.arguments?.getLong("vendorId")
                VendorDetailScreen(
                    viewModel = vendorViewModel,
                    vendorId = if (vendorId == 0L) null else vendorId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Expense screens
            composable(Screen.Expenses.route) {
                ExpenseScreen(
                    expenseViewModel = expenseViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToDetail = { expenseId ->
                        navController.navigate(
                            if (expenseId == null) "expense_detail/0"
                            else Screen.ExpenseDetail.createRoute(expenseId)
                        )
                    }
                )
            }
            composable(
                route = Screen.ExpenseDetail.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId")
                ExpenseDetailScreen(
                    expenseViewModel = expenseViewModel,
                    vendorViewModel = vendorViewModel,
                    buildingViewModel = buildingViewModel,
                    settingsViewModel = settingsViewModel,
                    expenseId = if (expenseId == 0L) null else expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Reports screen
            composable(Screen.Reports.route) {
                ReportsScreen(
                    tenantViewModel = tenantViewModel,
                    paymentViewModel = paymentViewModel,
                    settingsViewModel = settingsViewModel
                )
            }

            // Settings screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    exportImportViewModel = exportImportViewModel,
                    mainActivity = mainActivity,
                    database = database,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
