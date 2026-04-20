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
 *  v2  → April 20, 2026
 *        Removed ForeignKey constraints from OrderEntity (restaurantId→RestaurantEntity,
 *        tableId→TableEntity), BillEntity (orderId→OrderEntity, restaurantId→RestaurantEntity),
 *        and PaymentEntity (orderId→OrderEntity, billId→BillEntity).
 *        These FK constraints caused SQLiteConstraintException when server orders/bills
 *        were cached before their referenced parent rows existed locally.
 *        Much of this database mirrors server data, so cross-entity FKs on cached rows
 *        are not appropriate. However, this database is not cache-only: it also stores
 *        durable offline data in PendingOrderEntity for the pending order queue.
 *        Because pending orders cannot be re-fetched from the API after local loss,
 *        destructive migration is not safe while that queue remains stored in Room.
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
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun tableDao(): TableDao
    abstract fun orderDao(): OrderDao
    abstract fun pendingOrderDao(): PendingOrderDao
}
