package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var stockJob: Job? = null
    private var newsJob: Job? = null
    private var cryptoHealthJob: Job? = null

    init {
        viewModelScope.launch {
            repository.cryptoState.collect { items ->
                _cryptoState.value = UiState.Success(items)
            }
        }
        viewModelScope.launch {
            repository.stockState.collect { items ->
                _stockState.value = UiState.Success(items)
            }
        }
    }

    fun startUpdates() {
        startStockUpdates()
        startNewsUpdates()
        startCryptoHealthUpdates()
    }

    fun retryStocks() {
        startStockUpdates()
    }

    fun retryNews() {
        startNewsUpdates()
    }

    private fun startStockUpdates() {
        stockJob?.cancel()
        stockJob = viewModelScope.launch {
            while (true) {
                val stocks = repository.fetchStocks()
                _stockState.value = UiState.Success(stocks)
                delay(10_000) // Poll stocks every 10 seconds
            }
        }
    }

    private fun startCryptoHealthUpdates() {
        cryptoHealthJob?.cancel()
        cryptoHealthJob = viewModelScope.launch {
            while (true) {
                repository.fetchCryptoSnapshot()
                delay(10_000)
            }
        }
    }

    private fun startNewsUpdates() {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            while (true) {
                val news = repository.fetchNews()
                _newsState.value = UiState.Success(news)
                delay(1_800_000) // Poll news every 30 mins
            }
        }
    }
}
