package com.laioffer

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Data classes live at the top of this file (lecture: "add the data class to the top of main
// function in the Application.kt"). They mirror the shape of resources/playlists.json.
@Serializable
data class Song(
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

@Serializable
data class Playlist(
    val id: Long,
    val songs: List<Song>
)

fun main() {
    // port / host exactly as in the lecture: 0.0.0.0:8080 (the emulator reaches it via 10.0.2.2:8080)
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // ContentNegotiation = "turn Kotlin objects into JSON automatically".
    // That is what makes call.respond(playlist) print pretty JSON further down the road.
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    configureRouting()
}
