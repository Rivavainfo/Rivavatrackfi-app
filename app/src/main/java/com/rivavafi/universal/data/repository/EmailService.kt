package com.rivavafi.universal.data.repository

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.rivavafi.universal.BuildConfig

@Singleton
class EmailService @Inject constructor() {

    suspend fun sendWelcomeEmail(userEmail: String, userName: String) {
        withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.SENDGRID_API_KEY
                if (apiKey.isNullOrEmpty()) {
                    Log.e("EmailService", "SENDGRID_API_KEY is not set.")
                    return@withContext
                }

                val from = Email("hello@rivavafi.com")
                val subject = "Welcome to Universal!"
                val to = Email(userEmail)
                val content = Content("text/plain", "Welcome $userName!")
                val mail = Mail(from, subject, to, content)

                val sg = SendGrid(apiKey)
                val request = Request()

                request.method = Method.POST
                request.endpoint = "mail/send"
                request.body = mail.build()

                val response = sg.api(request)
                Log.d("EmailService", "SendGrid Response: ${response.statusCode} ${response.body}")
            } catch (ex: Exception) {
                Log.e("EmailService", "Failed to send welcome email", ex)
            }
        }
    }
}
