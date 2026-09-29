package com.laioffer.spotify.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
                    HomeScreen(viewModel)
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
}
