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
import com.renttracker.app.ui.viewmodel.TenantViewModel
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import java.text.DecimalFormat

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
    
    val totalPayments = allPayments.sumOf { it.amount }
    val decimalFormat = DecimalFormat("#,##0.00")

    val menuItems = listOf(
        DashboardItem("Owners", Icons.Filled.Person, "owners"),
        DashboardItem("Buildings", Icons.Filled.Home, "buildings"),
        DashboardItem("Tenants", Icons.Filled.Group, "tenants"),
        DashboardItem("Payments", Icons.Filled.Payment, "payments"),
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
            // Stats Cards
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
                    title = "Total Payments",
                    value = "$currencySymbol${decimalFormat.format(totalPayments)}",
                    icon = Icons.Filled.AttachMoney,
                    color = MaterialTheme.colorScheme.secondaryContainer
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
