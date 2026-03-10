package com.trackfi.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE merchantName = :merchantName ORDER BY date DESC")
    suspend fun getTransactionsByMerchant(merchantName: String): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE smsId = :smsId")
    suspend fun isSmsIdProcessed(smsId: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE date = :date AND amount = :amount AND merchantName = :merchantName")
    suspend fun doesTransactionExist(date: Long, amount: Double, merchantName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
