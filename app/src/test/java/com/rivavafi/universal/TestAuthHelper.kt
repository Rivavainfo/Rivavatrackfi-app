package com.rivavafi.universal

import kotlinx.coroutines.runBlocking

fun main() {
    println("Testing AuthHelper saving to Google Sheets...")
    runBlocking {
        try {
            AuthHelper.saveUserToFirestore(
                uid = "test-uid-1234",
                name = "Test User Sandbox",
                email = "test.sandbox@example.com",
                phone = "+15551234567",
                provider = "test_run"
            )
            println("Test run completed. Check logcat/Google Sheets if connected.")
        } catch (e: Exception) {
            println("Test run failed: ${e.message}")
            e.printStackTrace()
        }
    }
}
