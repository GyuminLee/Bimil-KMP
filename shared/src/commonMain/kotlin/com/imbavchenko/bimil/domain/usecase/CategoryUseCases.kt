package com.imbavchenko.bimil.domain.usecase

import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GetAllCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.getAllCategories()
}

class GetCategoryByIdUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(id: String): Flow<Category?> = categoryRepository.getCategoryById(id)
}

class SaveCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) {
        val existing = categoryRepository.getCategoryById(category.id).first()
        if (existing != null) {
            categoryRepository.updateCategory(category)
        } else {
            categoryRepository.insertCategory(category)
        }
    }
}

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String) {
        val defaultCategory = categoryRepository.getDefaultCategory().first()
        val defaultId = defaultCategory?.id ?: "other"
        categoryRepository.deleteCategory(id, defaultId)
    }
}

class InitializeCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke() {
        val count = categoryRepository.getCategoryCount().first()
        if (count == 0L) {
            categoryRepository.initializeDefaultCategories()
        }
    }
}
