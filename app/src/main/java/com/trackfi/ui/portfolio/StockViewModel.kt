package com.trackfi.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackfi.domain.api.FinnhubQuoteResponse
import com.trackfi.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockState(
    val isLoading: Boolean = false,
    val data: FinnhubQuoteResponse? = null,
    val error: String? = null
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _stockStates = MutableStateFlow<Map<String, StockState>>(emptyMap())
    val stockStates: StateFlow<Map<String, StockState>> = _stockStates

    fun startPolling(symbols: List<String>) {
        viewModelScope.launch {
            while (true) {
                symbols.forEach { symbol ->
                    _stockStates.value = _stockStates.value.toMutableMap().apply {
                        put(symbol, get(symbol)?.copy(isLoading = true) ?: StockState(isLoading = true))
                    }

                    repository.getRealtimeQuote(symbol).collect { result ->
                        result.onSuccess { quote ->
                            // Basic sanity check on Finnhub API rate limiting/empty responses (c == 0 usually means invalid symbol/key)
                            if (quote.c != 0.0) {
                                _stockStates.value = _stockStates.value.toMutableMap().apply {
                                    put(symbol, StockState(isLoading = false, data = quote, error = null))
                                }
                            } else {
                                // Fallback mock data if API limits hit or missing data
                                val mockData = generateMockData(symbol)
                                _stockStates.value = _stockStates.value.toMutableMap().apply {
                                    put(symbol, StockState(isLoading = false, data = mockData, error = "Using cached/mock data (API Rate Limit)"))
                                }
                            }
                        }.onFailure {
                            val mockData = generateMockData(symbol)
                            _stockStates.value = _stockStates.value.toMutableMap().apply {
                                put(symbol, StockState(isLoading = false, data = mockData, error = "Using cached/mock data (Network Error)"))
                            }
                        }
                    }
                }
                delay(15000) // Poll every 15 seconds
            }
        }
    }

    private fun generateMockData(symbol: String): FinnhubQuoteResponse {
        val basePrice = if (symbol == "RTX") 205.00 else if (symbol == "WMT") 125.12 else if (symbol == "HAL") 3995.0 else 150.0
        val fluctuation = (Math.random() - 0.5) * 2.0
        return FinnhubQuoteResponse(
            c = basePrice + fluctuation,
            d = fluctuation,
            dp = (fluctuation / basePrice) * 100,
            h = basePrice + 5.0,
            l = basePrice - 5.0,
            o = basePrice,
            pc = basePrice - fluctuation
        )
    }
}
