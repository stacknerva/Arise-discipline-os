package com.example.data

import retrofit2.http.GET
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorldTimeResponse(
    val datetime: String,
    val unixtime: Long
)

interface WorldTimeApiService {
    @GET("api/timezone/Etc/UTC")
    suspend fun getCurrentTime(): WorldTimeResponse
}
