package com.renttracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Vendor
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorScreen(
    viewModel: VendorViewModel,
    onNavigateToDetail: (Long?) -> Unit
) {
    val vendors by viewModel.vendors.collectAsState()

    Scaffold(
        topBar = {
            RentTrackerTopBar(title = "Vendors")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToDetail(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Vendor")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (vendors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No vendors found.\nTap + to add a vendor.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Total Vendors: ${vendors.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vendors) { vendor ->
                        VendorCard(
                            vendor = vendor,
                            onClick = { onNavigateToDetail(vendor.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VendorCard(
    vendor: Vendor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = vendor.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vendor.category.name.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            vendor.phone?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Phone: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            vendor.email?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Email: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
