package com.imbavchenko.bimil.domain.repository

import com.imbavchenko.bimil.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: String): Flow<Category?>
    fun getDefaultCategory(): Flow<Category?>
    fun getCategoryCount(): Flow<Long>

    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String, defaultCategoryId: String)
    suspend fun initializeDefaultCategories()
}
