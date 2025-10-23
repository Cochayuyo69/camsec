package com.example.mobilesecurityapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory personalizada para crear instancias de CameraViewModel.
 * Necesaria porque CameraViewModel requiere un Context como parámetro en el constructor.
 */
class CameraViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
