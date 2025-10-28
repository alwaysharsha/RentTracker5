package com.renttracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.ui.viewmodel.TenantViewModel
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.VendorViewModel
import com.renttracker.app.ui.viewmodel.ExpenseViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import java.text.DecimalFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    vendorViewModel: VendorViewModel,
    expenseViewModel: ExpenseViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToScreen: (String) -> Unit
) {
    val activeTenants by tenantViewModel.activeTenants.collectAsState()
    val allPayments by paymentViewModel.allPayments.collectAsState()
    val vendors by vendorViewModel.vendors.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    
    val currencySymbol = when (currency) {
        "USD", "CAD" -> "$"
        "GBP" -> "£"
        "EUR" -> "€"
        "INR" -> "₹"
        "JPY" -> "¥"
        "CNY" -> "¥"
        "AUD" -> "$"
        else -> "$"
    }
    
    // Calculate current month payments
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    val currentMonthPayments = allPayments.filter { payment ->
        val paymentCalendar = Calendar.getInstance().apply {
            timeInMillis = payment.date
        }
        paymentCalendar.get(Calendar.MONTH) == currentMonth &&
        paymentCalendar.get(Calendar.YEAR) == currentYear
    }
    
    val totalCurrentMonthPayments = currentMonthPayments.sumOf { it.amount }
    val totalPendingAmount = allPayments
        .filter { it.paymentType == PaymentStatus.PARTIAL }
        .sumOf { it.pendingAmount ?: 0.0 }
    
    val decimalFormat = DecimalFormat("#,##0.00")

    val menuItems = listOf(
        DashboardItem("Owners", Icons.Filled.Person, "owners"),
        DashboardItem("Buildings", Icons.Filled.Home, "buildings"),
        DashboardItem("Tenants", Icons.Filled.Group, "tenants"),
        DashboardItem("Payments", Icons.Filled.Payment, "payments"),
        DashboardItem("Vendors", Icons.Filled.Build, "vendors"),
        DashboardItem("Expenses", Icons.Filled.MoneyOff, "expenses"),
        DashboardItem("Reports", Icons.Filled.Assessment, "reports"),
        DashboardItem("Settings", Icons.Filled.Settings, "settings")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent Tracker") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Tenants",
                    value = activeTenants.size.toString(),
                    icon = Icons.Filled.Group,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "This Month",
                    value = "$currencySymbol${decimalFormat.format(totalCurrentMonthPayments)}",
                    icon = Icons.Filled.Payments,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            
            // Stats Cards Row 2 - Vendors and Expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Vendors",
                    value = vendors.size.toString(),
                    icon = Icons.Filled.Build,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
                
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Expenses",
                    value = "$currencySymbol${decimalFormat.format(expenses.sumOf { it.amount })}",
                    icon = Icons.Filled.MoneyOff,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                )
            }
            
            // Pending Payments Card
            if (totalPendingAmount > 0) {
                StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Total Pending",
                    value = "$currencySymbol${decimalFormat.format(totalPendingAmount)}",
                    icon = Icons.Filled.Warning,
                    color = MaterialTheme.colorScheme.errorContainer
                )
            }
            
            // Menu Grid
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(menuItems) { item ->
                    MenuCard(
                        item = item,
                        onClick = { onNavigateToScreen(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MenuCard(
    item: DashboardItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class DashboardItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
