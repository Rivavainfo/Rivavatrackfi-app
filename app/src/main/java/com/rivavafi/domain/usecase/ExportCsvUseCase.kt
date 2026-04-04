package com.rivavafi.domain.usecase

import android.content.Context
import android.os.Environment
import com.rivavafi.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportCsvUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(@Suppress("UNUSED_PARAMETER") context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val transactions = repository.getAllTransactionsSync()
            if (transactions.isEmpty()) {
                return@withContext Result.failure(Exception("No transactions to export."))
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val trackFiDir = File(downloadsDir, "TrackFi")
            if (!trackFiDir.exists()) {
                trackFiDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(trackFiDir, "TrackFi_Backup_$timestamp.csv")

            file.bufferedWriter().use { out ->
                // Write header
                out.write("date,merchant,amount,type,category,subcategory,bank,message\n")

                val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

                for (t in transactions) {
                    val dateStr = dateFormatter.format(Date(t.date))
                    val merchant = t.merchantName.replace("\"", "\"\"")
                    val amount = t.amount.toString()
                    val type = t.type
                    val category = t.category.replace("\"", "\"\"")
                    val subcategory = (t.subcategory ?: "").replace("\"", "\"\"")
                    val bank = (t.bankName ?: "").replace("\"", "\"\"")
                    val message = (t.rawMessage ?: "").replace("\"", "\"\"").replace("\n", " ")

                    out.write("\"$dateStr\",\"$merchant\",\"$amount\",\"$type\",\"$category\",\"$subcategory\",\"$bank\",\"$message\"\n")
                }
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
