package com.example.data

import retrofit2.http.GET
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteApiResponse(
    val quotes: List<QuoteDto>
)

@JsonClass(generateAdapter = true)
data class QuoteDto(
    val quote: String,
    val author: String,
    val source: String,
    val category: String
)

interface QuoteApiService {
    @GET("7JLHL53BA.txt")
    suspend fun getQuotes(): QuoteApiResponse
}
