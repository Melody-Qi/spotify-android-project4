package com.laioffer.spotify.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laioffer.spotify.R
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Section

/**
 * Lesson 56 - Home screen rewritten in Jetpack Compose.
 *
 * The View layer (HomeFragment) is unchanged in role: it still owns the fragment
 * lifecycle and triggers the first load. What moved here is the *rendering* - the
 * whole screen is now a tree of composables instead of an XML layout + RecyclerView.
 *
 * Structure (matches the lesson's design screenshot):
 *   HomeScreen            - observes the ViewModel's StateFlow, hands state down
 *   HomeScreenContent     - the scroll container; switches on loading vs. data
 *   HomeScreenHeader      - the "Home" title
 *   AlbumSection          - one horizontal row (title + LazyRow of covers)
 *   AlbumCover            - a single album card (cover image + name + artists)
 *   LoadingSection        - the "Screen is loading..." placeholder
 *
 * "UI = f(state)": HomeScreenContent takes the state as a parameter and describes
 * *what to draw for each state*. It holds no state of its own (stateless / hoisted),
 * which keeps it reusable and testable - the Compose version of the MVVM one-way
 * data flow (state down, events up).
 *
 * Lesson 57 adds one thread through the whole tree: `onTap: (Album) -> Unit`.
 * The event flows UP (album click -> ... -> HomeFragment -> navigate), the state
 * flows DOWN (uiState) - together they form the complete one-way data flow.
 */

@Composable
fun HomeScreen(viewModel: HomeViewModel, onTap: (Album) -> Unit) {
    // Lesson 56, screenshot 21: the single most important line. StateFlow -> Compose
    // State, so every new value the ViewModel emits re-composes this tree.
    // (The View-system equivalent was repeatOnLifecycle(STARTED) { uiState.collect {} }.)
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(uiState = uiState, onTap = onTap)
}

@Composable
fun HomeScreenContent(uiState: HomeUiState, onTap: (Album) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { HomeScreenHeader() }
        when {
            uiState.isLoading -> {
                // Lesson 56, screenshots 24-26: loading is just another item in the list.
                item { LoadingSection(text = stringResource(id = R.string.screen_loading)) }
            }
            else -> {
                // Lesson 56, screenshot 31: one AlbumSection per feed section.
                // Lesson 57: forward the tap event to every section unchanged.
                items(uiState.feed) { section -> AlbumSection(section = section, onTap = onTap) }
            }
        }
    }
}

@Composable
fun HomeScreenHeader() {
    Column {
        Text(
            text = stringResource(id = R.string.menu_home),
            style = MaterialTheme.typography.h4,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AlbumSection(section: Section, onTap: (Album) -> Unit) {
    Column {
        Text(
            text = section.sectionTitle,
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Lesson 56, screenshot 33: items() needs the androidx.compose.foundation.lazy.items import.
            // Lesson 57, screenshot 03: hand the callback to each cover.
            items(section.albums) { album -> AlbumCover(album = album, onTap = onTap) }
        }
    }
}

@Composable
fun AlbumCover(album: Album, onTap: (Album) -> Unit) {
    // Lesson 57, screenshot 03: clickable on the Column makes the whole card tappable;
    // onTap(album) hands the *specific* album of this card back to the caller.
    // In Kotlin, `onTap: (Album) -> Unit` is just a function value - equivalent to
    // `fun onTap(album: Album) {}` - and `Modifier.clickable { onTap(album) }` runs it
    // whenever the Column is clicked.
    Column(modifier = Modifier.clickable { onTap(album) }) {
        Box(modifier = Modifier.size(160.dp)) {
            // Lesson 56, screenshot 36: same Wikimedia 403 fix we already applied globally
            // in MainApplication - a browser-like User-Agent is required, otherwise the
            // cover request is rejected. (coil-compose uses the same Coil singleton, so
            // the global interceptor already covers this; the per-request header is a
            // belt-and-braces match to the lesson's code.)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.cover)
                    .addHeader("User-Agent", "Mozilla/5.0 (Android Emulator) Chrome/120.0")
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            // Album name overlaid on the bottom-left of the cover, like the lesson design.
            Text(
                text = album.name,
                style = MaterialTheme.typography.body2,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
        Text(
            text = album.artists,
            style = MaterialTheme.typography.body2,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun LoadingSection(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = text, style = MaterialTheme.typography.body2, color = Color.White)
    }
}

/**
 * Lesson 56, screenshot 20 - a preview so the IDE can render the screen without a
 * backend. It calls the stateless HomeScreenContent directly (no ViewModel needed),
 * which is exactly why keeping the composables hoisted pays off.
 */
@Preview(showBackground = true, widthDp = 412, heightDp = 732)
@Composable
fun HomeScreenPreview() {
    MaterialTheme(colors = darkColors()) {
        HomeScreenContent(
            uiState = HomeUiState(
                feed = listOf(
                    Section(
                        "Top mixes",
                        listOf(Album(1, "Hexagonal", "2023", "", "Jay Chou", ""))
                    )
                ),
                isLoading = false
            ),
            // Lesson 57: previews have nowhere to navigate, so a no-op lambda is enough.
            onTap = {}
        )
    }
}
