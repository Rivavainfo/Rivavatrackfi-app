package com.trackfi.domain.usecase

import com.trackfi.data.local.CategoryEntity
import com.trackfi.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: CategoryEntity) {
        repository.addCategory(category)
    }
}
