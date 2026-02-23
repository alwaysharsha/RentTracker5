package com.renttracker.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.renttracker.app.data.model.*
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.Spinner
import com.renttracker.app.ui.components.formatCurrency
import com.renttracker.app.ui.components.formatDate
import com.renttracker.app.ui.viewmodel.*
import com.renttracker.app.utils.PdfGenerator
import java.io.File

enum class ReportType(val displayName: String) {
    ACTIVE_TENANTS("Active Tenants"),
    CHECKOUT_TENANTS("Checked Out Tenants"),
    ALL_PAYMENTS("All Payments"),
    PAYMENT_PENDING("Pending Payments"),
    INCOME_BY_BUILDING("Income by Building"),
    INCOME_BY_OWNER("Income by Owner"),
    RENT_ROLL("Rent Roll"),
    ALL_EXPENSES("All Expenses"),
    EXPENSES_BY_CATEGORY("Expenses by Category")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    tenantViewModel: TenantViewModel,
    paymentViewModel: PaymentViewModel,
    buildingViewModel: BuildingViewModel,
    ownerViewModel: OwnerViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToTransactionHistory: () -> Unit = {}
) {
    var selectedReportType by remember { mutableStateOf(ReportType.ACTIVE_TENANTS) }
    var showReportList by remember { mutableStateOf(false) }
    val currency by settingsViewModel.currency.collectAsState()

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = "Reports",
                actions = {
                    Button(
                        onClick = onNavigateToTransactionHistory,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Transaction History")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Report Type Selector - Collapsible Left Side
                androidx.compose.animation.AnimatedVisibility(
                    visible = showReportList,
                    enter = androidx.compose.animation.slideInHorizontally(),
                    exit = androidx.compose.animation.slideOutHorizontally()
                ) {
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .fillMaxHeight()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ReportType.values().size) { index ->
                                val reportType = ReportType.values()[index]
                                val isSelected = selectedReportType == reportType
                                
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            selectedReportType = reportType
                                            showReportList = false
                                        },
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = reportType.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (index < ReportType.values().size - 1) {
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // Report Content - Right Side
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    when (selectedReportType) {
                ReportType.ACTIVE_TENANTS -> {
                    val tenants by tenantViewModel.activeTenants.collectAsState()
                    TenantReport(tenants = tenants, reportTitle = "Active Tenants Report")
                }
                ReportType.CHECKOUT_TENANTS -> {
                    val tenants by tenantViewModel.checkedOutTenants.collectAsState()
                    TenantReport(tenants = tenants, reportTitle = "Checked Out Tenants Report")
                }
                ReportType.ALL_PAYMENTS -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    val activeTenants by tenantViewModel.activeTenants.collectAsState()
                    val checkedOutTenants by tenantViewModel.checkedOutTenants.collectAsState()
                    val allTenants = remember(activeTenants, checkedOutTenants) { activeTenants + checkedOutTenants }
                    
                    PaymentReport(
                        allPayments = payments,
                        tenants = allTenants,
                        currency = currency
                    )
                }
                ReportType.PAYMENT_PENDING -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    val activeTenants by tenantViewModel.activeTenants.collectAsState()
                    val checkedOutTenants by tenantViewModel.checkedOutTenants.collectAsState()
                    val allTenants = remember(activeTenants, checkedOutTenants) { activeTenants + checkedOutTenants }
                    
                    PendingPaymentReport(
                        allPayments = payments, 
                        tenants = allTenants,
                        currency = currency
                    )
                }
                ReportType.INCOME_BY_BUILDING -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    val buildings by buildingViewModel.buildings.collectAsState()
                    val activeTenants by tenantViewModel.activeTenants.collectAsState()
                    val checkedOutTenants by tenantViewModel.checkedOutTenants.collectAsState()
                    val allTenants = remember(activeTenants, checkedOutTenants) { activeTenants + checkedOutTenants }
                    
                    IncomeByBuildingReport(
                        payments = payments,
                        buildings = buildings,
                        tenants = allTenants,
                        currency = currency
                    )
                }
                ReportType.INCOME_BY_OWNER -> {
                    val payments by paymentViewModel.allPayments.collectAsState()
                    val owners by ownerViewModel.owners.collectAsState()
                    val buildings by buildingViewModel.buildings.collectAsState()
                    val activeTenants by tenantViewModel.activeTenants.collectAsState()
                    val checkedOutTenants by tenantViewModel.checkedOutTenants.collectAsState()
                    val allTenants = remember(activeTenants, checkedOutTenants) { activeTenants + checkedOutTenants }
                    
                    IncomeByOwnerReport(
                        payments = payments,
                        owners = owners,
                        buildings = buildings,
                        tenants = allTenants,
                        currency = currency
                    )
                }
                ReportType.RENT_ROLL -> {
                    val activeTenants by tenantViewModel.activeTenants.collectAsState()
                    val buildings by buildingViewModel.buildings.collectAsState()
                    
                    RentRollReport(
                        tenants = activeTenants,
                        buildings = buildings,
                        currency = currency
                    )
                }
                ReportType.ALL_EXPENSES -> {
                    Text(
                        text = "Expense reports coming soon",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                ReportType.EXPENSES_BY_CATEGORY -> {
                    Text(
                        text = "Expense category reports coming soon",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
                }
            }
            
            // Floating Action Button to toggle report list
            FloatingActionButton(
                onClick = { showReportList = !showReportList },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (showReportList) Icons.Filled.Close else Icons.Filled.Menu,
                    contentDescription = if (showReportList) "Hide Reports" else "Show Reports"
                )
            }
        }
    }
}

