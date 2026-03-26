package com.trackfi.domain.repository

import com.trackfi.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionById(id: Long): Flow<TransactionEntity?>
    suspend fun getTransactionsByMerchant(merchantName: String): List<TransactionEntity>
    suspend fun isSmsIdProcessed(smsId: String): Boolean
    suspend fun doesTransactionExist(date: Long, amount: Double, merchantName: String): Boolean
    suspend fun getAllTransactionsSync(): List<TransactionEntity>
    suspend fun addTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun deleteAllTransactions()
}
