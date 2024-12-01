package com.example.nflstatsapp.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/" // Replace with your base URL

    // Creating the Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Set the base URL
            .addConverterFactory(GsonConverterFactory.create())  // Add the Gson converter to parse the response
            .build()
    }

    // Create and return an instance of ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
