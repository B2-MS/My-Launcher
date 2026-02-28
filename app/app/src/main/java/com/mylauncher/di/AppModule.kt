package com.mylauncher.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for dependency injection.
 *
 * Concrete classes (AppRepository, TileRepository, PreferencesManager) use
 * @Inject constructor + @Singleton for automatic binding — no manual @Provides
 * needed. This module is retained for future interface bindings or third-party
 * class providers.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
