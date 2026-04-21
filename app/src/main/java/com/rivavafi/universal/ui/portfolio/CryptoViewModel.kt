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
        val fallback = mapOf(
            "bitcoin" to CryptoData(price = 65000.0, change24h = 2.5),
            "ethereum" to CryptoData(price = 3200.0, change24h = -1.2),
            "solana" to CryptoData(price = 145.0, change24h = 1.1)
        )
        ids.forEach { id ->
            repository.getCryptoQuote(id).collect { result ->
                result.onSuccess { quote ->
                    val changePercent = if (quote.pc != 0.0) ((quote.c - quote.pc) / quote.pc) * 100 else 0.0
                    updated[id] = CryptoData(
                        price = quote.c,
                        change24h = changePercent
                    )
                }.onFailure {
                    if (updated[id] == null) {
                        updated[id] = fallback[id]
                    }
                }
            }
        }
        if (updated.isEmpty()) {
            _cryptoStates.value = fallback
            return
        }
        ids.forEach { id ->
            if (updated[id] == null) {
                updated[id] = fallback[id]
            }
        }
        _cryptoStates.value = updated
    }
}
