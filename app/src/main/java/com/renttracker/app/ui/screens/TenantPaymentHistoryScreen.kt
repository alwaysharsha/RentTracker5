package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.data.model.isPendingPayment
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import com.renttracker.app.ui.viewmodel.TenantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPaymentHistoryScreen(
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel,
    tenantId: Long,
    onNavigateBack: () -> Unit,
    onAddPayment: (Long) -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val tenantFlow = remember(tenantId) { tenantViewModel.getTenantById(tenantId) }
    val paymentsFlow = remember(tenantId) { paymentViewModel.getPaymentsByTenant(tenantId) }

    val tenant by tenantFlow.collectAsState(initial = null)
    val payments by paymentsFlow.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()

    var pendingOnly by rememberSaveable { mutableStateOf(false) }

    val displayedPayments = remember(payments, pendingOnly) {
        if (!pendingOnly) {
            payments
        } else {
            payments.filter { it.isPendingPayment() }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = tenant?.name ?: "Payment History",
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddPayment(tenantId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Payment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            tenant?.let { t ->
                // Tenant Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pending only",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Switch(
                                    checked = pendingOnly,
                                    onCheckedChange = { pendingOnly = it }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mobile: ${t.mobile}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        t.rent?.let { rent ->
                            Text(
                                text = "Monthly Rent: ${com.renttracker.app.ui.components.formatCurrency(rent, currency)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Payment Summary
            val totalPaid = displayedPayments.sumOf { it.amount }
            val paymentCount = displayedPayments.size
            val partialPayments = displayedPayments.filter { it.paymentType == PaymentStatus.PARTIAL }
            val partialPaymentCount = partialPayments.size
            val totalPending = partialPayments.sumOf { it.pendingAmount ?: 0.0 }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Compact summary in single row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Payments
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$paymentCount",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Payments",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Divider
                        Divider(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        
                        // Total Amount
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = com.renttracker.app.ui.components.formatCurrency(totalPaid, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Received",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Pending Payment (always show, below received)
                    if (partialPaymentCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pending Payment",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = com.renttracker.app.ui.components.formatCurrency(totalPending, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Payment History
            if (displayedPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payment records found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Payment History",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Group payments by Rent Month
                val groupedPayments = remember(displayedPayments) {
                    displayedPayments
                        .groupBy { payment -> payment.rentMonth }
                        .toSortedMap(compareByDescending { it })
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedPayments.forEach { (rentMonth, paymentsInMonth) ->
                        item {
                            Text(
                                text = dateFormatter.format(Date(rentMonth)),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            items = paymentsInMonth,
                            key = { it.id }
                        ) { payment ->
                            PaymentCard(
                                payment = payment,
                                currency = currency,
                                onClick = { onPaymentClick(payment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
