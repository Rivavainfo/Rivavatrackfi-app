package com.rivavafi.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.data.network.YahooFinanceApi
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
                fetchIredaData()
                delay(15000) // Poll every 15 seconds
            }
        }
    }

    private suspend fun fetchIredaData() {
        try {
            val response = yahooFinanceApi.getIredaStockData()
            val meta = response.chart?.result?.firstOrNull()?.meta

            if (meta?.regularMarketPrice != null && meta.previousClose != null) {
                _iredaPrice.value = meta.regularMarketPrice
                _iredaPreviousClose.value = meta.previousClose
                _isError.value = false
            } else {
                // Keep previous data but maybe log warning
                _isError.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isError.value = true
        } finally {
            _isLoading.value = false
        }
    }
}
