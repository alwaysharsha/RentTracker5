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
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Building
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.viewmodel.BuildingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingScreen(
    viewModel: BuildingViewModel,
    onNavigateToDetail: (Long?) -> Unit
) {
    val buildings by viewModel.buildings.collectAsState()

    Scaffold(
        topBar = {
            RentTrackerTopBar(title = "Buildings")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToDetail(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Building")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(buildings) { building ->
                BuildingCard(
                    building = building,
                    onClick = { onNavigateToDetail(building.id) }
                )
            }
        }
    }
}

@Composable
fun BuildingCard(
    building: Building,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = building.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Type: ${building.propertyType.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (building.address != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = building.address,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
