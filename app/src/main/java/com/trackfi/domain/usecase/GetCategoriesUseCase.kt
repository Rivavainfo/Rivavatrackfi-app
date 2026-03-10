package com.trackfi.domain.usecase

import com.trackfi.data.local.CategoryEntity
import com.trackfi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryEntity>> {
        return repository.getAllCategories()
    }

    suspend fun initialize() {
        repository.initializeDefaultCategories()
    }
}
