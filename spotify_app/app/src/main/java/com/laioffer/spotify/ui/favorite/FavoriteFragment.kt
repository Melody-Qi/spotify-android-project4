package com.laioffer.spotify.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.laioffer.spotify.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lesson 59 (Android 9) - the Favorite tab, now migrated to Compose the same way Home
 * (Lesson 56) and Playlist (Lesson 57) were.
 *
 * It used to inflate fragment_favorite.xml (a "2" placeholder from Lesson 54); that XML
 * is archived under app/src/main/_archive and is no longer referenced.
 *
 * No fetch call here: FavoriteViewModel loads the list in its init{} from Room's Flow
 * (Lesson 58), so the screen just observes. The onTap lambda forwards the tapped album
 * to the Playlist page through the Safe Args action already declared in nav_graph.xml
 * (action_favoriteFragment_to_playlistFragment) - the same navigation pattern Home uses.
 */
@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private val viewModel: FavoriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    FavoriteScreen(viewModel, onTap = { album ->
                        val direction =
                            FavoriteFragmentDirections.actionFavoriteFragmentToPlaylistFragment(album)
                        findNavController().navigate(direction)
                    })
                }
            }
        }
    }
}
