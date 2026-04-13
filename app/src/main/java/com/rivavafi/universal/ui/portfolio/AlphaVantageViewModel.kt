package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.api.Quote
import com.rivavafi.universal.domain.repository.AlphaVantageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlphaVantageViewModel @Inject constructor(
    private val repo: AlphaVantageRepository
) : ViewModel() {

    private val _stockData = MutableStateFlow<List<Quote>>(emptyList())
    val stockData: StateFlow<List<Quote>> = _stockData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    fun startAutoRefresh(symbols: List<String>) {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                loadStocks(symbols)
                delay(60000) // 60 sec
            }
        }
    }

    private suspend fun loadStocks(symbols: List<String>) {
        val result = mutableListOf<Quote>()

        for (symbol in symbols) {
            val data = repo.getStock(symbol)

            if (data != null) {
                result.add(data)
            } else {
                result.add(Quote(symbol = symbol, price = null, changePercent = null))
            }

            _stockData.value = result.toList()
            _isLoading.value = false // We have some data, stop total loading

            delay(12000) // avoid rate limit
        }
    }
}
