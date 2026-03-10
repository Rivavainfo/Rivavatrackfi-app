package com.trackfi.domain.repository

import com.trackfi.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun addCategory(category: CategoryEntity)
    suspend fun initializeDefaultCategories()
}
