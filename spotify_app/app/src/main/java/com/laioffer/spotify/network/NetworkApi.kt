package com.laioffer.spotify.network

import com.laioffer.spotify.datamodel.Playlist
import com.laioffer.spotify.datamodel.Section
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Lesson 54, step 1 of the network part - Retrofit turns this interface into a real
 * HTTP call. `@GET("feed")` is appended to the base url, so the final request is
 * http://10.0.2.2:8080/feed.
 *
 * The lesson shows two flavours:
 *   fun getHomeFeed(): Call<List<Section>>          <- callback / .execute() style (used here)
 *   suspend fun getHomeFeed(): List<Section>        <- coroutine style
 */
interface NetworkApi {

    @GET("feed")
    fun getHomeFeed(): Call<List<Section>>

    // Lesson 57, screenshot 35: the playlist endpoint. {id} is a path placeholder
    // filled by @Path, so getPlaylist(1) -> GET http://10.0.2.2:8080/playlist/1.
    @GET("playlist/{id}")
    fun getPlaylist(@Path("id") id: Int): Call<Playlist>
}
