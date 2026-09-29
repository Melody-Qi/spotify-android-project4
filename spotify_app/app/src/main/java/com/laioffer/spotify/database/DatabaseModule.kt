package com.laioffer.spotify.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Lesson 58, screenshots 16-17 - "similar to retrofit", the database is provided by a
 * Hilt module instead of being built by hand:
 *
 *   @Module @InstallIn(SingletonComponent::class)  -> lives for the whole app process
 *   provideDatabase     -> Room.databaseBuilder(context, AppDatabase::class.java, "spotify_db")
 *                          (the file lands in app-private storage as spotify_db)
 *   provideDatabaseDao  -> anyone asking for a DatabaseDao gets it FROM the database
 *                          (so both @Provides must be @Singleton: one database, one dao)
 *
 * Contrast with NetworkModule: Retrofit objects are cheap to rebuild, a RoomDatabase is
 * not (it owns a SQLite connection) - hence the explicit @Singleton here.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "spotify_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDatabaseDao(database: AppDatabase): DatabaseDao {
        return database.databaseDao()
    }
}
