package com.rivavafi.universal.domain.api

data class MarketItem(
    val symbol: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val type: String // stock or crypto
)

data class News(
    val title: String,
    val description: String,
    val imageUrl: String,
    val source: String,
    val url: String,
    val publishedAt: Long
)
