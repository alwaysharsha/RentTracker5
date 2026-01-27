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
import androidx.compose.ui.unit.sp
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.ui.viewmodel.TenantViewModel
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import java.text.DecimalFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToScreen: (String) -> Unit
) {
    val activeTenants by tenantViewModel.activeTenants.collectAsState()
    val allPayments by paymentViewModel.allPayments.collectAsState()
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
        DashboardItem("Documents", Icons.Filled.Description, "documents"),
        DashboardItem("Vendors", Icons.Filled.Build, "vendors"),
        DashboardItem("Expenses", Icons.Filled.ContentCut, "expenses"),
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
            // Compact Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactStatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Active",
                    value = activeTenants.size.toString(),
                    icon = Icons.Filled.Group,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                
                CompactStatsCard(
                    modifier = Modifier.weight(1f),
                    title = "This Month",
                    value = "$currencySymbol${decimalFormat.format(totalCurrentMonthPayments)}",
                    icon = Icons.Filled.Payments,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                
                CompactStatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Pending",
                    value = if (totalPendingAmount > 0) "$currencySymbol${decimalFormat.format(totalPendingAmount)}" else "$currencySymbol 0",
                    icon = Icons.Filled.Warning,
                    color = if (totalPendingAmount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
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
fun CompactStatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp,
            hoveredElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                softWrap = true,
                overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                lineHeight = 20.sp
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class DashboardItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
