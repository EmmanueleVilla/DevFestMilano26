package com.shadowings.devfestmilano26.apod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedTodayDao {
    @Query("SELECT * FROM cached_today WHERE id = ${CachedTodayEntity.SINGLETON_ID}")
    fun observe(): Flow<CachedTodayEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedTodayEntity)
}
