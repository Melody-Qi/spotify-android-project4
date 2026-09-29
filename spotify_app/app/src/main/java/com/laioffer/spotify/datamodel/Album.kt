package com.laioffer.spotify.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey
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
 *
 * Lesson 58 - Album is now ALSO a Room entity (screenshot 11): @Entity marks the class
 * as a table (table name = "Album") and @PrimaryKey marks `id` as the row key. Room
 * maps every remaining property to a column with the same name, so the table looks like:
 *
 *   Album(id INTEGER PRIMARY KEY, name TEXT, year TEXT, cover TEXT,
 *         artists TEXT, description TEXT)
 *
 * One class, three roles: Gson model (network) + navigation payload (Serializable)
 * + Room row (local database).
 */
@Entity
data class Album(
    @PrimaryKey val id: Int,
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
