package com.rivavafi.domain.usecase

import com.rivavafi.data.local.CategoryEntity
import com.rivavafi.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: CategoryEntity) {
        repository.addCategory(category)
    }
}
