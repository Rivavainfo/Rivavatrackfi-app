package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    private val _cryptoState = MutableStateFlow<UiState<List<MarketItem>>>(UiState.Loading)
    val cryptoState: StateFlow<UiState<List<MarketItem>>> = _cryptoState

    private val _stockState = MutableStateFlow<UiState<List<MarketItem>>>(UiState.Loading)
    val stockState: StateFlow<UiState<List<MarketItem>>> = _stockState

    private val _newsState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val newsState: StateFlow<UiState<List<News>>> = _newsState

    private var stockJob: kotlinx.coroutines.Job? = null
    private var newsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            repository.cryptoState.collect { items ->
                if (items.isNotEmpty()) {
                    _cryptoState.value = UiState.Success(items)
                }
            }
        }
        viewModelScope.launch {
            repository.stockState.collect { items ->
                if (items.isNotEmpty()) {
                    _stockState.value = UiState.Success(items)
                } else if (_stockState.value is UiState.Success) {
                    // Do not revert to loading or empty if we already had items and just failed an update gracefully.
                }
            }
        }
    }

    fun startUpdates() {
        startStockUpdates()
        startNewsUpdates()
    }

    fun retryStocks() {
        _stockState.value = UiState.Loading
        startStockUpdates()
    }

    fun retryNews() {
        _newsState.value = UiState.Loading
        startNewsUpdates()
    }

    private fun startStockUpdates() {
        stockJob?.cancel()
        stockJob = viewModelScope.launch {
            while (true) {
                val stocks = repository.fetchStocks()
                if (stocks.isNotEmpty()) {
                    _stockState.value = UiState.Success(stocks)
                } else if (_stockState.value !is UiState.Success) {
                    _stockState.value = UiState.Error("Failed to fetch stock data")
                }
                delay(60000) // Poll stocks every 60s
            }
        }
    }

    private fun startNewsUpdates() {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            while (true) {
                val news = repository.fetchNews()
                if (news.isNotEmpty()) {
                    _newsState.value = UiState.Success(news)
                } else if (_newsState.value !is UiState.Success) {
                    _newsState.value = UiState.Error("No news available")
                }
                delay(1800000) // Poll news every 30 mins
            }
        }
    }
}
