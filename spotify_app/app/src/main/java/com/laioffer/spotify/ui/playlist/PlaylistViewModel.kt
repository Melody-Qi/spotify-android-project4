package com.laioffer.spotify.ui.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song
import com.laioffer.spotify.repository.FavoriteAlbumRepository
import com.laioffer.spotify.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lesson 57, screenshots 43-45 - one snapshot of the playlist page.
 *
 *   album       = the album this page shows (arrived via navigation, Lesson 57 step 8)
 *   isFavorite  = heart state; since Lesson 58 it is DRIVEN by the Room database (a Flow
 *                 subscription in fetchPlaylist), not a plain field anymore
 *   playlist    = the track list fetched from /playlist/{id}, empty until loaded
 */
data class PlaylistUiState(
    val album: Album,
    val isFavorite: Boolean = false,
    val playlist: List<Song> = emptyList()
)

/**
 * Lesson 57, screenshots 39-45 - the ViewModel of the playlist page, built exactly
 * like HomeViewModel (Lesson 55): @HiltViewModel + @Inject constructor + one private
 * MutableStateFlow exposed as a read-only StateFlow.
 *
 * fetchPlaylist(album) does two things, in the order the MVVM diagram (screenshot 29,
 * left half) numbers them:
 *
 *   1. copy(album = album)  -> remember which album we are on, IMMEDIATELY (the screen
 *      can already draw cover/title from it)
 *   2. viewModelScope.launch -> fetch the track list (2.Set Album And Fetch New Playlist
 *      -> 3.Fetch Playlist Content -> 4.Got Playlist from Restful Server -> 5.Update UI
 *      State) and copy(playlist = songs) when it arrives.
 *
 * Note `copy`: a data class built-in that clones the state changing ONLY the given
 * fields - the rest stay untouched. Never rebuild the whole state by hand.
 *
 * Coroutine revisit (the lesson re-reads it here too): viewModelScope dies with the
 * screen, so a slow /playlist request can never outlive the page and leak.
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    // Lesson 58, screenshot 22: the local-data side of the MVVM diagram joins the party.
    private val favoriteAlbumRepository: FavoriteAlbumRepository
) : ViewModel() {

    // The initial state carries an "empty" Album (id = -1): the real one arrives with
    // fetchPlaylist() a moment after the screen opens.
    private val _uiState = MutableStateFlow(
        PlaylistUiState(
            Album(
                id = -1,
                name = "",
                cover = "",
                description = "",
                artists = "",
                year = ""
            )
        )
    )
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    /** Called by PlaylistFragment.onViewCreated with the album from Safe Args. */
    fun fetchPlaylist(album: Album) {
        _uiState.value = _uiState.value.copy(album = album)

        viewModelScope.launch {
            try {
                val playlist = playlistRepository.getPlaylist(album.id)
                _uiState.value = _uiState.value.copy(playlist = playlist.songs)
                Log.d(TAG, _uiState.value.toString())
            } catch (t: Throwable) {
                // OUR addition, not in the lesson (same reason as HomeViewModel): a dead
                // backend must not crash the page - keep the album, just show no songs.
                Log.w(TAG, "fetchPlaylist failed: ${t.javaClass.simpleName}: ${t.message}")
                _uiState.value = _uiState.value.copy(playlist = emptyList())
            }
        }

        // Lesson 58, screenshot 22: a SECOND coroutine in the same viewModelScope - the
        // two run in parallel. One fetches the track list over the network, the other
        // asks the local database whether this album was favorited before. collect {}
        // re-fires on every table change, so the heart also UN-lights the moment the
        // row is deleted.
        viewModelScope.launch {
            favoriteAlbumRepository.isFavoriteAlbum(album.id).collect { isFavorite ->
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    /**
     * Lesson 58, screenshot 24: the write path behind the heart tap. if/else on the NEW
     * state (Cover already calls onTapFavorite(!isFavorite)) -> insert or delete the row.
     * Both are suspend repository calls, so it must run inside a coroutine.
     */
    fun toggleFavorite(isFavorite: Boolean) {
        val album = _uiState.value.album
        viewModelScope.launch {
            if (isFavorite) {
                favoriteAlbumRepository.favoriteAlbum(album)
            } else {
                favoriteAlbumRepository.unFavoriteAlbum(album)
            }
        }
    }

    private companion object {
        const val TAG = "PlaylistViewModel"
    }
}
