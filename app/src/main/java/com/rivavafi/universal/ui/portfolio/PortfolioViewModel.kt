package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.network.YahooStock
import com.rivavafi.universal.data.repository.YahooStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: YahooStockRepository
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<YahooStock>>(emptyList())
    val stocks: StateFlow<List<YahooStock>> = _stocks

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    init {
        startPolling()
    }

    fun startPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            // Do an immediate fetch so the screen does not remain in loading state on first open.
            fetchData()

            while (isActive) {
                delay(20 * 60 * 1000L) // Poll every 20 minutes
                fetchData()
            }
        }
    }

    private suspend fun fetchData() {
        try {
            val result = repository.getStocks()
            if (result.isNotEmpty()) {
                _stocks.value = result
                _isError.value = false
            } else {
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
