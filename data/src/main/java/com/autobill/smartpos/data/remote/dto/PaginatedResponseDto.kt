package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Pagination metadata returned inside all paginated responses.
 * API shape: { "current_page": 0, "limit": 20, "total": 45, "total_pages": 3, "has_next": true, "has_previous": false }
 */
@JsonClass(generateAdapter = true)
data class PaginationDto(
    @param:Json(name = "current_page")
    val currentPage: Int,
    @param:Json(name = "limit")
    val limit: Int,
    @param:Json(name = "total")
    val total: Int,
    @param:Json(name = "total_pages")
    val totalPages: Int,
    @param:Json(name = "has_next")
    val hasNext: Boolean,
    @param:Json(name = "has_previous")
    val hasPrevious: Boolean,
)

/**
 * Generic paginated data container used by Food and Category list endpoints.
 * API shape: { "data": [...], "pagination": { ... } }
 */
@JsonClass(generateAdapter = true)
data class PagedDataDto<T>(
    @param:Json(name = "data")
    val data: List<T>,
    @param:Json(name = "pagination")
    val pagination: PaginationDto,
)
