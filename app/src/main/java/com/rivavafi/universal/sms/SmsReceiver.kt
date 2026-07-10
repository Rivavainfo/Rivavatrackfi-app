package com.rivavafi.universal.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        for (sms in messages) {
            val sender = sms.originatingAddress ?: ""
            val body = sms.messageBody ?: ""
            val timestamp = sms.timestampMillis
            val smsId = "inc_${timestamp}"

            val data = Data.Builder()
                .putString("sender", sender)
                .putString("body", body)
                .putLong("timestamp", timestamp)
                .putString("smsId", smsId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SmsWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
