package com.rivavafi.domain.usecase

import com.rivavafi.data.local.TransactionEntity
import com.rivavafi.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) {
        repository.addTransaction(transaction)
    }
}
