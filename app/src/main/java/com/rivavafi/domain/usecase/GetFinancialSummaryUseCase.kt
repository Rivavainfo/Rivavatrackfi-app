package com.rivavafi.domain.usecase

import com.rivavafi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class FinancialSummaryState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0
)

class GetFinancialSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<FinancialSummaryState> {
        return repository.getAllTransactions().map { transactions ->
            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            FinancialSummaryState(
                totalIncome = income,
                totalExpense = expense,
                netSavings = income - expense
            )
        }
    }
}
