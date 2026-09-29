package com.laioffer.spotify.repository

import com.laioffer.spotify.datamodel.Playlist
import com.laioffer.spotify.network.NetworkApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Lesson 57, screenshot 37 - the Model layer of the playlist page, built exactly like
 * HomeRepository in Lesson 55:
 *
 *   1. @Inject constructor  -> Hilt can build PlaylistRepository and fills NetworkApi in
 *   2. suspend              -> may take seconds, so it can only be called in a coroutine
 *   3. withContext(IO)      -> moves the blocking .execute() off the main thread
 *
 * `body()!!` is the lesson's version (it trusts the demo backend to always answer);
 * our PlaylistViewModel adds a try/catch around the call so a dead backend degrades
 * to "empty playlist" instead of crashing, matching what we did for the home feed.
 */
class PlaylistRepository @Inject constructor(private val networkApi: NetworkApi) {

    suspend fun getPlaylist(id: Int): Playlist = withContext(Dispatchers.IO) {
        networkApi.getPlaylist(id).execute().body()!!
    }
}
