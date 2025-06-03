package com.leafree.app.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leafree.app.model.SensorInfo

@Composable
fun SensorList(sensorList: List<SensorInfo>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .padding(horizontal = 30.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sensorList.size) { index ->
            val sensor = sensorList[index]
            SensorCard(sensor.type, sensor.value, sensor.unit)
        }
    }
}

@Composable
fun SensorCard(type: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(
                text = type,
                modifier = Modifier.align(Alignment.TopStart),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$value $unit",
                modifier = Modifier.align(Alignment.BottomEnd),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

