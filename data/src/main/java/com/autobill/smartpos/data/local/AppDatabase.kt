package com.autobill.smartpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.local.dao.PendingOrderDao
import com.autobill.smartpos.data.local.dao.RestaurantDao
import com.autobill.smartpos.data.local.dao.TableDao
import com.autobill.smartpos.data.local.entity.BillEntity
import com.autobill.smartpos.data.local.entity.FoodEntity
import com.autobill.smartpos.data.local.entity.OrderEntity
import com.autobill.smartpos.data.local.entity.OrderItemEntity
import com.autobill.smartpos.data.local.entity.PaymentEntity
import com.autobill.smartpos.data.local.entity.PendingOrderEntity
import com.autobill.smartpos.data.local.entity.RestaurantEntity
import com.autobill.smartpos.data.local.entity.TableEntity

/**
 * Room Database: AppDatabase
 *
 * Version history:
 *  v1  → initial production schema (April 19, 2026)
 *        Entities: FoodEntity, RestaurantEntity, TableEntity, OrderEntity,
 *        OrderItemEntity, BillEntity, PaymentEntity, PendingOrderEntity
 *
 * NOTE: versions 1–11 existed during development only (app was never released).
 * Schema was reset to v1 on April 19, 2026 before first production release.
 * Add migrations here when releasing updates to production users.
 */
@Database(
    entities = [
        FoodEntity::class,
        RestaurantEntity::class,
        TableEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        BillEntity::class,
        PaymentEntity::class,
        PendingOrderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun tableDao(): TableDao
    abstract fun orderDao(): OrderDao
    abstract fun pendingOrderDao(): PendingOrderDao
}
