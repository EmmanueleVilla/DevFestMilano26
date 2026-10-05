package com.shadowings.devfestmilano26.apod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAtEpochMillis DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT syncStatus FROM favorites WHERE date = :date")
    fun observeStatus(date: String): Flow<String?>

    @Query("SELECT * FROM favorites WHERE date = :date")
    suspend fun getByDate(date: String): FavoriteEntity?

    @Query("SELECT * FROM favorites WHERE syncStatus = 'LOCAL_ONLY'")
    suspend fun getLocalOnly(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("UPDATE favorites SET syncStatus = :status, ownerUid = :uid WHERE date = :date")
    suspend fun updateSyncStatus(date: String, status: String, uid: String?)
}
