package com.renttracker.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.renttracker.app.data.model.*
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.formatCurrency
import com.renttracker.app.ui.components.formatDate
import com.renttracker.app.ui.viewmodel.*
import com.renttracker.app.utils.PdfGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    paymentViewModel: PaymentViewModel,
    tenantViewModel: TenantViewModel,
    buildingViewModel: BuildingViewModel,
    ownerViewModel: OwnerViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val allPayments by paymentViewModel.allPayments.collectAsState()
    val allTenants by tenantViewModel.activeTenants.collectAsState()
    val allBuildings by buildingViewModel.buildings.collectAsState()
    val allOwners by ownerViewModel.owners.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    var selectedOwner by remember { mutableStateOf<Owner?>(null) }
    var selectedPaymentType by remember { mutableStateOf<PaymentStatus?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    var expandedTenant by remember { mutableStateOf(false) }
    var expandedBuilding by remember { mutableStateOf(false) }
    var expandedOwner by remember { mutableStateOf(false) }
    
    val paymentMethods by settingsViewModel.paymentMethods.collectAsState()
    
    // Filter payments based on selected criteria
    val filteredPayments = remember(
        allPayments,
        selectedTenant,
        selectedBuilding,
        selectedOwner,
        selectedPaymentType,
        selectedPaymentMethod,
        startDate,
        endDate
    ) {
        var filtered = allPayments
        
        // Filter by tenant
        selectedTenant?.let { tenant ->
            filtered = filtered.filter { payment -> payment.tenantId == tenant.id }
        }
        
        // Filter by building
        selectedBuilding?.let { building ->
            val buildingTenantIds = allTenants.filter { tenant -> tenant.buildingId == building.id }.map { tenant -> tenant.id }
            filtered = filtered.filter { payment -> payment.tenantId in buildingTenantIds }
        }
        
        // Filter by owner
        selectedOwner?.let { owner ->
            val ownerBuildingIds = allBuildings.filter { building -> building.ownerId == owner.id }.map { building -> building.id }
            val ownerTenantIds = allTenants.filter { tenant -> tenant.buildingId in ownerBuildingIds }.map { tenant -> tenant.id }
            filtered = filtered.filter { payment -> payment.tenantId in ownerTenantIds }
        }
        
        // Filter by payment type
        selectedPaymentType?.let { type ->
            filtered = filtered.filter { payment -> payment.paymentType == type }
        }
        
        // Filter by payment method
        selectedPaymentMethod?.let { method ->
            filtered = filtered.filter { payment -> payment.paymentMethod == method }
        }
        
        // Filter by date range
        startDate?.let { start ->
            filtered = filtered.filter { payment -> payment.date >= start }
        }
        endDate?.let { end ->
            filtered = filtered.filter { payment -> payment.date <= end }
        }
        
        filtered.sortedByDescending { payment -> payment.date }
    }
    
    val totalAmount = filteredPayments.sumOf { it.amount }
    val totalPending = filteredPayments.sumOf { it.pendingAmount ?: 0.0 }
    
    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = "Transaction History",
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Filled.FilterAltOff else Icons.Filled.FilterAlt,
                            contentDescription = "Toggle Filters"
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = PdfGenerator.generatePaymentReportPdf(
                                        context = context,
                                        payments = filteredPayments,
                                        tenants = allTenants,
                                        reportTitle = "Transaction History Report",
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
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        enabled = filteredPayments.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Export Report")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters Section
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Tenant Filter
                        ExposedDropdownMenuBox(
                            expanded = expandedTenant,
                            onExpandedChange = { expandedTenant = it }
                        ) {
                            OutlinedTextField(
                                value = selectedTenant?.name ?: "All Tenants",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tenant") },
                                trailingIcon = {
                                    if (selectedTenant != null) {
                                        IconButton(onClick = { selectedTenant = null }) {
                                            Icon(Icons.Filled.Clear, "Clear")
                                        }
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenant)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTenant,
                                onDismissRequest = { expandedTenant = false }
                            ) {
                                allTenants.forEach { tenant ->
                                    DropdownMenuItem(
                                        text = { Text(tenant.name) },
                                        onClick = {
                                            selectedTenant = tenant
                                            expandedTenant = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Building Filter
                        ExposedDropdownMenuBox(
                            expanded = expandedBuilding,
                            onExpandedChange = { expandedBuilding = it }
                        ) {
                            OutlinedTextField(
                                value = selectedBuilding?.name ?: "All Buildings",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Building") },
                                trailingIcon = {
                                    if (selectedBuilding != null) {
                                        IconButton(onClick = { selectedBuilding = null }) {
                                            Icon(Icons.Filled.Clear, "Clear")
                                        }
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBuilding,
                                onDismissRequest = { expandedBuilding = false }
                            ) {
                                allBuildings.forEach { building ->
                                    DropdownMenuItem(
                                        text = { Text(building.name) },
                                        onClick = {
                                            selectedBuilding = building
                                            expandedBuilding = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Owner Filter
                        ExposedDropdownMenuBox(
                            expanded = expandedOwner,
                            onExpandedChange = { expandedOwner = it }
                        ) {
                            OutlinedTextField(
                                value = selectedOwner?.name ?: "All Owners",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Owner") },
                                trailingIcon = {
                                    if (selectedOwner != null) {
                                        IconButton(onClick = { selectedOwner = null }) {
                                            Icon(Icons.Filled.Clear, "Clear")
                                        }
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOwner)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedOwner,
                                onDismissRequest = { expandedOwner = false }
                            ) {
                                allOwners.forEach { owner ->
                                    DropdownMenuItem(
                                        text = { Text(owner.name) },
                                        onClick = {
                                            selectedOwner = owner
                                            expandedOwner = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Payment Type Filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPaymentType == PaymentStatus.FULL,
                                onClick = {
                                    selectedPaymentType = if (selectedPaymentType == PaymentStatus.FULL) null else PaymentStatus.FULL
                                },
                                label = { Text("Full") }
                            )
                            FilterChip(
                                selected = selectedPaymentType == PaymentStatus.PARTIAL,
                                onClick = {
                                    selectedPaymentType = if (selectedPaymentType == PaymentStatus.PARTIAL) null else PaymentStatus.PARTIAL
                                },
                                label = { Text("Partial") }
                            )
                        }
                        
                        // Clear All Filters Button
                        Button(
                            onClick = {
                                selectedTenant = null
                                selectedBuilding = null
                                selectedOwner = null
                                selectedPaymentType = null
                                selectedPaymentMethod = null
                                startDate = null
                                endDate = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Filters")
                        }
                    }
                }
            }
            
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${filteredPayments.size}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatCurrency(totalAmount, currency),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total Paid",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (totalPending > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatCurrency(totalPending, currency),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // Transactions List
            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredPayments,
                        key = { payment -> payment.id }
                    ) { payment ->
                        TransactionCard(
                            payment = payment,
                            tenant = allTenants.find { tenant -> tenant.id == payment.tenantId },
                            currency = currency,
                            onClick = { onPaymentClick(payment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    payment: Payment,
    tenant: Tenant?,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tenant?.name ?: "Unknown Tenant",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Rent: ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(payment.rentMonth))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Paid: ${formatDate(payment.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(payment.amount, currency),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (payment.paymentType == PaymentStatus.PARTIAL)
                            Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    if (payment.pendingAmount != null && payment.pendingAmount > 0) {
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
