package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.remote.dto.RestaurantDto
import com.autobill.smartpos.data.remote.dto.RestaurantSettingsDto
import com.autobill.smartpos.data.remote.dto.UpdateRestaurantRequest
import com.autobill.smartpos.data.remote.dto.UpdateRestaurantSettingsBody
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.RestaurantSettings
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest

// ── DTO → Domain ──────────────────────────────────────────────────────────────

fun RestaurantSettingsDto.toDomain(): RestaurantSettings = RestaurantSettings(
    enableTips             = enableTips,
    defaultTipPercentage   = defaultTipPercentage,
    autoPrintBill          = autoPrintBill,
    taxInclusive           = taxInclusive,
)

fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id         = id,
    name       = name,
    address    = address,
    phone      = phone,
    email      = email,
    logoUrl    = logoUrl,
    timezone   = timezone,
    currency   = currency,
    taxRate    = taxRate,
    isActive   = isActive,
    settings   = settings?.toDomain() ?: RestaurantSettings(),
    createdAt  = createdAt,
    updatedAt  = updatedAt,
)

// ── Domain → Request ──────────────────────────────────────────────────────────

fun UpdateRestaurantSettingsRequest.toDto(): UpdateRestaurantRequest {
    val hasSettingsChange = enableTips != null
            || defaultTipPercentage != null
            || autoPrintBill != null
            || taxInclusive != null
    return UpdateRestaurantRequest(
        taxRate  = taxRate,
        settings = if (hasSettingsChange) {
            UpdateRestaurantSettingsBody(
                enableTips           = enableTips,
                defaultTipPercentage = defaultTipPercentage,
                autoPrintBill        = autoPrintBill,
                taxInclusive         = taxInclusive,
            )
        } else null,
    )
}

