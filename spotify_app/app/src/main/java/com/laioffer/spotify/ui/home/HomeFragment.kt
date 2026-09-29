package com.laioffer.spotify.ui.home

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
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lesson 56 - the View-system shell that hosts the Compose Home screen.
 *
 * The Fragment keeps its role from Lesson 55: an @AndroidEntryPoint that Hilt can
 * inject into, a single HomeViewModel instance (by viewModels()), and the place that
 * fires the first load. What changed is *how the UI is produced*:
 *
 *   - onCreateView no longer inflates fragment_home.xml. It returns a ComposeView and
 *     calls setContent { MaterialTheme { HomeScreen(viewModel) } }. ComposeView is the
 *     bridge that lets a Compose world live inside a Fragment (View system) - this is
 *     "incremental migration", not a full rewrite of the app.
 *   - onViewCreated no longer observes the StateFlow by hand; HomeScreen reads it via
 *     collectAsState() and re-composes itself.
 *
 * The old XML layout and RecyclerView adapter are archived under _archive/home_lesson54_view/.
 *
 * Lesson 57, screenshots 25-26: the onTap lambda now really navigates. The flow:
 *
 *   AlbumCover click -> onTap(album) -> ... -> this lambda ->
 *   HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(album)  (Safe Args)
 *   findNavController().navigate(direction)
 *
 * findNavController() works here because the whole call sits inside the ComposeView's
 * `apply {}` block - the receiver IS a View, and androidx.navigation.findNavController(View)
 * walks up the hierarchy until it finds the NavHostFragment.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    HomeScreen(viewModel, onTap = { album ->
                        // Lesson 57 step 8: Safe Args - the generated Directions class
                        // (named after the SOURCE fragment) refuses to navigate without
                        // the mandatory Album argument.
                        val direction =
                            HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(album)
                        findNavController().navigate(direction)
                        Log.d(TAG, "We tapped ${album.name}")
                    })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lesson 55, screenshot 29: only fire the request while still in the loading
        // state, so a screen rotation does not refetch.
        if (viewModel.uiState.value.isLoading) {
            viewModel.fetchHomeScreen()
        }
    }

    private companion object {
        const val TAG = "HomeFragment"
    }
}
