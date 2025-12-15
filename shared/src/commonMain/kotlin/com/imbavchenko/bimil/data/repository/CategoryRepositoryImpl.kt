package com.imbavchenko.bimil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.imbavchenko.bimil.db.BimilDatabase
import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CategoryRepositoryImpl(
    private val database: BimilDatabase
) : CategoryRepository {

    private val queries = database.categoryQueries
    private val accountQueries = database.accountEntryQueries

    override fun getAllCategories(): Flow<List<Category>> {
        return queries.getAllCategories()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getCategoryById(id: String): Flow<Category?> {
        return queries.getCategoryById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainModel() }
    }

    override fun getDefaultCategory(): Flow<Category?> {
        return queries.getDefaultCategory()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainModel() }
    }

    override fun getCategoryCount(): Flow<Long> {
        return queries.getCategoryCount()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0L }
    }

    override suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        queries.insertCategory(
            id = category.id,
            name = category.name,
            color = category.color,
            icon = category.icon,
            is_default = if (category.isDefault) 1L else 0L,
            sort_order = category.sortOrder.toLong(),
            created_at = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        queries.updateCategory(
            name = category.name,
            color = category.color,
            icon = category.icon,
            sort_order = category.sortOrder.toLong(),
            id = category.id
        )
    }

    override suspend fun deleteCategory(id: String, defaultCategoryId: String) = withContext(Dispatchers.IO) {
        // First, move all accounts in this category to the default category
        accountQueries.updateAccountCategoryToDefault(defaultCategoryId, id)
        // Then delete the category
        queries.deleteCategory(id)
    }

    override suspend fun deleteAllNonDefaultCategories() = withContext(Dispatchers.IO) {
        queries.deleteAllNonDefaultCategories()
    }

    override suspend fun initializeDefaultCategories() = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        Category.DEFAULT_CATEGORIES.forEach { category ->
            queries.insertCategory(
                id = category.id,
                name = category.name,
                color = category.color,
                icon = category.icon,
                is_default = if (category.isDefault) 1L else 0L,
                sort_order = category.sortOrder.toLong(),
                created_at = now
            )
        }
    }

    private fun com.imbavchenko.bimil.db.Category.toDomainModel() = Category(
        id = id,
        name = name,
        color = color,
        icon = icon,
        isDefault = is_default == 1L,
        sortOrder = sort_order.toInt(),
        createdAt = created_at
    )
}
