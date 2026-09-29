package com.laioffer.spotify.ui.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lesson 57 - the playlist page's View-system shell, the twin of HomeFragment:
 * an @AndroidEntryPoint hosting a Compose screen via ComposeView.
 *
 * Where the Album comes from - the Safe Args round trip (screenshots 25-28):
 *
 *   nav_graph.xml declares <argument android:name="album"
 *   app:argType="...datamodel.Album"/> on this destination, so the plugin generates
 *   PlaylistFragmentArgs. The `by navArgs<PlaylistFragmentArgs>()` delegate lazily
 *   reads the Bundle (only when first touched - that is the lesson's 'lazy
 *   initialization' aside) and hands us `navArgs.album`, the very object the user
 *   tapped on the home grid.
 */
@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private val navArgs by navArgs<PlaylistFragmentArgs>()

    private val viewModel: PlaylistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    PlaylistScreen(
                        playlistViewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lesson 57, screenshots 27-28: prove the album really travelled here, then
        // kick off the fetch (MVVM step 2: Set Album And Fetch New Playlist).
        Log.d(TAG, navArgs.album.toString())
        viewModel.fetchPlaylist(navArgs.album)
    }

    private companion object {
        const val TAG = "PlaylistFragment"
    }
}
