package com.autobill.smartpos.domain.model

/**
 * Domain model for a food category.
 *
 * Categories are restaurant-scoped — every category belongs to one restaurant.
 * The [restaurantId] is not stored here because the app always works within a
 * single outlet context (sourced once from [GetRestaurantIdUseCase]).
 *
 * [foodCount] is server-computed and reflects how many active food items belong
 * to this category. Use it for display only — never derive business logic from it.
 */
data class Category(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    /** Sort order for display — lower numbers appear first. */
    val displayOrder: Int,
    val isActive: Boolean,
    /** Server-computed count of food items in this category. */
    val foodCount: Int,
)

