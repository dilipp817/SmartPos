package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Fetch all categories for the current restaurant and refresh the in-memory cache.
 *
 * Inject this in any ViewModel that needs the full category list — e.g. the food
 * filter chip bar or a category picker in food CRUD.
 *
 * Usage:
 * ```kotlin
 * val restaurantId = getRestaurantIdUseCase() ?: return
 * getCategoriesUseCase(restaurantId)  // result also cached via ObserveCategoriesUseCase
 * ```
 */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<List<Category>> =
        repository.getCategories(restaurantId)
}

/**
 * Observe the in-memory category cache.
 * Emits null before the first successful [GetCategoriesUseCase] call this session.
 *
 * Typical use:
 * ```kotlin
 * observeCategoriesUseCase()
 *     .filterNotNull()
 *     .onEach { categories -> _uiState.update { it.copy(categories = categories) } }
 *     .launchIn(viewModelScope)
 * ```
 */
class ObserveCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>?> = repository.observeCategories()
}

/** Fetch a single [Category] by its [id]. */
class GetCategoryByIdUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: Long): Result<Category> =
        repository.getCategoryById(id)
}

/**
 * Get paginated foods belonging to a specific category.
 * Use `GetFoodsPaginatedUseCase` with a `categoryId` filter for the main menu screen instead —
 * this use case is for category-scoped food lists only (e.g. a category detail page).
 */
class GetFoodsByCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(
        categoryId: Long,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<List<Food>> = repository.getFoodsByCategory(categoryId, offset, limit)
}

// ── Admin-only mutations ────────────────────────────────────────────────────
// Server returns 403 Forbidden for staff / manager roles.
// Gate these behind canManageMenu from ObserveRolePermissionsUseCase.

/**
 * Create a new category. 🔴 Admin / super_admin only.
 * [restaurantId] MUST come from [GetRestaurantIdUseCase] — never hardcoded.
 */
class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        displayOrder: Int = 0,
    ): Result<Category> = repository.createCategory(
        restaurantId  = restaurantId,
        name          = name,
        description   = description,
        imageUrl      = imageUrl,
        displayOrder  = displayOrder,
    )
}

/**
 * Update an existing category. 🔴 Admin / super_admin only.
 * All fields are replaced — pass the current values for fields you are not changing.
 */
class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(
        id: Long,
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        displayOrder: Int = 0,
    ): Result<Category> = repository.updateCategory(
        id           = id,
        name         = name,
        description  = description,
        imageUrl     = imageUrl,
        displayOrder = displayOrder,
    )
}

/** Delete a category by [id]. 🔴 Admin / super_admin only. */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: Long): Result<String> =
        repository.deleteCategory(id)
}

