package com.laioffer.spotify.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.laioffer.spotify.datamodel.Album
import kotlinx.coroutines.flow.Flow

/**
 * Lesson 58, screenshot 12 - the DAO (Data Access Object): an interface whose methods
 * ARE the SQL. Room generates the implementation at compile time (via KSP), so there
 * is no hand-written SQLite anywhere.
 *
 *   favoriteAlbum(album)    -> INSERT. onConflict = REPLACE: re-favoriting the same id
 *                              overwrites the row instead of crashing with a UNIQUE
 *                              constraint violation.
 *   isFavoriteAlbum(id)     -> SELECT EXISTS(...) returns true/false; wrapped in a
 *                              Flow<Boolean> so the UI is notified on EVERY table change
 *                              (that is what makes the heart light up on app restart).
 *   unFavoriteAlbum(album)  -> DELETE the row.
 *   fetchFavoriteAlbums()   -> SELECT * FROM Album (screenshot 27: the Favorite page's
 *                              ViewModel collects this to list all favorited albums).
 *
 * suspend = "this call may take long" (it runs off the main thread); Flow = "subscribe
 * to future updates". Insert/Delete are one-shot -> suspend; the two queries push
 * updates -> Flow.
 */
@Dao
interface DatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun favoriteAlbum(album: Album)

    @Query("SELECT EXISTS(SELECT * FROM Album WHERE id = :id)")
    fun isFavoriteAlbum(id: Int): Flow<Boolean>

    @Delete
    suspend fun unFavoriteAlbum(album: Album)

    // Screenshot 27: the Favorite page lists every row - a plain SELECT * wrapped in Flow.
    @Query("SELECT * FROM Album")
    fun fetchFavoriteAlbums(): Flow<List<Album>>
}
