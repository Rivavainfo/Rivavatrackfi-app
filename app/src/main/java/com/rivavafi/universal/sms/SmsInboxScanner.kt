package com.rivavafi.universal.sms

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import javax.inject.Inject

class SmsInboxScanner @Inject constructor() {
    fun scanInbox(context: Context, mode: SmsTrackingMode, userId: String) {
        if (mode == SmsTrackingMode.OFF) return

        val inputData = Data.Builder()
            .putString("userId", userId)
            .putString("mode", mode.name)
            .build()

        val request = OneTimeWorkRequestBuilder<SmsCatchupWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
