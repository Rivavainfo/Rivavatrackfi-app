package com.rivavafi.universal.domain.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.rivavafi.universal.BuildConfig

data class NewsResponse(
    val articles: List<Article>
)

data class Article(
    val title: String?,
    val description: String?,
    val urlToImage: String?,
    val url: String?
)

interface NewsApi {
    @GET("top-headlines")
    suspend fun getFinanceNews(
        @Query("category") category: String = "business",
        @Query("country") country: String = "in",
        @Query("apiKey") apiKey: String = "110b0e149cb34598b1fb69ba8a78963"
    ): NewsResponse

    @GET("everything")
    suspend fun getEverythingNews(
        @Query("q") query: String = "finance OR stock market",
        @Query("apiKey") apiKey: String = "110b0e149cb34598b1fb69ba8a78963"
    ): NewsResponse
}
