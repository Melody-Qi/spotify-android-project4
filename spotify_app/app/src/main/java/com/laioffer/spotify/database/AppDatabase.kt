package com.laioffer.spotify.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.laioffer.spotify.datamodel.Album

/**
 * Lesson 58, screenshots 14-15 - the database holder, the main access point to the
 * persisted data. Room's rules for this class (screenshot 9's checklist):
 *
 *   1. @Database annotation listing ALL entities ([Album::class]) - one entry = one table
 *   2. version = 1 - bump it whenever the schema changes (or Room crashes at runtime)
 *   3. abstract class extending RoomDatabase
 *   4. one abstract getter per DAO, zero arguments - Room generates the implementation
 *
 * The lesson's IDE screenshot names the getter `spotifyDao()` while the slide and the
 * DatabaseModule both call `databaseDao()`; we use `databaseDao()` everywhere so the
 * three files stay consistent.
 *
 * exportSchema = false: Room would otherwise warn (KSP) that no schema export
 * directory is configured. Schema export matters for production apps that need
 * migration history checked in; this course project has version = 1 and no
 * migrations, so silencing it is the honest setting.
 */
@Database(entities = [Album::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDao(): DatabaseDao
}
