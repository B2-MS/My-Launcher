package com.mylauncher.di

import android.content.Context
import com.mylauncher.data.preferences.PreferencesManager
import com.mylauncher.data.repository.AppRepository
import com.mylauncher.data.repository.TileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository = AppRepository(context)

    @Provides
    @Singleton
    fun provideTileRepository(): TileRepository = TileRepository()

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)
}
