package com.laioffer.spotify.repository

import com.laioffer.spotify.database.DatabaseDao
import com.laioffer.spotify.datamodel.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Lesson 58, screenshots 20-21 - the data layer of the "favorite album" feature, built
 * exactly like PlaylistRepository (Lesson 57): @Inject constructor so Hilt builds it,
 * and every database call kept off the main thread.
 *
 *   isFavoriteAlbum(id)  -> the DAO already returns Flow<Boolean>; flowOn(IO) moves the
 *                           emission upstream to a background thread (the collector in
 *                           the ViewModel stays on main).
 *   favoriteAlbum(...)   -> one-shot write: suspend + withContext(IO).
 *   unFavoriteAlbum(...) -> one-shot delete: same pattern.
 *   fetchFavoriteAlbums()-> Flow<List<Album>> for the Favorite page (screenshot 27).
 *
 * Why a repository at all when the DAO exists? Same reason as the network side: the
 * ViewModel should not know WHERE data comes from (Retrofit or Room) - swapping or
 * combining sources stays a Model-layer decision.
 */
class FavoriteAlbumRepository @Inject constructor(private val databaseDao: DatabaseDao) {

    fun isFavoriteAlbum(id: Int): Flow<Boolean> =
        databaseDao.isFavoriteAlbum(id).flowOn(Dispatchers.IO)

    suspend fun favoriteAlbum(album: Album) = withContext(Dispatchers.IO) {
        databaseDao.favoriteAlbum(album)
    }

    suspend fun unFavoriteAlbum(album: Album) = withContext(Dispatchers.IO) {
        databaseDao.unFavoriteAlbum(album)
    }

    fun fetchFavoriteAlbums(): Flow<List<Album>> =
        databaseDao.fetchFavoriteAlbums().flowOn(Dispatchers.IO)
}
