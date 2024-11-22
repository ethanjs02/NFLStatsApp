package com.example.nflstatsapp.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // GET request with query parameters
    @GET("/api/player_data")  // The relative path to the endpoint
    suspend fun getPlayerData(
        @Query("player_id") playerId: String,  // Query parameter for player_id
        @Query("team_id") teamId: String,      // Query parameter for team_id
        @Query("pos_id") posId: String         // Query parameter for pos_id
    ): Response<PlayerStats>  // Response type (you can define PlayerDataResponse according to the response from your API)
}