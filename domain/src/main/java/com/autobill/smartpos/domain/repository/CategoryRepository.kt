package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for categories.
 *
 * ## Role access
 * - [getCategories] / [getCategoryById] / [getFoodsByCategory] — any authenticated role.
 * - [createCategory] / [updateCategory] / [deleteCategory] — **admin / super_admin only**.
 *   The server returns 403 Forbidden for lower roles. These methods are provided for
 *   completeness; the POS UI shows them only when `canManageMenu` is true.
 *
 * ## Caching
 * The last successfully fetched category list is held in an in-memory cache and exposed
 * via [observeCategories]. It is cleared when the app process terminates (session-scoped).
 */
interface CategoryRepository {

    /** Fetch all categories for [restaurantId] from the network and refresh the cache. */
    suspend fun getCategories(restaurantId: Long): Result<List<Category>>

    /**
     * Observe the in-memory category cache as a [Flow].
     * Emits null until the first successful [getCategories] call in this session.
     */
    fun observeCategories(): Flow<List<Category>?>

    /** Fetch a single category by [id]. */
    suspend fun getCategoryById(id: Long): Result<Category>

    /**
     * Paginated list of foods belonging to [categoryId].
     *
     * ⚠️ This endpoint uses `offset` + `limit` pagination (not `page` + `limit`).
     *    Use `GET /foods?restaurantId=&categoryId=` for the food menu screen instead
     *    (consistent pagination shape). Use this only when a category-scoped food
     *    list is explicitly needed.
     */
    suspend fun getFoodsByCategory(
        categoryId: Long,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<List<Food>>

    /**
     * Create a new category. 🔴 Admin / super_admin only.
     * @param restaurantId The outlet this category belongs to.
     * @param name Required — 2–100 characters.
     * @param description Optional free-text description.
     * @param imageUrl Optional CDN URL for the category image.
     * @param displayOrder Sort order (default 0).
     */
    suspend fun createCategory(
        restaurantId: Long,
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        displayOrder: Int = 0,
    ): Result<Category>

    /**
     * Update an existing category. 🔴 Admin / super_admin only.
     * All fields are replaced — pass the current value if unchanged.
     */
    suspend fun updateCategory(
        id: Long,
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        displayOrder: Int = 0,
    ): Result<Category>

    /**
     * Delete a category by [id]. 🔴 Admin / super_admin only.
     * Returns [Result.Success] with the server confirmation message on success.
     */
    suspend fun deleteCategory(id: Long): Result<String>
}

