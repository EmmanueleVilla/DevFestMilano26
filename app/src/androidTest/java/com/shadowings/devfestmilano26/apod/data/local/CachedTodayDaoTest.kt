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
class CachedTodayDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CachedTodayDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.cachedTodayDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeIsNullBeforeAnyFetch() = runBlocking {
        assertNull(dao.observe().first())
    }

    @Test
    fun upsertOverwritesTheSingleRow() = runBlocking {
        dao.upsert(
            CachedTodayEntity(
                date = "2026-05-14",
                title = "First",
                explanation = "Explanation",
                url = "https://apod.example/first.jpg",
                hdUrl = null,
                mediaType = "IMAGE",
                copyright = null,
                fetchedAtEpochMillis = 1L,
            ),
        )
        dao.upsert(
            CachedTodayEntity(
                date = "2026-05-15",
                title = "Second",
                explanation = "Explanation",
                url = "https://apod.example/second.jpg",
                hdUrl = null,
                mediaType = "IMAGE",
                copyright = null,
                fetchedAtEpochMillis = 2L,
            ),
        )

        val cached = dao.observe().first()

        assertEquals("Second", cached?.title)
    }
}
