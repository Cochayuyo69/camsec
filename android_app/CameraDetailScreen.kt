package com.example.mobilesecurityapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Pantalla de detalle que muestra información específica de una cámara seleccionada.
 * Incluye un placeholder para el feed de video y botones de control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(navController: NavController, viewModel: CameraViewModel, cameraId: String) {
    val uiState by viewModel.uiState.collectAsState()
    val camera = uiState.cameras.find { it.id == cameraId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(camera?.name ?: "Detalle de Cámara") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (camera == null) {
                // Mensaje si la cámara no se encuentra
                Text(
                    text = "Cámara no encontrada",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Placeholder para el feed de video
                    VideoFeedPlaceholder(camera)

                    // Información de la cámara
                    CameraInfoCard(camera)

                    // Botones de control
                    ControlButtons(viewModel, camera)
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
 * Placeholder para el feed de video de la cámara.
 * En una implementación real, aquí se integraría ExoPlayer o similar para streaming RTSP.
 */
@Composable
fun VideoFeedPlaceholder(camera: Camera) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📹",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Feed de Video - ${camera.name}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RTSP URL: ${camera.rtspUrl ?: "No configurada"}",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Tarjeta con información detallada de la cámara.
 */
@Composable
fun CameraInfoCard(camera: Camera) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Información de la Cámara",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("ID", camera.id)
            InfoRow("Nombre", camera.name)
            InfoRow("Estado", camera.status)
            camera.lastActivity?.let { timestamp ->
                InfoRow("Última Actividad", formatTimestamp(timestamp))
            }
            camera.wifiSsid?.let { ssid ->
                InfoRow("WiFi SSID", ssid)
            }
            camera.bluetoothAddress?.let { address ->
                InfoRow("Dirección Bluetooth", address)
            }
        }
    }
}

/**
 * Fila de información con etiqueta y valor.
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Botones de control para la cámara (Activar Alarma y Ver Historial).
 */
@Composable
fun ControlButtons(viewModel: CameraViewModel, camera: Camera) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón para activar alarma
        OutlinedButton(
            onClick = {
                // Acción para activar alarma (placeholder)
                // En implementación real, enviaría comando a la cámara
                viewModel.updateCameraStatus(camera.id, "Alarma Activada")
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Activar Alarma")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Activar Alarma")
        }

        // Botón para ver historial
        OutlinedButton(
            onClick = {
                // Acción para ver historial (placeholder)
                // En implementación real, navegaría a pantalla de historial
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.History, contentDescription = "Ver Historial")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Historial")
        }
    }
}
