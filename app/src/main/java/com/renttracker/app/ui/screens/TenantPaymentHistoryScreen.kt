package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Payment
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
    val tenant by tenantViewModel.getTenantById(tenantId).collectAsState(initial = null)
    val payments by paymentViewModel.getPaymentsByTenant(tenantId).collectAsState()
    val currency by settingsViewModel.currency.collectAsState()

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
                        Text(
                            text = t.name,
                            style = MaterialTheme.typography.titleLarge
                        )
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
            val totalPaid = payments.sumOf { it.amount }
            val paymentCount = payments.size
            val partialPayments = payments.filter { it.paymentType == com.renttracker.app.data.model.PaymentStatus.PARTIAL }
            val partialPaymentCount = partialPayments.size
            val totalPartialAmount = partialPayments.sumOf { it.amount }
            val totalPending = partialPayments.sumOf { it.pendingAmount ?: 0.0 }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Summary",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total Payments",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = paymentCount.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = androidx.compose.ui.Alignment.End
                        ) {
                            Text(
                                text = "Total Amount",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = com.renttracker.app.ui.components.formatCurrency(totalPaid, currency),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Partial Payments",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = partialPaymentCount.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (partialPaymentCount > 0) 
                                    MaterialTheme.colorScheme.tertiary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = androidx.compose.ui.Alignment.End
                        ) {
                            Text(
                                text = "Total Partial Payments",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = com.renttracker.app.ui.components.formatCurrency(totalPartialAmount, currency),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (partialPaymentCount > 0) 
                                    MaterialTheme.colorScheme.tertiary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (totalPending > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    text = "Total Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = com.renttracker.app.ui.components.formatCurrency(totalPending, currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Payment History
            if (payments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
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

                // Group payments by Month-Year
                val dateFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val groupedPayments = payments.groupBy { payment ->
                    dateFormatter.format(Date(payment.date))
                }.toSortedMap(compareByDescending { 
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(it)
                })

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedPayments.forEach { (monthYear, paymentsInMonth) ->
                        item {
                            Text(
                                text = monthYear,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(paymentsInMonth.size) { index ->
                            PaymentCard(
                                payment = paymentsInMonth[index],
                                currency = currency,
                                onClick = { onPaymentClick(paymentsInMonth[index].id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
