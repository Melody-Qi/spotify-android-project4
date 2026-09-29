package com.laioffer.spotify.datamodel

/**
 * Lesson 57, screenshot 31 - one row of the playlist page.
 *
 * The fields map 1:1 to the /playlist/1 JSON (screenshot 30):
 *
 *   { "name": "HEXAGONAL (Intro)", "lyric": "Bizzy",
 *     "src": "http://10.0.2.2:8080/songs/LeeSSang_Hexagonal.mp3", "length": "3:47" }
 *
 * name   = the song title (white, or green while playing)
 * lyric  = actually the artist line shown under the title (the lesson keeps the
 *          backend's field name even though it is really "artists")
 * src    = the mp3 URL - static-hosted by the Ktor backend, used by a player later
 * length = display string like "3:47"
 */
data class Song(
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)
