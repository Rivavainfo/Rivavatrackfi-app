package com.rivavafi.domain.repository

import com.rivavafi.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun addCategory(category: CategoryEntity)
    suspend fun initializeDefaultCategories()
}
