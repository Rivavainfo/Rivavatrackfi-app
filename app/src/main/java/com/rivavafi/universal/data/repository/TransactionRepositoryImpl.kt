package com.rivavafi.universal.data.repository

import com.rivavafi.universal.data.local.TransactionDao
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return dao.getAllTransactions()
    }

    override fun getTransactionById(id: Long): Flow<TransactionEntity?> {
        return dao.getTransactionById(id)
    }

    override suspend fun getTransactionsByMerchant(merchantName: String): List<TransactionEntity> {
        return dao.getTransactionsByMerchant(merchantName)
    }

    override suspend fun isSmsIdProcessed(smsId: String): Boolean {
        return dao.isSmsIdProcessed(smsId) > 0
    }

    override suspend fun doesTransactionExist(date: Long, amount: Double, merchantName: String): Boolean {
        return dao.doesTransactionExist(date, amount, merchantName) > 0
    }

    override suspend fun getAllTransactionsSync(): List<TransactionEntity> {
        return dao.getAllTransactionsSync()
    }

    override suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    override suspend fun deleteAllTransactions() {
        dao.deleteAllTransactions()
    }
}
