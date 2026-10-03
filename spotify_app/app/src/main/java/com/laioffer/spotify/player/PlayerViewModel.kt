package com.laioffer.spotify.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Handout used the legacy com.google.android.exoplayer2 package; that artifact is no
// longer downloadable (jcenter is dead), so we use its official successor androidx.media3.
// The APIs are identical - only the import package changed (see build.gradle.kts note).
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lesson 60 - the playback ViewModel, built up in 4 steps across the lesson:
 *
 * 1. screenshots 08-09 : empty @HiltViewModel that @Inject's the singleton ExoPlayer
 *                        (provided by PlayerModule), plus the PlayerUiState data class
 * 2. screenshot 10     : load() / play() / pause() to drive the player
 * 3. screenshots 20-22 : implement Player.Listener so the ViewModel HEARS the player -
 *                        onIsPlayingChanged mirrors the playing flag into uiState,
 *                        onPlayerError logs playback failures (bad URL, no network...)
 * 4. screenshot 24     : a polling Flow that samples currentPosition/duration every
 *                        second while playing, so the UI can show progress later
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer
) : ViewModel(), Player.Listener {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        // The ViewModel implements Player.Listener, so it can register ITSELF
        // (screenshots 21, 28-29 for the classroom mini-demo of this mechanism).
        exoPlayer.addListener(this)

        // Screenshot 24: poll the progress once per second. ExoPlayer has no
        // "position changed" callback, so the handout polls while playing.
        viewModelScope.launch {
            flow {
                while (true) {
                    if (exoPlayer.isPlaying) {
                        emit(exoPlayer.currentPosition to exoPlayer.duration)
                    }
                    delay(1000)
                }
            }.collect { (current, duration) ->
                _uiState.value = _uiState.value.copy(currentMs = current, durationMs = duration)
                Log.d("SpotifyPlayer", "CurrentMs: $current, DurationMs: $duration")
            }
        }
    }

    // Screenshot 10: hand a song to the player. MediaItem is ExoPlayer's wrapper for
    // "something playable" - here built from the song's mp3 URL (song.src, hosted by
    // the Ktor backend, e.g. http://10.0.2.2:8080/songs/LeeSSang_Hexagonal.mp3).
    // prepare() makes the player start buffering; play() then starts the audio.
    fun load(song: Song, album: Album) {
        _uiState.value = PlayerUiState(album = album, song = song, isPlaying = false)
        val mediaItem = MediaItem.Builder().setUri(song.src).build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    // Lesson 61, screenshot 13 - move the playhead. The uiState is updated
    // FIRST on purpose ("optimistically"): the 1s poll would otherwise take up
    // to a second to agree with the new position, and the slider thumb would
    // visibly jump back for up to a second after the user lets go.
    fun seekTo(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentMs = positionMs)
        exoPlayer.seekTo(positionMs)
    }

    // Lesson 61, "X out button" section - clear the current song.
    //   1. pause() first: stop the audio (the handout asks for pause, not stop -
    //      stop() would also throw away the playhead, which is stronger than asked).
    //   2. reset the whole state box: album and song go back to null, so
    //      isVisible = album != null && song != null becomes false and the bar
    //      slides out on its own - no "hide" call is needed anywhere.
    //      Resetting the whole PlayerUiState (instead of only album/song) also
    //      clears currentMs/durationMs, so no stale progress can flash when the
    //      next song is loaded.
    fun clear() {
        exoPlayer.pause()
        _uiState.value = PlayerUiState()
    }

    // Screenshot 22: the player tells us whether it is playing (user pressed pause,
    // audio focus loss, buffering...). Mirror it into uiState so the UI can switch
    // the row highlight / play-pause icon.
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d("SpotifyPlayer", isPlaying.toString())
        _uiState.value = _uiState.value.copy(
            isPlaying = isPlaying
        )
    }

    // Screenshot 22: log playback errors (404 mp3, wrong URL, codec issues...).
    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.d("SpotifyPlayer", error.toString())
    }

    override fun onCleared() {
        exoPlayer.removeListener(this)
        super.onCleared()
    }
}

data class PlayerUiState(
    val album: Album? = null,
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val currentMs: Long = 0,
    val durationMs: Long = 0
)
