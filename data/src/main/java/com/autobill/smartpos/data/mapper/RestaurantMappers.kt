package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.remote.dto.RestaurantAddressDto
import com.autobill.smartpos.data.remote.dto.RestaurantDto
import com.autobill.smartpos.data.remote.dto.StoreAddressRequest
import com.autobill.smartpos.data.remote.dto.UpdateRestaurantRequest
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.RestaurantAddress
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest

// ── DTO → Domain ──────────────────────────────────────────────────────────────

fun RestaurantAddressDto.toDomain(): RestaurantAddress = RestaurantAddress(
    building = building,
    street   = street,
    location = location,
    zipCode  = zipCode,
)

fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id            = id,
    outletName    = outletName,
    displayName   = displayName,
    outletManager = outletManager,
    address       = address.toDomain(),
    createdAt     = createdAt,
    updatedAt     = updatedAt,
)

// ── Domain → Request ──────────────────────────────────────────────────────────

fun UpdateRestaurantSettingsRequest.toDto(): UpdateRestaurantRequest {
    val hasAddressChange = building != null || street != null || location != null || zipCode != null
    return UpdateRestaurantRequest(
        outletName    = outletName,
        displayName   = displayName,
        outletManager = outletManager,
        storeAddress  = if (hasAddressChange) StoreAddressRequest(
            building = building,
            street   = street,
            location = location,
            zipCode  = zipCode,
        ) else null,
    )
}
