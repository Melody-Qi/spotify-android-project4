package com.laioffer

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticBasePackage
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// All endpoints of the lecture live here, one block per screenshot of the lecture.
// The JSON files are read from src/main/resources (= classpath after build), never hard-coded.
fun Application.configureRouting() {
    routing {
        // 1. sanity check route -------------------------------------------------
        get("/") {
            call.respondText("Hello World!")
        }

        // 2. /feed -> resources/feed.json ---------------------------------------
        get("/feed") {
            val jsonString = this::class.java.classLoader.getResource("feed.json")?.readText()
            val json = Json.parseToJsonElement(jsonString ?: "")
            call.respondText(json.toString(), ContentType.Application.Json)
        }

        // 3. /playlists -> resources/playlists.json -----------------------------
        get("/playlists") {
            val jsonString = this::class.java.classLoader.getResource("playlists.json")?.readText()
            val json = Json.parseToJsonElement(jsonString ?: "")
            call.respondText(json.toString(), ContentType.Application.Json)
        }

        // 4. static mp3 hosting: GET /songs/<file>.mp3 -> resources/static/songs/
        static("/") {
            staticBasePackage = "static"
            static("songs") {
                resources("songs")
            }
        }

        // 5. /playlist/{id} -> deserialize + filter by id ------------------------
        // The lecture writes respondNullable(playlist); it is deprecated in Ktor 2.3 and
        // removed in Ktor 3, so an explicit 404 is used instead (same contract).
        get("playlist/{id}") {
            val jsonString = this::class.java.classLoader.getResource("playlists.json")?.readText() ?: ""
            val playlists = Json.decodeFromString(ListSerializer(Playlist.serializer()), jsonString)
            val id = call.parameters["id"]
            val playlist = playlists.firstOrNull { it.id.toString() == id }
            if (playlist != null) {
                call.respond(playlist)
            } else {
                call.respondText("Playlist $id not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
