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
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Stock(val symbol: String, val price: Double, val change: Double)


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

    private val _liveStocks = MutableStateFlow<List<Stock>>(listOf(
        Stock("RTX", 118.00, 0.0),
        Stock("IREDA", 248.50, 0.0)
    ))
    val liveStocks: StateFlow<List<Stock>> = _liveStocks

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                fetchLiveStocks()
                delay(20 * 60 * 1000L) // Poll every 20 minutes
            }
        }
    }

    private suspend fun fetchLiveStocks() {
        withContext(Dispatchers.IO) {
            try {
                var rtxPrice = 118.00
                var rtxChange = 0.0
                try {
                    val rtxDoc = Jsoup.connect("https://in.investing.com/equities/raytheon-co").userAgent("Mozilla/5.0").get()
                    val priceStr = rtxDoc.select("div[data-test=instrument-price-last]").text().replace(",", "")
                    val changeStr = rtxDoc.select("span[data-test=instrument-price-change-percent]").text().replace("%", "").replace("(", "").replace(")", "").replace("+", "")
                    if (priceStr.isNotEmpty()) rtxPrice = priceStr.toDouble()
                    if (changeStr.isNotEmpty()) rtxChange = changeStr.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                var iredaPrice = 248.50
                var iredaChange = 0.0
                try {
                    val iredaDoc = Jsoup.connect("https://share.google/ZAqbc8qHxTbmaznOr").userAgent("Mozilla/5.0").followRedirects(true).get()
                    val p = iredaDoc.select("div.YMlKec.fxKbKc").first()?.text()?.replace("₹", "")?.replace(",", "")
                    val c = iredaDoc.select("div.JwB6zf").first()?.text()?.replace("%", "")?.replace("+", "")?.trim()
                    if (p != null) iredaPrice = p.toDouble()
                    if (c != null) iredaChange = c.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _liveStocks.value = listOf(
                    Stock("RTX", rtxPrice, rtxChange),
                    Stock("IREDA", iredaPrice, iredaChange)
                )
            } catch (e: Exception) {
                e.printStackTrace()
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
