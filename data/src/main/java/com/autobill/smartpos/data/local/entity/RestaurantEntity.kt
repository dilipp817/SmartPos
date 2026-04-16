package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity: Restaurant
 *
 * Schema updated for MOBILE_GUIDE_REVIEW.md 1.1 (April 16, 2026).
 * Old fields removed: name, phone, email, logoUrl, timezone, currency, taxRate, isActive.
 * New fields: outletName, displayName, outletManager, address (flattened).
 *
 * Note: RestaurantEntity is stored in Room only as a FK target for tables/orders/bills/payments.
 * Actual restaurant data is read/written via RestaurantDataStore (DataStore Preferences).
 * There is no RestaurantDao — the entity is never directly queried.
 *
 * DB migration: MIGRATION_9_10 in AppDatabase.
 */
@Entity(
    tableName = "restaurants",
    indices = [Index(value = ["id"])],
)
data class RestaurantEntity(
    @PrimaryKey
    val id: Long,
    val outletName: String,
    val displayName: String,
    val outletManager: String,
    val addressBuilding: String,
    val addressStreet: String,
    val addressLocation: String,
    val addressZipCode: String,
    val createdAt: String,
    val updatedAt: String,
)
