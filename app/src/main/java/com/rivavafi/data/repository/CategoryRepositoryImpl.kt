package com.rivavafi.data.repository

import com.rivavafi.data.local.CategoryDao
import com.rivavafi.data.local.CategoryEntity
import com.rivavafi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return dao.getAllCategories()
    }

    override suspend fun addCategory(category: CategoryEntity) {
        dao.insertCategory(category)
    }

    override suspend fun initializeDefaultCategories() {
        if (dao.getCount() == 0) {
            val defaults = listOf(
                CategoryEntity(name = "Food & Dining", type = "EXPENSE"),
                CategoryEntity(name = "Shopping", type = "EXPENSE"),
                CategoryEntity(name = "Transport", type = "EXPENSE"),
                CategoryEntity(name = "Bills & Utilities", type = "EXPENSE"),
                CategoryEntity(name = "Entertainment", type = "EXPENSE"),
                CategoryEntity(name = "Health", type = "EXPENSE"),
                CategoryEntity(name = "Salary", type = "INCOME"),
                CategoryEntity(name = "Refund", type = "INCOME"),
                CategoryEntity(name = "General", type = "EXPENSE")
            )
            defaults.forEach { dao.insertCategory(it) }
        }
    }
}
