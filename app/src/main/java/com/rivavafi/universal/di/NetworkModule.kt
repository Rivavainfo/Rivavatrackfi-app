package com.rivavafi.universal.di

import com.rivavafi.universal.domain.api.CryptoApiService
import com.rivavafi.universal.domain.api.StockApiService
import com.rivavafi.universal.domain.api.ChatApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("finnhub")
    fun provideFinnhubRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://finnhub.io/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("chat")
    fun provideChatRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // Placeholder base URL for the chatbot API as requested
            .baseUrl("https://api.rivava.in/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStockApiService(@Named("finnhub") retrofit: Retrofit): StockApiService {
        return retrofit.create(StockApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCryptoApiService(@Named("finnhub") retrofit: Retrofit): CryptoApiService {
        return retrofit.create(CryptoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(@Named("chat") retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

}
