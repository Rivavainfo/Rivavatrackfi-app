package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.network.YahooFinanceApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val yahooFinanceApi: YahooFinanceApi
) : ViewModel() {

    private val _iredaPrice = MutableStateFlow(0.0)
    val iredaPrice: StateFlow<Double> = _iredaPrice

    private val _iredaPreviousClose = MutableStateFlow(0.0)
    val iredaPreviousClose: StateFlow<Double> = _iredaPreviousClose

    private val _rtxPrice = MutableStateFlow(0.0)
    val rtxPrice: StateFlow<Double> = _rtxPrice

    private val _rtxPreviousClose = MutableStateFlow(0.0)
    val rtxPreviousClose: StateFlow<Double> = _rtxPreviousClose

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                fetchData()
                delay(900000) // 15 minutes
            }
        }
    }

    private suspend fun fetchData() {
        _isLoading.value = true
        var success = false
        try {
            val response = yahooFinanceApi.getFallbackQuotes("IREDA.NS,RTX")
            val items = response.quoteResponse?.result ?: emptyList()

            items.forEach { item ->
                val price = item.regularMarketPrice ?: 0.0
                val changePercent = item.regularMarketChangePercent ?: 0.0

                // Reverse engineer previous close: prevClose = price / (1 + changePercent/100)
                val prevClose = if (changePercent != -100.0) price / (1 + changePercent / 100) else 0.0

                if (item.symbol?.contains("IREDA") == true) {
                    _iredaPrice.value = price
                    _iredaPreviousClose.value = prevClose
                } else if (item.symbol?.contains("RTX") == true) {
                    _rtxPrice.value = price
                    _rtxPreviousClose.value = prevClose
                }
            }
            if (items.isNotEmpty()) {
                success = true
                _isError.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!success) {
                _isError.value = true
            }
            _isLoading.value = false
        }
    }
}
