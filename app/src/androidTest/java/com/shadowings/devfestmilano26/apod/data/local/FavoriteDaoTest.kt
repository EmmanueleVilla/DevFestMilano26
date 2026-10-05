package com.shadowings.devfestmilano26.apod.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavoriteDao

    private val entity = FavoriteEntity(
        date = "2026-05-14",
        title = "The Star-Forming Pillars of M16",
        explanation = "Dense cold molecular hydrogen pillars.",
        url = "https://apod.example/m16.jpg",
        hdUrl = null,
        mediaType = "IMAGE",
        copyright = null,
        syncStatus = "LOCAL_ONLY",
        ownerUid = null,
        savedAtEpochMillis = 0L,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.favoriteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertThenObserveAllReturnsTheRow() = runBlocking {
        dao.upsert(entity)

        val all = dao.observeAll().first()

        assertEquals(1, all.size)
        assertEquals(entity.title, all.single().title)
    }

    @Test
    fun upsertingTheSameDateReplacesTheRowInsteadOfDuplicating() = runBlocking {
        dao.upsert(entity)
        dao.upsert(entity.copy(title = "Updated title"))

        val all = dao.observeAll().first()

        assertEquals(1, all.size)
        assertEquals("Updated title", all.single().title)
    }

    @Test
    fun deleteByDateRemovesTheRow() = runBlocking {
        dao.upsert(entity)

        dao.deleteByDate(entity.date)

        assertNull(dao.getByDate(entity.date))
    }

    @Test
    fun updateSyncStatusChangesStatusAndOwner() = runBlocking {
        dao.upsert(entity)

        dao.updateSyncStatus(entity.date, "SYNCED", "uid-1")

        val updated = dao.getByDate(entity.date)
        assertEquals("SYNCED", updated?.syncStatus)
        assertEquals("uid-1", updated?.ownerUid)
    }
}
