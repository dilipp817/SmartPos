package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.FoodEntity
import com.autobill.smartpos.data.remote.dto.FoodDto
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodMappersTest {

    @Test
    fun `dto maps to entity correctly`() {
        val dto = FoodDto(
            id = 1,
            name = "Margherita Pizza",
            price = 299.99,
            restaurantId = 1,
        )

        val entity = dto.toEntity()

        assertEquals(1, entity.id)
        assertEquals("Margherita Pizza", entity.name)
        assertEquals(299.99, entity.price, 0.0)
        assertEquals(1, entity.restaurantId)
    }

    @Test
    fun `entity maps to domain correctly`() {
        val entity = FoodEntity(
            id = 2,
            name = "Paneer Tikka",
            price = 249.5,
            restaurantId = 1,
        )

        val domain = entity.toDomain()

        assertEquals(2, domain.id)
        assertEquals("Paneer Tikka", domain.name)
        assertEquals(249.5, domain.price, 0.0)
        assertEquals(1, domain.restaurantId)
    }
}

