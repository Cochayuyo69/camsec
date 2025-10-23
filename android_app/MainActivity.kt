package com.example.mobilesecurityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilesecurityapp.ui.theme.MobileSecurityAppTheme

/**
 * Actividad principal de la aplicación MobileSecurityApp.
 * Esta actividad configura Jetpack Compose y la navegación entre pantallas.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita diseño de borde a borde para una experiencia moderna

        setContent {
            MobileSecurityAppTheme {
                // Superficie principal con el tema de Material Design 3
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Configura la navegación y el ViewModel compartido
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Función composable que maneja la navegación de la aplicación.
 * Utiliza Jetpack Navigation para navegar entre las pantallas de lista y detalle de cámaras.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Crea una instancia del ViewModel compartida entre pantallas
    val viewModel: CameraViewModel = viewModel(factory = CameraViewModelFactory(navController.context))

    NavHost(navController = navController, startDestination = "camera_list") {
        // Pantalla de lista de cámaras
        composable("camera_list") {
            CameraListScreen(navController, viewModel)
        }
        // Pantalla de detalle de cámara, recibe el ID como parámetro
        composable("camera_detail/{cameraId}") { backStackEntry ->
            val cameraId = backStackEntry.arguments?.getString("cameraId") ?: ""
            CameraDetailScreen(navController, viewModel, cameraId)
        }
    }
}
