package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.api.Article
import com.rivavafi.universal.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {

    private val _newsData = MutableStateFlow<List<Article>>(emptyList())
    val newsData: StateFlow<List<Article>> = _newsData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    fun startNewsRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                loadNews()
                delay(1800000) // 30 minutes
            }
        }
    }

    private suspend fun loadNews() {
        _isLoading.value = true
        val data = repo.getNews()
        _newsData.value = data
        _isLoading.value = false
    }
}
