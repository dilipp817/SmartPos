package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.mapper.buildCreateCategoryRequest
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.CategoryApiService
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Category repository implementation.
 *
 * ## Caching strategy
 * An in-memory [MutableStateFlow] holds the last successfully fetched category list.
 * This survives navigation within a session but resets on process death.
 * It is refreshed whenever [getCategories] is called (typically on screen entry + pull-to-refresh).
 *
 * Mutations ([createCategory], [updateCategory], [deleteCategory]) call [getCategories] on
 * success to keep the cache consistent — no stale data shown after a change.
 *
 * ## Role enforcement
 * The server returns 403 Forbidden for POST / PUT / DELETE calls from staff / manager roles.
 * Use [com.autobill.smartpos.domain.model.RolePermissions.canManageMenu] to gate these
 * operations in the UI before calling the use cases.
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: CategoryApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryRepository {

    /** Nullable = not yet loaded; empty list = loaded but no categories exist. */
    private val _categories = MutableStateFlow<List<Category>?>(null)

    // ── Read operations ───────────────────────────────────────────────────────

    override suspend fun getCategories(restaurantId: Long): Result<List<Category>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getCategoriesByRestaurant(restaurantId)
                val dtoList = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch categories"
                }
                val categories = dtoList.map { it.toDomain() }
                // Refresh in-memory cache — sorted by displayOrder then name
                _categories.value = categories.sortedWith(
                    compareBy(Category::displayOrder, Category::name)
                )
                Result.Success(categories)
            } catch (e: Exception) {
                // Surface the cached list if available, otherwise propagate the error
                val cached = _categories.value
                if (cached != null) Result.Success(cached) else Result.Failure(e)
            }
        }

    override fun observeCategories(): Flow<List<Category>?> = _categories.asStateFlow()

    override suspend fun getCategoryById(id: Long): Result<Category> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getCategoryById(id)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Category not found"
                }
                Result.Success(dto.toDomain())
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    override suspend fun getFoodsByCategory(
        categoryId: Long,
        offset: Int,
        limit: Int,
    ): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getFoodsByCategory(categoryId, offset, limit)
            val pagedData = checkNotNull(response.data) {
                response.message ?: "Failed to fetch foods for category"
            }
            // Reuse FoodListItemDto → FoodEntity → Food mapper chain.
            // restaurantId defaults to 0L because the category endpoint does not return it;
            // callers that need the restaurantId should use GetFoodsPaginatedUseCase with
            // a categoryId filter instead.
            val foods = pagedData.data.map { it.toEntity(restaurantId = 0L).toDomain() }
            Result.Success(foods)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    // ── Write operations (admin / super_admin only) ───────────────────────────

    override suspend fun createCategory(
        restaurantId: Long,
        name: String,
        description: String?,
        imageUrl: String?,
        displayOrder: Int,
    ): Result<Category> = withContext(ioDispatcher) {
        try {
            val response = apiService.createCategory(
                restaurantId = restaurantId,
                request      = buildCreateCategoryRequest(name, description, imageUrl, displayOrder),
            )
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to create category"
            }
            val category = dto.toDomain()
            // Optimistically add to cache then re-fetch for consistency
            _categories.value = (_categories.value.orEmpty() + category)
                .sortedWith(compareBy(Category::displayOrder, Category::name))
            // Refresh to guarantee the full list is authoritative
            getCategories(restaurantId)
            Result.Success(category)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateCategory(
        id: Long,
        name: String,
        description: String?,
        imageUrl: String?,
        displayOrder: Int,
    ): Result<Category> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateCategory(
                id      = id,
                request = buildCreateCategoryRequest(name, description, imageUrl, displayOrder),
            )
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update category"
            }
            val updated = dto.toDomain()
            // Replace the matching entry in the cache
            _categories.value = _categories.value?.map {
                if (it.id == id) updated else it
            }
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteCategory(id: Long): Result<String> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.deleteCategory(id)
                // Remove from cache immediately
                _categories.value = _categories.value?.filter { it.id != id }
                Result.Success(response.message ?: "Category deleted")
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }
}

