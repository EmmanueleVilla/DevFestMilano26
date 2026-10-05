package com.shadowings.devfestmilano26.core.di

import com.shadowings.devfestmilano26.apod.data.repository.FavoritesRepositoryImpl
import com.shadowings.devfestmilano26.apod.data.repository.SessionRepositoryImpl
import com.shadowings.devfestmilano26.apod.data.repository.TodayPictureRepositoryImpl
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.repository.SessionRepository
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTodayPictureRepository(impl: TodayPictureRepositoryImpl): TodayPictureRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
