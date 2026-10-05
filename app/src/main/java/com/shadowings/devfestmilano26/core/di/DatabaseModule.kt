package com.shadowings.devfestmilano26.core.di

import android.content.Context
import androidx.room.Room
import com.shadowings.devfestmilano26.apod.data.local.AppDatabase
import com.shadowings.devfestmilano26.apod.data.local.CachedTodayDao
import com.shadowings.devfestmilano26.apod.data.local.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "devfestmilano26.db").build()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideCachedTodayDao(database: AppDatabase): CachedTodayDao = database.cachedTodayDao()
}
