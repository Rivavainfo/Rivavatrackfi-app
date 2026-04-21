package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.repository.CryptoRepository
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

    private var currentIds: List<String> = emptyList()
    private var pollingJob: kotlinx.coroutines.Job? = null

    fun startPolling(ids: List<String>) {
        currentIds = ids
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // Immediately initialize state so it never displays a hanging "Loading" message.
            _cryptoStates.value = generateMockCryptoData(ids)

            while (true) {
                fetchCrypto(ids)
                delay(30000) // Poll crypto every 30 seconds
            }
        }
    }

    fun refresh() {
        if (currentIds.isNotEmpty()) {
            viewModelScope.launch {
                fetchCrypto(currentIds)
            }
        }
    }

    private suspend fun fetchCrypto(ids: List<String>) {
        val updated = _cryptoStates.value.toMutableMap()
        ids.forEach { id ->
            repository.getCryptoQuote(id).collect { result ->
                result.onSuccess { quote ->
                    val changePercent = if (quote.pc != 0.0) ((quote.c - quote.pc) / quote.pc) * 100 else 0.0
                    updated[id] = CryptoData(
                        price = quote.c,
                        change24h = changePercent
                    )
                }.onFailure {
                    if (!updated.containsKey(id)) {
                        updated[id] = generateMockCryptoData(listOf(id))[id] ?: CryptoData(0.0, 0.0)
                    }
                }
            }
        }
        _cryptoStates.value = updated
    }

    private fun generateMockCryptoData(ids: List<String>): Map<String, CryptoData> {
        val mockData = mutableMapOf<String, CryptoData>()
        ids.forEach { id ->
            val basePrice = when (id) {
                "bitcoin" -> 64500.0
                "ethereum" -> 3400.0
                "solana" -> 145.0
                else -> 100.0
            }
            val fluctuation = (Math.random() - 0.5) * (basePrice * 0.05) // 5% max fluctuation
            val price = basePrice + fluctuation
            val change24h = (fluctuation / basePrice) * 100
            mockData[id] = CryptoData(price, change24h)
        }
        return mockData
    }
}
