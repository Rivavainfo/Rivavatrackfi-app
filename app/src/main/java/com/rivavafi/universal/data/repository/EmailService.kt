package com.rivavafi.universal.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class EmailService @Inject constructor() {

    private val client = OkHttpClient()
    private val resendApiKey = com.rivavafi.universal.BuildConfig.RESEND_API_KEY

    suspend fun sendWelcomeEmail(userEmail: String, userName: String) {
        if (userEmail.isBlank()) return
        withContext(Dispatchers.IO) {
            try {
                val greeting = if (userName.isNotBlank()) "Hello ${userName.trim()}," else "Hello,"
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #F8FAFC; padding: 20px; color: #334155;">
                        <div style="max-width: 600px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 12px; border: 1px solid #E2E8F0; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                            <div style="text-align: center; margin-bottom: 24px;">
                                <h1 style="color: #0F172A; margin: 0; font-size: 24px;">Rivava <span style="color: #38BDF8;">TrackFi</span></h1>
                                <p style="color: #64748B; font-size: 13px; margin: 4px 0 0 0;">Smart Financial Intelligence & Expense Tracking</p>
                            </div>
                            <hr style="border: none; border-top: 1px solid #E2E8F0; margin: 20px 0;" />
                            <h2 style="color: #0F172A; font-size: 20px;">Welcome to Rivava! 🎉</h2>
                            <p style="color: #475569; font-size: 15px; line-height: 1.6;">$greeting</p>
                            <p style="color: #475569; font-size: 15px; line-height: 1.6;">Thank you for creating an account with Rivava. Your account (<strong>$userEmail</strong>) is now active and ready to help you track expenses and manage your personal finances with real-time insights.</p>
                            <div style="text-align: center; margin: 28px 0;">
                                <a href="https://rivava.in" style="background-color: #2563EB; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 8px; font-weight: bold; font-size: 15px; display: inline-block;">Explore Dashboard</a>
                            </div>
                            <p style="color: #94A3B8; font-size: 12px; text-align: center; margin-top: 30px;">© 2026 Rivava Universal. All rights reserved.</p>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                val json = JSONObject().apply {
                    put("from", "Rivava TrackFi <onboarding@resend.dev>")
                    put("to", JSONArray().put(userEmail))
                    put("subject", "Welcome to Rivava TrackFi 🎉")
                    put("html", htmlContent)
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://api.resend.com/emails")
                    .addHeader("Authorization", "Bearer $resendApiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("EmailService", "Resend Direct API Response: ${response.code} $responseBody")
            } catch (e: Exception) {
                Log.e("EmailService", "Failed to send welcome email via Resend", e)
            }
        }
    }
}


