package com.example.spotify.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApiService {
    @GET("v1/me/top/tracks")
    fun getTopTracks(
        @Header("Authorization") authToken: String,
        @Query("limit") limit: Int = 10
    ): Call<SpotifyTracksResponse>
}
