package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.network.YahooStock
import com.rivavafi.universal.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repo: MarketRepository
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<YahooStock>>(emptyList())
    val stocks: StateFlow<List<YahooStock>> = _stocks

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                val data = repo.getStocks()
                _stocks.value = data
                _isLoading.value = false
                delay(10000) // Refresh stocks every 10 seconds
            }
        }
    }
}
