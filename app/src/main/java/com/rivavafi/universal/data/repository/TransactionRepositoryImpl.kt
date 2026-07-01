package com.rivavafi.universal.data.repository

import com.rivavafi.universal.data.local.TransactionDao
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> {
        return dao.getAllTransactions(userId)
    }

    override fun getTransactionById(id: Long, userId: String): Flow<TransactionEntity?> {
        return dao.getTransactionById(id, userId)
    }

    override suspend fun getTransactionsByMerchant(merchantName: String, userId: String): List<TransactionEntity> {
        return dao.getTransactionsByMerchant(merchantName, userId)
    }

    override suspend fun isSmsIdProcessed(smsId: String, userId: String): Boolean {
        return dao.isSmsIdProcessed(smsId, userId) > 0
    }

    override suspend fun doesTransactionExist(date: Long, amount: Double, merchantName: String, userId: String): Boolean {
        return dao.doesTransactionExist(date, amount, merchantName, userId) > 0
    }

    override suspend fun getAllTransactionsSync(userId: String): List<TransactionEntity> {
        return dao.getAllTransactionsSync(userId)
    }

    override suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    override suspend fun deleteAllTransactions(userId: String) {
        dao.deleteAllTransactions(userId)
    }
}
