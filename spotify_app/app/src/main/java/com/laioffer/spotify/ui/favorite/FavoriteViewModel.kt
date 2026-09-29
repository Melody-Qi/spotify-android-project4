package com.laioffer.spotify.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.repository.FavoriteAlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lesson 58, screenshot 27 - the Favorite page's ViewModel, same shape as the other
 * ViewModels (HomeViewModel / PlaylistViewModel): @HiltViewModel + @Inject constructor
 * + MutableStateFlow exposed read-only.
 *
 * The difference: data is loaded in `init { }`, not via a fetch call - because the
 * source is Room's Flow. collect { } never completes: every INSERT/DELETE on the Album
 * table re-emits the full list, so the UI keeps itself up to date for free (the same
 * reactive wiring that lights the heart on the playlist page, screenshot 26's Live
 * updates column).
 */
data class FavoriteUiState(
    val albums: List<Album> = emptyList()
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteAlbumRepository: FavoriteAlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState(emptyList()))
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteAlbumRepository.fetchFavoriteAlbums().collect { albums ->
                _uiState.value = FavoriteUiState(albums)
            }
        }
    }
}
