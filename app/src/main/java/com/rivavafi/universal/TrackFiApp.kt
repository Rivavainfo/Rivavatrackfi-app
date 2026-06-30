package com.rivavafi.universal

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RivavaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        validateFinnhubApiKey()
    }

    private fun validateFinnhubApiKey() {
        val apiKey = BuildConfig.FINNHUB_API_KEY
        if (apiKey.isBlank() || apiKey == "d7r4hahr01qtpsm11kc0d7r4hahr01qtpsm11kcg") {
            Log.w("RivavaApp", "Finnhub API key is blank or using placeholder. Live data may not work.")
        }
    }
}
