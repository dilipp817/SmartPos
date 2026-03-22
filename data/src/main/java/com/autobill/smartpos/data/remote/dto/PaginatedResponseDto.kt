package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Paginated API response wrapper.
 * Used for all paginated endpoints.
 *
 * @param data List of items in current page
 * @param currentPage Current page number (0-indexed)
 * @param limit Items per page
 * @param total Total items available
 * @param hasMore Whether more pages available
 */
@JsonClass(generateAdapter = true)
data class PaginatedResponseDto<T>(
    @Json(name = "data")
    val data: List<T>,
    @Json(name = "current_page")
    val currentPage: Int,
    @Json(name = "limit")
    val limit: Int,
    @Json(name = "total")
    val total: Int,
    @Json(name = "has_more")
    val hasMore: Boolean,
)

