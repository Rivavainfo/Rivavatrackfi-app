package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    private val _marketItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketItems: StateFlow<List<MarketItem>> = _marketItems

    private val _newsItems = MutableStateFlow<List<News>>(emptyList())
    val newsItems: StateFlow<List<News>> = _newsItems

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var pollingJob: kotlinx.coroutines.Job? = null
    private var newsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            repository.marketState.collect { items ->
                _marketItems.value = items
                if (items.isNotEmpty()) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun startRealtimeUpdates() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                repository.fetchIredaFallback()
                delay(15000) // Poll IREDA every 15s
            }
        }
    }

    fun startNewsUpdates() {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            while (true) {
                val news = repository.fetchNews()
                _newsItems.value = news
                delay(1800000) // Poll news every 30 mins
            }
        }
    }
}
