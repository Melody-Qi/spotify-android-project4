package com.laioffer.spotify.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Lesson 54, step 4 - one horizontal block of the home feed ("Top mixes", ...).
 * `section_title` is snake_case in JSON, so it needs @SerializedName to become the
 * Kotlin-style `sectionTitle`.
 *
 * Serializable for the same reason as Album: it can be passed around in a Bundle.
 */
data class Section(
    @SerializedName("section_title") val sectionTitle: String,
    val albums: List<Album>
) : Serializable
