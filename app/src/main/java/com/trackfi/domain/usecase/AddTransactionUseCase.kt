package com.trackfi.domain.usecase

import com.trackfi.data.local.TransactionEntity
import com.trackfi.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) {
        repository.addTransaction(transaction)
    }
}
