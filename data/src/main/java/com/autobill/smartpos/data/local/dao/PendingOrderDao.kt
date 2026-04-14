package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobill.smartpos.data.local.entity.PendingOrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the offline order queue ([PendingOrderEntity]).
 *
 * Rows are inserted offline, updated to SYNCING during sync, and deleted on
 * successful server submission.  FAILED rows remain until the user retries.
 */
@Dao
interface PendingOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingOrderEntity): Long

    /** All PENDING rows — drained by [SyncWorker] in FIFO order. */
    @Query("SELECT * FROM pending_orders WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<PendingOrderEntity>

    /** All FAILED rows — shown in the "failed queue" UI. */
    @Query("SELECT * FROM pending_orders WHERE status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getFailed(): List<PendingOrderEntity>

    /**
     * Hot count of PENDING + FAILED rows.
     * Does NOT include SYNCING (in-flight) to avoid flickering badge.
     */
    @Query("SELECT COUNT(*) FROM pending_orders WHERE status IN ('PENDING', 'FAILED')")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM pending_orders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_orders SET status = :status, failureReason = :reason WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, reason: String? = null)

    /** Reset all SYNCING rows back to PENDING so a crash during sync doesn't strand items. */
    @Query("UPDATE pending_orders SET status = 'PENDING' WHERE status = 'SYNCING'")
    suspend fun resetSyncing()
}

