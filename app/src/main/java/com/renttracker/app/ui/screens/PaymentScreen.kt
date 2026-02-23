package com.renttracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.formatCurrency
import com.renttracker.app.ui.components.formatDate
import com.renttracker.app.ui.viewmodel.TenantViewModel
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    paymentViewModel: PaymentViewModel,
    tenantViewModel: TenantViewModel,
    settingsViewModel: SettingsViewModel,
    onTenantClick: (Long) -> Unit
) {
    val activeTenants by tenantViewModel.activeTenants.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()

    Scaffold(
        topBar = {
            RentTrackerTopBar(title = "Payments")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (activeTenants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No active tenants found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Select Tenant to View Payment History",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = activeTenants,
                        key = { tenant -> tenant.id }
                    ) { tenant ->
                        TenantPaymentCard(
                            tenant = tenant,
                            paymentViewModel = paymentViewModel,
                            currency = currency,
                            onClick = { onTenantClick(tenant.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantPaymentCard(
    tenant: Tenant,
    paymentViewModel: PaymentViewModel,
    currency: String,
    onClick: () -> Unit
) {
    // Use remember with key to avoid recalculating on every recomposition
    val payments by remember(tenant.id) {
        paymentViewModel.getPaymentsByTenant(tenant.id)
    }.collectAsState(initial = emptyList())
    
    // Use derivedStateOf to avoid unnecessary recompositions when stats don't change
    val paymentStats by remember {
        derivedStateOf {
            val lastPayment = payments.maxByOrNull { it.rentMonth }
            PaymentStats(
                count = payments.size,
                lastRentMonth = lastPayment?.rentMonth
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tenant.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tenant.mobile,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                tenant.rent?.let { rent ->
                    Text(
                        text = "Rent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.renttracker.app.ui.components.formatCurrency(rent, currency),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "${paymentStats.count} Payments",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                paymentStats.lastRentMonth?.let { lastMonth ->
                    Text(
                        text = "Last: ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(lastMonth))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun PaymentCard(
    payment: Payment,
    currency: String,
    onClick: () -> Unit
) {
    val backgroundColor = if (payment.paymentType == PaymentStatus.PARTIAL) {
        Color.Red.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rent: ${SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(payment.rentMonth))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Paid: ${formatDate(payment.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = formatCurrency(payment.amount, currency),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (payment.paymentType == PaymentStatus.PARTIAL) 
                            Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    if (payment.paymentType == PaymentStatus.PARTIAL && payment.pendingAmount != null && payment.pendingAmount > 0) {
                        Text(
                            text = "Pending: ${formatCurrency(payment.pendingAmount, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Method: ${payment.paymentMethod} | Status: ${payment.paymentType.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class PaymentStats(
    val count: Int,
    val lastRentMonth: Long?
)
