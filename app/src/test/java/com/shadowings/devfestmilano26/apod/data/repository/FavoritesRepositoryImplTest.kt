package com.shadowings.devfestmilano26.apod.data.repository

import androidx.room.Room
import com.shadowings.devfestmilano26.apod.data.local.AppDatabase
import com.shadowings.devfestmilano26.apod.data.local.FavoriteDao
import com.shadowings.devfestmilano26.apod.data.local.SessionPreferences
import com.shadowings.devfestmilano26.apod.data.remote.FirestoreFavoritesSource
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.model.UserSession
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class FavoritesRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var firestoreSource: FirestoreFavoritesSource
    private lateinit var sessionPreferences: SessionPreferences
    private lateinit var repository: FavoritesRepositoryImpl

    private val picture = AstronomyPicture(
        date = LocalDate.of(2026, 5, 14),
        title = "The Star-Forming Pillars of M16",
        explanation = "Dense cold molecular hydrogen pillars.",
        url = "https://apod.example/m16.jpg",
        hdUrl = "https://apod.example/m16-hd.jpg",
        mediaType = MediaType.IMAGE,
        copyright = null,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        favoriteDao = db.favoriteDao()
        firestoreSource = mockk(relaxed = true)
        sessionPreferences = mockk()
        // Baseline stub so the constructor's remote-listener init block has something to collect;
        // individual tests below re-stub observe() for their own scenario before calling methods
        // that read it fresh (saveFavorite/removeFavorite/migrateLocalFavoritesToAccount).
        coEvery { sessionPreferences.observe() } returns flowOf(UserSession.SignedOut)
        repository = FavoritesRepositoryImpl(
            favoriteDao,
            firestoreSource,
            sessionPreferences,
            applicationScope = CoroutineScope(UnconfinedTestDispatcher()),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `save while signed out marks the favorite local-only`() = runTest {
        coEvery { sessionPreferences.observe() } returns flowOf(UserSession.SignedOut)

        repository.saveFavorite(picture)

        val status = repository.observeFavoriteStatus(picture.date).first()
        assertEquals(SyncStatus.LOCAL_ONLY, status)
    }

    @Test
    fun `save while signed in marks the favorite synced and mirrors to firestore`() = runTest {
        val session = UserSession(isSignedIn = true, uid = "uid-1", email = "a@b.com")
        coEvery { sessionPreferences.observe() } returns flowOf(session)
        coJustRun { firestoreSource.upsert(any(), any()) }

        repository.saveFavorite(picture)

        val status = repository.observeFavoriteStatus(picture.date).first()
        assertEquals(SyncStatus.SYNCED, status)
    }

    @Test
    fun `saving the same date twice updates instead of duplicating`() = runTest {
        coEvery { sessionPreferences.observe() } returns flowOf(UserSession.SignedOut)

        repository.saveFavorite(picture)
        repository.saveFavorite(picture.copy(title = "Updated title"))

        val favorites = repository.observeFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("Updated title", favorites.single().picture.title)
    }

    @Test
    fun `removing a favorite clears its status`() = runTest {
        coEvery { sessionPreferences.observe() } returns flowOf(UserSession.SignedOut)
        repository.saveFavorite(picture)

        repository.removeFavorite(picture.date)

        assertNull(repository.observeFavoriteStatus(picture.date).first())
    }

    @Test
    fun `migrating on login relabels every local-only favorite as synced`() = runTest {
        coEvery { sessionPreferences.observe() } returns flowOf(UserSession.SignedOut)
        coJustRun { firestoreSource.upsert(any(), any()) }
        repository.saveFavorite(picture)

        repository.migrateLocalFavoritesToAccount(uid = "uid-2")

        val status = repository.observeFavoriteStatus(picture.date).first()
        assertEquals(SyncStatus.SYNCED, status)
    }
}
