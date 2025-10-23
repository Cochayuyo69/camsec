package com.example.mobilesecurityapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UiState(
    val cameras: List<Camera> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: String? = null // For WiFi/Bluetooth connection feedback
)

class CameraViewModel(private val context: Context) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadCameras()
    }

    fun loadCameras() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val snapshot: QuerySnapshot = db.collection("cameras").get().await()
                val cameras = snapshot.documents.map { doc ->
                    Camera(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        status = doc.getString("status") ?: "Offline",
                        lastActivity = doc.getTimestamp("lastActivity"),
                        rtspUrl = doc.getString("rtspUrl"),
                        wifiSsid = doc.getString("wifiSsid"),
                        bluetoothAddress = doc.getString("bluetoothAddress")
                    )
                }
                _uiState.value = _uiState.value.copy(cameras = cameras, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addCamera(camera: Camera) {
        viewModelScope.launch {
            try {
                db.collection("cameras").document(camera.id).set(camera).await()
                loadCameras() // Reload list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateCameraStatus(id: String, status: String) {
        viewModelScope.launch {
            try {
                db.collection("cameras").document(id).update("status", status).await()
                loadCameras()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // Connect to camera via WiFi
    fun connectToCameraWifi(ssid: String, password: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .apply { password?.let { setWpa2Passphrase(it) } }
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    _uiState.value = _uiState.value.copy(connectionStatus = "Conectado a WiFi: $ssid")
                }

                override fun onUnavailable() {
                    _uiState.value = _uiState.value.copy(connectionStatus = "Error al conectar a WiFi: $ssid")
                }
            })
        } else {
            // For older versions, enable WiFi and attempt connection (simplified)
            if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
            _uiState.value = _uiState.value.copy(connectionStatus = "Intento de conexión a WiFi: $ssid (versión antigua)")
        }
    }

    // Connect to camera via Bluetooth
    fun connectToCameraBluetooth(address: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            _uiState.value = _uiState.value.copy(error = "Permiso de Bluetooth no concedido")
            return
        }

        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(address)
        if (device != null) {
            // Simplified pairing/connection (in real app, handle bonding and socket connection)
            device.createBond()
            _uiState.value = _uiState.value.copy(connectionStatus = "Conectando a Bluetooth: $address")
        } else {
            _uiState.value = _uiState.value.copy(connectionStatus = "Dispositivo Bluetooth no encontrado: $address")
        }
    }
}
