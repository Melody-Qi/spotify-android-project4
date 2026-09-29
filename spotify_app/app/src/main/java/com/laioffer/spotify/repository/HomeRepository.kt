package com.laioffer.spotify.repository

import com.laioffer.spotify.datamodel.Section
import com.laioffer.spotify.network.NetworkApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Lesson 55 (Android part, screenshot 23) - the Repository: the layer that hides
 * *where* the data comes from. The ViewModel asks this class for the feed and never
 * sees Retrofit.
 *
 * Teacher's final version (Lesson 55), reproduced line for line:
 *
 *   class HomeRepository @Inject constructor(private val networkApi: NetworkApi) {
 *       suspend fun getHomeSections(): List<Section> = withContext(Dispatchers.IO) {
 *           networkApi.getHomeFeed().execute().body() ?: listOf()
 *       }
 *   }
 *
 * Reading it piece by piece:
 *
 *   @Inject constructor      -> Hilt can build it, and fills NetworkApi in for us
 *   suspend                  -> it may take a while, so it can only run in a coroutine
 *   withContext(IO)          -> moves .execute() off the main thread (otherwise:
 *                               NetworkOnMainThreadException)
 *   body() ?: listOf()       -> empty list instead of null if the response has no body
 *
 * Note the two names on the same line:
 *   getHomeSections()  = the name the *ViewModel* calls (what the screen needs)
 *   getHomeFeed()      = the name Retrofit declares (what the HTTP endpoint is called)
 * That is the point of the pattern: each layer offers the same job to the layer above,
 * under its own vocabulary.
 */
class HomeRepository @Inject constructor(private val networkApi: NetworkApi) {

    suspend fun getHomeSections(): List<Section> = withContext(Dispatchers.IO) {
        networkApi.getHomeFeed().execute().body() ?: listOf()
    }
}
