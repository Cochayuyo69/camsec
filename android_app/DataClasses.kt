package com.example.mobilesecurityapp

import com.google.firebase.Timestamp

data class Camera(
    val id: String = "",
    val name: String = "",
    val status: String = "Offline", // "Online" or "Offline"
    val lastActivity: Timestamp? = null, // Firebase Timestamp for last activity
    val rtspUrl: String? = null, // Optional RTSP URL for streaming
    val wifiSsid: String? = null, // WiFi SSID for connection
    val bluetoothAddress: String? = null // Bluetooth MAC address for connection
)
