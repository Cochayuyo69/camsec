package com.example.mobilesecurityapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal que muestra la lista de cámaras de seguridad.
 * Incluye un indicador de carga, mensajes de error, y un botón flotante para agregar cámaras.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraListScreen(navController: NavController, viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cámaras de Seguridad") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Acción para agregar nueva cámara (placeholder por ahora)
                // En una implementación completa, navegaría a una pantalla de agregar
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cámara")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Indicador de carga centrado
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    // Mensaje de error con opción de reintentar
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCameras() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    // Lista de cámaras
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.cameras) { camera ->
                            CameraCard(camera, navController, viewModel)
                        }
                    }
                }
            }

            // Mostrar estado de conexión si existe
            uiState.connectionStatus?.let { status ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(status)
                }
            }
        }
    }
}

/**
 * Tarjeta individual para representar una cámara en la lista.
 * Muestra nombre, estado, última actividad y botones de conexión.
 */
@Composable
fun CameraCard(camera: Camera, navController: NavController, viewModel: CameraViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("camera_detail/${camera.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = camera.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Indicador de estado con color
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (camera.status == "Online") Color.Green else Color.Red,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estado: ${camera.status}",
                style = MaterialTheme.typography.bodyMedium
            )

            camera.lastActivity?.let { timestamp ->
                Text(
                    text = "Última actividad: ${formatTimestamp(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de conexión WiFi y Bluetooth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                camera.wifiSsid?.let { ssid ->
                    OutlinedButton(
                        onClick = { viewModel.connectToCameraWifi(ssid) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = "Conectar WiFi")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WiFi")
                    }
                }

                camera.bluetoothAddress?.let { address ->
                    OutlinedButton(
                        onClick = { viewModel.connectToCameraBluetooth(address) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Conectar Bluetooth")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BT")
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para formatear timestamps de Firebase en un formato legible.
 */
fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
