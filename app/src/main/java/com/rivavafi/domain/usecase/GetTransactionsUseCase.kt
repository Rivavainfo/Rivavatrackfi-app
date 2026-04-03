package com.rivavafi.domain.usecase

import com.rivavafi.data.local.TransactionEntity
import com.rivavafi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<TransactionEntity>> {
        return repository.getAllTransactions()
    }
}
