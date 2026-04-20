package com.autobill.smartpos.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
 *        Removed ForeignKey constraints from OrderEntity, BillEntity, PaymentEntity.
 *        Cross-entity FKs on cached server rows caused SQLiteConstraintException when
 *        parent rows had not yet been fetched locally.
 *  v3  → April 21, 2026
 *        Two schema changes in the same release:
 *        (a) PendingOrderEntity.tableId: NOT NULL → nullable (Long → Long?)
 *            Required for TAKEAWAY / TABLE_MANAGEMENT=false orders (no table).
 *            Migration: create-copy-drop-rename to preserve all queued offline rows.
 *        (b) OrderEntity.tableNumber: NOT NULL → nullable (String → String?)
 *            Required to cache server orders that have no table assigned.
 *            Migration: drop + recreate orders table (pure server cache — safe to wipe).
 *        See FINAL_ORDER_TYPE_CONTRACT.md — Section C1.
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
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun tableDao(): TableDao
    abstract fun orderDao(): OrderDao
    abstract fun pendingOrderDao(): PendingOrderDao

    companion object {
        /**
         * v2 → v3: Make pending_orders.tableId nullable.
         *
         * SQLite does not support ALTER COLUMN, so we:
         *  1. Create pending_orders_new with the desired nullable tableId schema.
         *  2. Copy all existing rows — existing tableId values are valid longs, copy is lossless.
         *  3. Drop the old table.
         *  4. Rename the new table.
         *  5. Recreate the two indices (status, createdAt) that were defined on the entity.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // ── (a) pending_orders: make tableId nullable ──────────────────────────
                // Durable offline data — must be preserved. Use create-copy-drop-rename.

                // 1. Create new table with nullable tableId
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_orders_new` (
                        `id`            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `restaurantId`  INTEGER NOT NULL,
                        `tableId`       INTEGER,
                        `orderType`     TEXT    NOT NULL,
                        `notes`         TEXT,
                        `itemsJson`     TEXT    NOT NULL,
                        `status`        TEXT    NOT NULL DEFAULT 'PENDING',
                        `failureReason` TEXT,
                        `createdAt`     INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Copy all existing rows (tableId was NOT NULL so values are all valid longs)
                db.execSQL("""
                    INSERT INTO `pending_orders_new`
                        (id, restaurantId, tableId, orderType, notes, itemsJson, status, failureReason, createdAt)
                    SELECT
                        id, restaurantId, tableId, orderType, notes, itemsJson, status, failureReason, createdAt
                    FROM `pending_orders`
                """.trimIndent())

                // 3. Drop the old table
                db.execSQL("DROP TABLE `pending_orders`")

                // 4. Rename new table to the canonical name
                db.execSQL("ALTER TABLE `pending_orders_new` RENAME TO `pending_orders`")

                // 5. Recreate indices (Room won't recreate them automatically after rename)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_orders_status`    ON `pending_orders` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_orders_createdAt` ON `pending_orders` (`createdAt`)")

                // ── (b) orders: make tableNumber nullable ──────────────────────────────
                // Pure server-data cache — safe to drop and recreate. No data loss risk.

                db.execSQL("DROP TABLE IF EXISTS `orders`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `orders` (
                        `id`           INTEGER PRIMARY KEY NOT NULL,
                        `orderNumber`  TEXT    NOT NULL,
                        `restaurantId` INTEGER NOT NULL,
                        `tableId`      INTEGER,
                        `tableNumber`  TEXT,
                        `orderType`    TEXT    NOT NULL,
                        `status`       TEXT    NOT NULL,
                        `subtotal`     REAL    NOT NULL,
                        `totalAmount`  REAL    NOT NULL,
                        `notes`        TEXT,
                        `createdAt`    TEXT    NOT NULL,
                        `updatedAt`    TEXT    NOT NULL,
                        `version`      INTEGER NOT NULL
                    )
                """.trimIndent())

                // Recreate the four indices defined on OrderEntity
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_restaurantId` ON `orders` (`restaurantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_tableId`      ON `orders` (`tableId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_status`       ON `orders` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_createdAt`    ON `orders` (`createdAt`)")
            }
        }
    }
}
