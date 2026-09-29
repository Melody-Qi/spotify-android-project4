package com.laioffer.spotify.datamodel

import com.google.gson.annotations.SerializedName

/**
 * Lesson 57, screenshot 33 - the response of GET /playlist/{id}.
 *
 *   { "id": 1, "songs": [ ...Song... ] }
 *
 * The JSON key is "id", but on the Kotlin side `albumId` reads much better next to
 * an Album - @SerializedName renames it without touching the wire format. (Gson
 * happily coerces the numeric id into the String field.)
 *
 * Note this is a different concept from Section (Lesson 54): a Section is a block of
 * the home feed holding *Albums*; a Playlist is one album's *track list*.
 */
data class Playlist(
    @SerializedName("id") val albumId: String,
    val songs: List<Song>
)
