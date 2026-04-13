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
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponse

    @GET("everything")
    suspend fun getEverythingNews(
        @Query("q") query: String = "finance OR stock market",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponse
}