@Composable
fun TenantReport(tenants: List<Tenant>, reportTitle: String) {
    val context = LocalContext.current
    
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
                
                Button(
                    onClick = {
                        val file = PdfGenerator.generateTenantListPdf(
                            context = context,
                            tenants = tenants,
                            reportTitle = reportTitle
                        )
                        
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Report"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as PDF")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(tenants) { tenant ->
                ReportTenantCard(tenant = tenant, onClick = {})
            }
        }
    }
}

@Composable
fun ReportTenantCard(
    tenant: Tenant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = tenant.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (tenant.email != null) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tenant.email, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = tenant.mobile, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PaymentReport(
    allPayments: List<Payment>,
    tenants: List<Tenant>,
    currency: String
) {
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    val context = LocalContext.current
    
    // Filter payments
    val filteredPayments = remember(allPayments, selectedTenant) {
        val tenantId = selectedTenant?.id
        if (tenantId == null) {
            allPayments
        } else {
            allPayments.filter { it.tenantId == tenantId }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tenant Filter
        Spinner(
            label = "Filter by Tenant",
            items = listOf(null) + tenants,
            selectedItem = selectedTenant,
            onItemSelected = { selectedTenant = it },
            itemToString = { it?.name ?: "All Tenants" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Export Button
        if (filteredPayments.isNotEmpty()) {
            Button(
                onClick = {
                    val currentTenant = selectedTenant
                    val reportTitle = if (currentTenant != null) 
                        "All Payments - ${currentTenant.name}" 
                    else 
                        "All Payments Report"
                        
                    val file = PdfGenerator.generatePaymentReportPdf(
                        context = context,
                        payments = filteredPayments,
                        tenants = tenants,
                        reportTitle = reportTitle,
                        currency = currency
                    )
                    
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export as PDF")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (filteredPayments.isEmpty()) {
            Text(
                text = "No payments found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val totalAmount = filteredPayments.sumOf { it.amount }
            val fullPayments = filteredPayments.count { it.paymentType == PaymentStatus.FULL }
            val partialPayments = filteredPayments.count { it.paymentType == PaymentStatus.PARTIAL }
            
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
                            Text("Total Payments: ${filteredPayments.size}")
                            Text("Full Payments: $fullPayments")
                            Text("Partial Payments: $partialPayments")
                            Text("Total Amount: ${formatCurrency(totalAmount, currency)}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(filteredPayments) { payment ->
                    PaymentCard(payment = payment, currency = currency, onClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingPaymentReport(
    allPayments: List<Payment>,
    tenants: List<Tenant>,
    currency: String
) {
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    val context = LocalContext.current
    
    // Filter payments
    val filteredPayments = remember(allPayments, selectedTenant) {
        val currentTenant = selectedTenant
        allPayments.filter { payment ->
            payment.paymentType == PaymentStatus.PARTIAL &&
            (currentTenant == null || payment.tenantId == currentTenant.id)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tenant Filter
        Spinner(
            label = "Filter by Tenant",
            items = listOf(null) + tenants,
            selectedItem = selectedTenant,
            onItemSelected = { selectedTenant = it },
            itemToString = { it?.name ?: "All Tenants" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Export Button
        if (filteredPayments.isNotEmpty()) {
            Button(
                onClick = {
                    val currentTenant = selectedTenant
                    val reportTitle = if (currentTenant != null) 
                        "Pending Payments - ${currentTenant.name}" 
                    else 
                        "Pending Payments Report"
                        
                    val file = PdfGenerator.generatePaymentReportPdf(
                        context = context,
                        payments = filteredPayments,
                        tenants = tenants,
                        reportTitle = reportTitle,
                        currency = currency
                    )
                    
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export as PDF")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredPayments.isEmpty()) {
            Text(
                text = "No pending payments found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val totalPendingAmount = filteredPayments.sumOf { it.pendingAmount ?: 0.0 }
            val totalPaidAmount = filteredPayments.sumOf { it.amount }
            
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
                            Text("Total Partial Payments: ${filteredPayments.size}")
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
                items(filteredPayments) { payment ->
                    PaymentCard(payment = payment, currency = currency, onClick = {})
                }
            }
        }
    }
}

@Composable
fun IncomeByBuildingReport(
    payments: List<Payment>,
    buildings: List<Building>,
    tenants: List<Tenant>,
    currency: String
) {
    val context = LocalContext.current
    
    // Calculate income by building
    val buildingIncomeMap = remember(payments, buildings, tenants) {
        buildings.associateWith { building ->
            val buildingTenants = tenants.filter { it.buildingId == building.id }
            val tenantIds = buildingTenants.map { it.id }
            payments.filter { it.tenantId in tenantIds }.sumOf { it.amount }
        }
    }
    
    Column {
        Text(
            text = "Income by Building Report",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = {
                val file = PdfGenerator.generateIncomeByBuildingPdf(
                    context = context,
                    buildingIncomeMap = buildingIncomeMap,
                    currency = currency
                )
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export as PDF")
        }
    }
}

@Composable
fun IncomeByOwnerReport(
    payments: List<Payment>,
    owners: List<Owner>,
    buildings: List<Building>,
    tenants: List<Tenant>,
    currency: String
) {
    val context = LocalContext.current
    
    // Calculate income by owner
    val ownerIncomeMap = remember(payments, owners, buildings, tenants) {
        owners.associateWith { owner ->
            val ownerBuildings = buildings.filter { it.ownerId == owner.id }
            val buildingIds = ownerBuildings.map { it.id }
            val buildingTenants = tenants.filter { it.buildingId in buildingIds }
            val tenantIds = buildingTenants.map { it.id }
            payments.filter { it.tenantId in tenantIds }.sumOf { it.amount }
        }
    }
    
    Column {
        Text(
            text = "Income by Owner Report",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = {
                val file = PdfGenerator.generateIncomeByOwnerPdf(
                    context = context,
                    ownerIncomeMap = ownerIncomeMap,
                    currency = currency
                )
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export as PDF")
        }
    }
}

@Composable
fun RentRollReport(
    tenants: List<Tenant>,
    buildings: List<Building>,
    currency: String
) {
    val context = LocalContext.current
    Column {
        Text(
            text = "Rent Roll Report",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = {
                val file = PdfGenerator.generateRentRollPdf(
                    context = context,
                    tenants = tenants,
                    buildings = buildings,
                    currency = currency
                )
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export as PDF")
        }
    }
}
