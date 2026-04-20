package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response body for GET /api/v1/feature-flags
 *
 * Backend contract:
 * {
 *   "data": {
 *     "flags": {
 *       "offline_order_queue": false,
 *       "real_time_updates": true,
 *       ...
 *     }
 *   }
 * }
 *
 * Keys must match [com.autobill.smartpos.domain.featureflag.FeatureFlag.key].
 * Unknown keys are silently ignored. Missing keys fall back to the flag's defaultValue.
 */
@JsonClass(generateAdapter = true)
data class FeatureFlagResponseDto(
    @param:Json(name = "flags")
    val flags: Map<String, Boolean> = emptyMap(),
)

