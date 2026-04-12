package com.autobill.smartpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.local.dao.TableDao
import com.autobill.smartpos.data.local.entity.BillEntity
import com.autobill.smartpos.data.local.entity.FoodEntity
import com.autobill.smartpos.data.local.entity.OrderEntity
import com.autobill.smartpos.data.local.entity.OrderItemEntity
import com.autobill.smartpos.data.local.entity.PaymentEntity
import com.autobill.smartpos.data.local.entity.RestaurantEntity
import com.autobill.smartpos.data.local.entity.TableEntity

/**
 * Room Database: AppDatabase
 *
 * Version history:
 *  v1 → initial schema (FoodEntity basic fields)
 *  v2 → added imageUrl, category, description, isAvailable to FoodEntity
 *  v3 → (internal — see git history)
 *  v4 → replaced FoodEntity.category with categoryId + categoryName; added isVegetarian + isSpicy
 *  v5 → added RestaurantEntity, TableEntity, OrderEntity, OrderItemEntity,
 *        BillEntity, PaymentEntity (Item 15 — April 12, 2026)
 *       Deprecated: CustomerEntity, MenuItemEntity, MenuItemVariantEntity (not registered)
 *  v6 → added paidAmount + remainingAmount columns to bills table
 *        (BACKEND_ALIGNMENT.md Item 4 — April 12, 2026)
 *  v7 → added customerId column (nullable) to orders table
 *        (BACKEND_ALIGNMENT.md Q1 — April 12, 2026; always null in v1, used in v2)
 *  v8 → added preparationTime, allergens, calories columns to foods table
 *        (backendapi.md §7 — April 12, 2026)
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
    ],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun tableDao(): TableDao
    abstract fun orderDao(): OrderDao

    companion object {

        /**
         * Migration 3 → 4
         * Recreates foods table: category → categoryId + categoryName; adds isVegetarian + isSpicy.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `foods_new` (
                        `id`           INTEGER NOT NULL,
                        `name`         TEXT    NOT NULL,
                        `price`        REAL    NOT NULL,
                        `restaurantId` INTEGER NOT NULL,
                        `imageUrl`     TEXT,
                        `categoryId`   INTEGER,
                        `categoryName` TEXT,
                        `description`  TEXT,
                        `isAvailable`  INTEGER NOT NULL DEFAULT 1,
                        `isVegetarian` INTEGER NOT NULL DEFAULT 0,
                        `isSpicy`      INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `foods_new`
                        (id, name, price, restaurantId, imageUrl, categoryName, description, isAvailable)
                    SELECT id, name, price, restaurantId, imageUrl, category, description, isAvailable
                    FROM `foods`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `foods`")
                db.execSQL("ALTER TABLE `foods_new` RENAME TO `foods`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_foods_restaurantId` ON `foods` (`restaurantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_foods_categoryId`   ON `foods` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_foods_isAvailable`  ON `foods` (`isAvailable`)")
            }
        }

        /**
         * Migration 4 → 5  (Item 15 — April 12, 2026)
         * Adds six new tables: restaurants, tables, orders, order_items, bills, payments.
         * FoodEntity is unchanged — no foods table alteration needed.
         * CustomerEntity, MenuItemEntity, MenuItemVariantEntity are deprecated and NOT registered.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // ── restaurants ──────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `restaurants` (
                        `id`       INTEGER NOT NULL,
                        `name`     TEXT    NOT NULL,
                        `address`  TEXT    NOT NULL,
                        `phone`    TEXT    NOT NULL,
                        `email`    TEXT    NOT NULL,
                        `logoUrl`  TEXT    NOT NULL,
                        `timezone` TEXT    NOT NULL,
                        `currency` TEXT    NOT NULL,
                        `taxRate`  REAL    NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` TEXT   NOT NULL,
                        `updatedAt` TEXT   NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_restaurants_id` ON `restaurants` (`id`)")

                // ── tables ───────────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tables` (
                        `id`             INTEGER NOT NULL,
                        `tableNumber`    TEXT    NOT NULL,
                        `floor`          INTEGER NOT NULL,
                        `capacity`       INTEGER NOT NULL,
                        `status`         TEXT    NOT NULL,
                        `currentOrderId` INTEGER,
                        `lastOccupiedAt` TEXT,
                        `restaurantId`   INTEGER NOT NULL,
                        `createdAt`      TEXT    NOT NULL,
                        `updatedAt`      TEXT    NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`restaurantId`) REFERENCES `restaurants`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tables_restaurantId` ON `tables` (`restaurantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tables_status`       ON `tables` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tables_floor`        ON `tables` (`floor`)")

                // ── orders ───────────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `orders` (
                        `id`          INTEGER NOT NULL,
                        `orderNumber` TEXT    NOT NULL,
                        `restaurantId` INTEGER NOT NULL,
                        `tableId`     INTEGER,
                        `tableNumber` TEXT    NOT NULL,
                        `orderType`   TEXT    NOT NULL,
                        `status`      TEXT    NOT NULL,
                        `subtotal`    REAL    NOT NULL,
                        `totalAmount` REAL    NOT NULL,
                        `notes`       TEXT,
                        `createdAt`   TEXT    NOT NULL,
                        `updatedAt`   TEXT    NOT NULL,
                        `version`     INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`restaurantId`) REFERENCES `restaurants`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`tableId`)      REFERENCES `tables`(`id`)      ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_restaurantId` ON `orders` (`restaurantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_tableId`      ON `orders` (`tableId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_status`       ON `orders` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_createdAt`    ON `orders` (`createdAt`)")

                // ── order_items ──────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `order_items` (
                        `id`              INTEGER NOT NULL,
                        `orderId`         INTEGER NOT NULL,
                        `foodId`          INTEGER NOT NULL,
                        `foodName`        TEXT    NOT NULL,
                        `quantity`        INTEGER NOT NULL,
                        `unitPrice`       REAL    NOT NULL,
                        `subtotal`        REAL    NOT NULL,
                        `itemStatus`      TEXT    NOT NULL,
                        `specialRequests` TEXT,
                        `createdAt`       TEXT    NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_orderId`     ON `order_items` (`orderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_foodId`      ON `order_items` (`foodId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_itemStatus`  ON `order_items` (`itemStatus`)")

                // ── bills ────────────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bills` (
                        `id`             INTEGER NOT NULL,
                        `billNumber`     TEXT    NOT NULL,
                        `orderId`        INTEGER NOT NULL,
                        `restaurantId`   INTEGER NOT NULL,
                        `restaurantName` TEXT,
                        `subtotal`       REAL    NOT NULL,
                        `taxAmount`      REAL    NOT NULL,
                        `cgstAmount`     REAL    NOT NULL,
                        `sgstAmount`     REAL    NOT NULL,
                        `discountAmount` REAL    NOT NULL,
                        `totalAmount`    REAL    NOT NULL,
                        `status`         TEXT    NOT NULL,
                        `createdAt`      TEXT    NOT NULL,
                        `updatedAt`      TEXT    NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`orderId`)      REFERENCES `orders`(`id`)      ON DELETE CASCADE,
                        FOREIGN KEY(`restaurantId`) REFERENCES `restaurants`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_orderId`      ON `bills` (`orderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_restaurantId` ON `bills` (`restaurantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_status`       ON `bills` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_createdAt`    ON `bills` (`createdAt`)")

                // ── payments ─────────────────────────────────────────────────────────────
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `payments` (
                        `id`              INTEGER NOT NULL,
                        `orderId`         INTEGER NOT NULL,
                        `billId`          INTEGER,
                        `paymentMethod`   TEXT    NOT NULL,
                        `amount`          REAL    NOT NULL,
                        `status`          TEXT    NOT NULL,
                        `referenceNumber` TEXT    NOT NULL,
                        `transactionId`   TEXT,
                        `changeAmount`    REAL    NOT NULL,
                        `notes`           TEXT,
                        `createdAt`       TEXT    NOT NULL,
                        `updatedAt`       TEXT    NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`billId`)  REFERENCES `bills`(`id`)  ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_orderId`   ON `payments` (`orderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_billId`    ON `payments` (`billId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_status`    ON `payments` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_createdAt` ON `payments` (`createdAt`)")
            }
        }
        /**
         * Migration 5 → 6  (BACKEND_ALIGNMENT.md Item 4 — April 12, 2026)
         * Adds paidAmount + remainingAmount columns to the bills table.
         * Both default to 0.0 for existing rows (all pre-existing bills are ISSUED with no payments).
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bills` ADD COLUMN `paidAmount` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `bills` ADD COLUMN `remainingAmount` REAL NOT NULL DEFAULT 0.0")
            }
        }

        /**
         * Migration 6 → 7  (BACKEND_ALIGNMENT.md Q1 — April 12, 2026)
         * Adds customerId column (nullable Long) to the orders table.
         * Null for all existing rows — always null in v1; backend will populate in v2
         * when customer-linking ships.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `customerId` INTEGER DEFAULT NULL")
            }
        }

        /**
         * Migration 7 → 8  (backendapi.md §7 — April 12, 2026)
         * Adds preparationTime, allergens, calories columns to the foods table.
         * All nullable — existing rows default to NULL.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `preparationTime` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `allergens`       TEXT    DEFAULT NULL")
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `calories`        INTEGER DEFAULT NULL")
            }
        }
    }
}
