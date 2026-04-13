package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.remote.dto.CategoryDto
import com.autobill.smartpos.data.remote.dto.CreateCategoryRequest
import com.autobill.smartpos.domain.model.Category

// ── DTO → Domain ──────────────────────────────────────────────────────────────

fun CategoryDto.toDomain(): Category = Category(
    id           = id,
    name         = name,
    description  = description,
    imageUrl     = imageUrl,
    displayOrder = displayOrder,
    isActive     = isActive,
    foodCount    = foodCount,
)

// ── Domain params → Request ───────────────────────────────────────────────────

fun buildCreateCategoryRequest(
    name: String,
    description: String?,
    imageUrl: String?,
    displayOrder: Int,
): CreateCategoryRequest = CreateCategoryRequest(
    name         = name,
    description  = description,
    imageUrl     = imageUrl,
    displayOrder = displayOrder,
)

