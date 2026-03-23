package com.trackfi.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackfi.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CryptoData(
    val price: Double,
    val change24h: Double
)

@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _cryptoStates = MutableStateFlow<Map<String, CryptoData>>(emptyMap())
    val cryptoStates: StateFlow<Map<String, CryptoData>> = _cryptoStates

    fun startPolling(ids: List<String>) {
        viewModelScope.launch {
            val idsString = ids.joinToString(",")
            while (true) {
                repository.getSimplePrices(idsString).collect { result ->
                    result.onSuccess { data ->
                        val newState = mutableMapOf<String, CryptoData>()
                        data.forEach { (id, metrics) ->
                            val price = metrics["usd"] ?: 0.0
                            val change = metrics["usd_24h_change"] ?: 0.0
                            newState[id] = CryptoData(price, change)
                        }
                        _cryptoStates.value = newState
                    }
                }
                delay(30000) // Poll crypto every 30 seconds
            }
        }
    }
}
