# TODO: Migración de App Web de Cámaras de Seguridad a Android Nativo

## Pasos Aprobados
- [x] Analizar requisitos y planificar migración a Kotlin/Jetpack Compose con MVVM.
- [x] Recomendar y confirmar uso de Firebase Firestore como base de datos.
- [x] Crear DataClasses.kt: Definir modelos de datos (Camera, etc.).
- [x] Crear ApiService.kt: Interfaz Retrofit para llamadas HTTP (si se combina con backend Node.js para streaming).
- [x] Crear CameraViewModel.kt: Lógica de estado, llamadas a Firebase/Firestore.
- [x] Agregar funcionalidad de conexión WiFi y Bluetooth: Botón en UI, lógica en ViewModel.
- [x] Integrar Firebase en el proyecto Android (build.gradle, google-services.json).
- [x] Implementar pantallas básicas con Jetpack Compose (CameraListScreen, CameraDetailScreen).
- [x] Configurar navegación con Jetpack Navigation.
- [ ] Pruebas en emulador Android.

## Notas
- Base de datos: Firebase Firestore para almacenamiento en la nube de datos de cámaras (id, name, status, lastActivity).
- Arquitectura: MVVM con ViewModel manejando estado y operaciones de DB.
- Streaming: Placeholder por ahora; integrar ExoPlayer o similar para video real más tarde.
