package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.domain.api.Article
import com.rivavafi.universal.domain.api.NewsApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor() {
    private val api = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsApi::class.java)

    suspend fun getNews(): List<Article> {
        return try {
            api.getFinanceNews().articles
        } catch (e: Exception) {
            emptyList()
        }
    }
}
