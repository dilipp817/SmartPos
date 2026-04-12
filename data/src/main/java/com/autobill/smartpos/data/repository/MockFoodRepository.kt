package com.autobill.smartpos.data.repository

import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Mock FoodRepository for development.
 * Returns realistic restaurant menu data without requiring a backend.
 * Switch to FoodRepositoryImpl in RepositoryModule when backend is ready.
 */
class MockFoodRepository @Inject constructor() : FoodRepository {

    companion object {
        val mockFoods = listOf(
            // MAIN COURSE
            Food(1, "Paneer Butter Masala", 280.0, 1, null, categoryName = "MAIN COURSE", description = "Rich creamy tomato curry with soft paneer"),
            Food(2, "Dal Makhani", 220.0, 1, null, categoryName = "MAIN COURSE", description = "Slow cooked black lentils in butter and cream"),
            Food(3, "Chicken Biryani", 320.0, 1, null, categoryName = "MAIN COURSE", description = "Fragrant basmati rice with spiced chicken"),
            Food(4, "Butter Chicken", 299.0, 1, null, categoryName = "MAIN COURSE", description = "Classic creamy tomato chicken curry"),
            Food(5, "Shahi Paneer", 270.0, 1, null, categoryName = "MAIN COURSE", description = "Paneer in rich saffron cream sauce"),
            // PIZZA
            Food(6, "Margherita Pizza", 299.0, 1, null, categoryName = "PIZZA", description = "Classic tomato base with mozzarella"),
            Food(7, "Pepperoni Pizza", 349.0, 1, null, categoryName = "PIZZA", description = "Loaded with pepperoni and cheese"),
            Food(8, "BBQ Chicken Pizza", 369.0, 1, null, categoryName = "PIZZA", description = "Tangy BBQ sauce with grilled chicken"),
            Food(9, "Veg Supreme Pizza", 329.0, 1, null, categoryName = "PIZZA", description = "Garden fresh vegetables on tomato base"),
            // STARTERS
            Food(10, "Samosa (2 pcs)", 80.0, 1, null, categoryName = "STARTERS", description = "Crispy fried pastry with spiced potato filling"),
            Food(11, "Veg Spring Rolls", 120.0, 1, null, categoryName = "STARTERS", description = "Crispy golden rolls with mixed vegetables"),
            Food(12, "Chicken 65", 199.0, 1, null, categoryName = "STARTERS", description = "Spicy deep fried chicken with curry leaves", isSpicy = true),
            Food(13, "Paneer Tikka", 220.0, 1, null, categoryName = "STARTERS", description = "Grilled cottage cheese with mint chutney", isVegetarian = true),
            // DESSERTS
            Food(14, "Gulab Jamun", 80.0, 1, null, categoryName = "DESSERTS", description = "Soft milk dumplings soaked in rose syrup", isVegetarian = true),
            Food(15, "Rasgulla", 70.0, 1, null, categoryName = "DESSERTS", description = "Soft spongy cottage cheese balls in syrup", isVegetarian = true),
            Food(16, "Mango Kulfi", 90.0, 1, null, categoryName = "DESSERTS", description = "Traditional Indian ice cream on a stick", isVegetarian = true),
            Food(17, "Gajar Ka Halwa", 110.0, 1, null, categoryName = "DESSERTS", description = "Classic carrot pudding with dry fruits", isVegetarian = true),
            // BEVERAGES
            Food(18, "Cold Coffee", 120.0, 1, null, categoryName = "BEVERAGES", description = "Chilled blended coffee with ice cream", isVegetarian = true),
            Food(19, "Mango Lassi", 99.0, 1, null, categoryName = "BEVERAGES", description = "Refreshing mango yogurt smoothie", isVegetarian = true),
            Food(20, "Fresh Lime Soda", 60.0, 1, null, categoryName = "BEVERAGES", description = "Freshly squeezed lime with soda water", isVegetarian = true),
            Food(21, "Masala Chai", 40.0, 1, null, categoryName = "BEVERAGES", description = "Traditional Indian spiced milk tea", isVegetarian = true),
        )
    }

    override suspend fun getFoods(): Result<List<Food>> = Result.Success(mockFoods)

    override suspend fun getFoodsPaginated(
        offset: Int,
        limit: Int,
        category: String?,
        sort: String?,
    ): PaginationResult<Food> {
        val filtered = if (category != null) {
            mockFoods.filter { it.categoryName.equals(category, ignoreCase = true) }
        } else {
            mockFoods
        }
        val sorted = when (sort) {
            "price:asc" -> filtered.sortedBy { it.price }
            "price:desc" -> filtered.sortedByDescending { it.price }
            "name:asc" -> filtered.sortedBy { it.name }
            "name:desc" -> filtered.sortedByDescending { it.name }
            else -> filtered
        }
        val page = sorted.drop(offset).take(limit)
        return PaginationResult.Success(
            Pagination(
                data = page,
                currentPage = if (limit > 0) offset / limit else 0,
                limit = limit,
                total = sorted.size,
                hasMore = offset + limit < sorted.size,
            )
        )
    }

    override suspend fun getFoodById(id: Long): Result<Food> {
        val food = mockFoods.find { it.id == id }
        return if (food != null) Result.Success(food) else Result.Failure(Exception("Food not found"))
    }

    override suspend fun searchFoods(query: String): Result<List<Food>> {
        val results = mockFoods.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
        }
        return Result.Success(results)
    }

    override suspend fun searchFoodsPaginated(
        query: String,
        offset: Int,
        limit: Int,
    ): PaginationResult<Food> {
        val filtered = mockFoods.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
        }
        val page = filtered.drop(offset).take(limit)
        return PaginationResult.Success(
            Pagination(
                data = page,
                currentPage = if (limit > 0) offset / limit else 0,
                limit = limit,
                total = filtered.size,
                hasMore = offset + limit < filtered.size,
            )
        )
    }
}
