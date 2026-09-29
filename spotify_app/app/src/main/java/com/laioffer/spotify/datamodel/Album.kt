package com.laioffer.spotify.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Lesson 54, step 3 of the network part - the data model for one album in /feed.
 *
 * Note the mismatch the lesson points out: the JSON field is called "album", but in
 * Kotlin it reads better as `name`, so @SerializedName maps one to the other. Without
 * it Gson would leave `name` null.
 *
 * Serializable lets a whole Album travel inside a navigation Bundle (that is how a
 * detail screen would receive an album later); empty() is the placeholder (id = -1)
 * used when there is nothing to show yet.
 */
data class Album(
    val id: Int,
    @SerializedName("album") val name: String,
    val year: String,
    val cover: String,
    val artists: String,
    val description: String
) : Serializable {
    companion object {
        fun empty(): Album {
            return Album(id = -1, "", "", "", "", "")
        }
    }
}
