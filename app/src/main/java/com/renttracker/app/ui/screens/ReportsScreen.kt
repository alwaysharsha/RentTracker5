package com.renttracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.*
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.formatCurrency
import com.renttracker.app.ui.components.formatDate
import com.renttracker.app.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedReportType by remember { mutableStateOf(ReportType.ACTIVE_TENANTS) }
    val currency by settingsViewModel.currency.collectAsState()

    Scaffold(
        topBar = {
            RentTrackerTopBar(title = "Reports")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Report Type Selector
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportType.values().forEach { reportType ->
                    FilterChip(
                        selected = selectedReportType == reportType,
                        onClick = { selectedReportType = reportType },
                        label = { Text(reportType.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Report Content
            when (selectedReportType) {
                ReportType.ACTIVE_TENANTS -> {
                    val tenants by tenantViewModel.activeTenants.collectAsState()
                    TenantReport(tenants = tenants)
                }
                ReportType.CHECKOUT_TENANTS -> {
                    val tenants by tenantViewModel.checkedOutTenants.collectAsState()
                    TenantReport(tenants = tenants)
                }
                ReportType.ALL_PAYMENTS -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    PaymentReport(payments = payments, currency = currency)
                }
                ReportType.PAYMENT_PENDING -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    val pendingPayments = payments.filter { it.paymentType == PaymentStatus.PARTIAL }
                    PendingPaymentReport(payments = pendingPayments, currency = currency)
                }
            }
        }
    }
}

@Composable
fun TenantReport(tenants: List<Tenant>) {
    if (tenants.isEmpty()) {
        Text(
            text = "No tenants found",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Total Tenants: ${tenants.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(tenants) { tenant ->
                TenantCard(tenant = tenant, onClick = {})
            }
        }
    }
}

@Composable
fun PaymentReport(payments: List<Payment>, currency: String) {
    if (payments.isEmpty()) {
        Text(
            text = "No payments found",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        val totalAmount = payments.sumOf { it.amount }
        val fullPayments = payments.count { it.paymentType == PaymentStatus.FULL }
        val partialPayments = payments.count { it.paymentType == PaymentStatus.PARTIAL }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Payments: ${payments.size}")
                        Text("Full Payments: $fullPayments")
                        Text("Partial Payments: $partialPayments")
                        Text("Total Amount: ${formatCurrency(totalAmount, currency)}")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(payments) { payment ->
                PaymentCard(payment = payment, currency = currency, onClick = {})
            }
        }
    }
}

enum class ReportType(val displayName: String) {
    ACTIVE_TENANTS("Active Tenants"),
    CHECKOUT_TENANTS("Checked Out Tenants"),
    ALL_PAYMENTS("All Payments"),
    PAYMENT_PENDING("Pending Payments")
}

@Composable
fun PendingPaymentReport(payments: List<Payment>, currency: String) {
    if (payments.isEmpty()) {
        Text(
            text = "No pending payments found",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        val totalPendingAmount = payments.sumOf { it.pendingAmount ?: 0.0 }
        val totalPaidAmount = payments.sumOf { it.amount }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pending Payments Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Partial Payments: ${payments.size}")
                        Text("Total Paid Amount: ${formatCurrency(totalPaidAmount, currency)}")
                        Text(
                            text = "Total Pending Amount: ${formatCurrency(totalPendingAmount, currency)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(payments) { payment ->
                PaymentCard(payment = payment, currency = currency, onClick = {})
            }
        }
    }
}
