package com.example.mobilesecurityapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

// Assuming the Node.js backend has REST endpoints for cameras
interface ApiService {
    @GET("cameras")
    suspend fun getCameras(): Response<List<Camera>>

    @GET("cameras/{id}")
    suspend fun getCamera(@Path("id") id: String): Response<Camera>

    companion object {
        private const val BASE_URL = "http://168.174.41.93:3000/" // Android emulator localhost

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
