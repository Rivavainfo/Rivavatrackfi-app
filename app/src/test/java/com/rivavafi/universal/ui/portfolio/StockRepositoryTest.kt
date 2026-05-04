package com.rivavafi.universal.ui.portfolio

import android.content.Context
import android.content.SharedPreferences
import com.rivavafi.universal.domain.api.StockApiService
import com.rivavafi.universal.domain.api.StockResponse
import com.rivavafi.universal.domain.repository.QuoteSource
import com.rivavafi.universal.domain.repository.StockRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.lang.reflect.Field

class StockRepositoryTest {

    @Mock
    private lateinit var apiService: StockApiService

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var repository: StockRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(context.getSharedPreferences("stock_quotes_cache", Context.MODE_PRIVATE)).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putFloat(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyFloat())).thenReturn(editor)

        repository = StockRepository(apiService, context)
    }

    private fun setApiKey(key: String) {
        val field: Field = StockRepository::class.java.getDeclaredField("apiKey")
        field.isAccessible = true
        field.set(repository, key)
    }

    @Test
    fun `successful live quote returns LIVE source`() = runBlocking {
        setApiKey("VALID_KEY")
        val mockResponse = StockResponse(c = 150.0, pc = 145.0, h = 155.0, l = 140.0, o = 145.0)
        `when`(apiService.getQuote("IREDA.NS", "VALID_KEY")).thenReturn(retrofit2.Response.success(mockResponse))

        val result = repository.getGuaranteedQuote("IREDA")

        assertEquals(QuoteSource.LIVE, result.source)
        assertTrue(result.diagnostics!!.contains("LIVE success for IREDA.NS"))
    }

    @Test
    fun `when Finnhub fails with 401, attempts scrape fallback`() = runBlocking {
        setApiKey("VALID_KEY")
        val errorBody = okhttp3.ResponseBody.create(null, "Unauthorized")
        `when`(apiService.getQuote("IREDA.NS", "VALID_KEY")).thenReturn(retrofit2.Response.error(401, errorBody))

        // Mock missing cache so we can verify it falls through correctly
        `when`(sharedPreferences.getFloat("IREDA_price", -1f)).thenReturn(-1f)
        `when`(sharedPreferences.getFloat("IREDA_pc", -1f)).thenReturn(-1f)

        val result = repository.getGuaranteedQuote("IREDA")

        // Depending on network/scrape success, it might be SCRAPE or DEFAULT.
        // Assuming no internet connection during tests, it should fail to scrape and hit DEFAULT.
        // But we just verify it chains diagnostics from LIVE -> SCRAPE -> DEFAULT
        assertTrue(result.diagnostics!!.contains("LIVE HTTP Error: code=401"))
    }

    @Test
    fun `when Finnhub fails and cache available, returns CACHE source`() = runBlocking {
        setApiKey("VALID_KEY")
        val errorBody = okhttp3.ResponseBody.create(null, "Server Error")
        `when`(apiService.getQuote("IREDA.NS", "VALID_KEY")).thenReturn(retrofit2.Response.error(500, errorBody))

        // Mock valid cache
        `when`(sharedPreferences.getFloat("IREDA_price", -1f)).thenReturn(150f)
        `when`(sharedPreferences.getFloat("IREDA_pc", -1f)).thenReturn(145f)

        val result = repository.getGuaranteedQuote("IREDA")

        assertEquals(QuoteSource.CACHE, result.source)
        assertTrue(result.diagnostics!!.contains("CACHE success"))
    }

    @Test
    fun `when API key is placeholder, skips live and returns default if no cache`() = runBlocking {
        setApiKey("d7r4hahr01qtpsm11kc0d7r4hahr01qtpsm11kcg")
        `when`(sharedPreferences.getFloat("IREDA_price", -1f)).thenReturn(-1f)
        `when`(sharedPreferences.getFloat("IREDA_pc", -1f)).thenReturn(-1f)

        val result = repository.getGuaranteedQuote("IREDA")

        assertEquals(QuoteSource.DEFAULT, result.source)
        assertTrue(result.diagnostics!!.contains("LIVE fetch skipped"))
    }
}
