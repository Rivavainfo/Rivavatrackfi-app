package com.rivavafi.di

import com.rivavafi.data.network.GoogleAppsScriptApi
import com.rivavafi.data.network.YahooFinanceApi
import com.rivavafi.domain.api.CryptoApiService
import com.rivavafi.domain.api.StockApiService
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
    fun provideYahooFinanceApi(client: OkHttpClient): YahooFinanceApi {
        return Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YahooFinanceApi::class.java)
    }

    @Provides
    @Singleton
    @Named("finnhub")
    fun provideFinnhubRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://finnhub.io/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("coingecko")
    fun provideCoinGeckoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/")
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
    fun provideCryptoApiService(@Named("coingecko") retrofit: Retrofit): CryptoApiService {
        return retrofit.create(CryptoApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("googleAppsScript")
    fun provideGoogleAppsScriptRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .client(okHttpClient.newBuilder().followRedirects(true).followSslRedirects(true).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleAppsScriptApi(@Named("googleAppsScript") retrofit: Retrofit): GoogleAppsScriptApi {
        return retrofit.create(GoogleAppsScriptApi::class.java)
    }
}
