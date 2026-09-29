package com.laioffer.spotify.ui.playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laioffer.spotify.R
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.datamodel.Song

/**
 * Lesson 57 - the playlist page, split into three stateless components exactly as the
 * design screenshot (screenshot 47) marks them:
 *
 *   PlaylistScreen          - stateful entry: collects PlaylistUiState from the ViewModel
 *   PlaylistScreenContent   - stateless Column: Cover + PlaylistHeader + PlaylistContent
 *   Cover                   - heart icon + vinyl record (background png + round cover) + description
 *   PlaylistHeader          - album name (big bold) + "artists • year" line
 *   PlaylistContent         - LazyColumn of Song rows (+ a 40dp tail spacer)
 *   Song                    - one row: name/artist on the left, length on the right
 *
 * Same "UI = f(state)" contract as HomeScreen: everything below PlaylistScreen is
 * stateless and only describes what to draw for the given state.
 */

@Composable
fun PlaylistScreen(playlistViewModel: PlaylistViewModel) {
    // Lesson 57, screenshot 48: StateFlow -> Compose State, identical to HomeScreen.
    val playlistUiState by playlistViewModel.uiState.collectAsState()

    PlaylistScreenContent(
        playlistUiState = playlistUiState
    )
}

@Composable
private fun PlaylistScreenContent(
    playlistUiState: PlaylistUiState
) {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        Cover(
            album = playlistUiState.album,
            isFavorite = playlistUiState.isFavorite
        )

        // Lesson 57, screenshot 58: the middle component (screenshot 47's yellow box).
        PlaylistHeader(album = playlistUiState.album)

        // Lesson 57, screenshots 63-66: the bottom component (song list).
        PlaylistContent(playlist = playlistUiState.playlist)
    }
}

@Composable
private fun Cover(
    album: Album,
    isFavorite: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lesson 57, screenshots 50-52: the heart in the top-right corner. The icon
            // AND its tint both switch on isFavorite - green heart vs gray outline.
            Icon(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd),
                painter = painterResource(
                    id = if (isFavorite) {
                        R.drawable.ic_favorite_24
                    } else {
                        R.drawable.ic_unfavorite_24
                    }
                ),
                tint = if (isFavorite) {
                    Color.Green
                } else {
                    Color.Gray
                },
                contentDescription = ""
            )

            // Lesson 57, screenshots 53-55: the vinyl record. A square box (0.6 of the
            // width, aspectRatio 1:1) centered in the row; the black vinyl png fills it,
            // and the real album cover is clipped to a circle on top of it - the cover
            // becomes the record's label. (.clip needs androidx.compose.ui.draw.clip.)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1.0f)
                    .align(Alignment.Center)
            ) {
                // Vinyl background
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.vinyl_background),
                    contentDescription = null
                )

                // Round album cover on top of the vinyl. Same UA-header fix as the home
                // covers (Wikimedia 403 without a browser-like User-Agent).
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.cover)
                        .addHeader("User-Agent", "Mozilla/5.0 (Android Emulator) Chrome/120.0")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1.0f)
                        .align(Alignment.Center)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        // Lesson 57, screenshots 56-57: the album description under the vinyl - small,
        // gray, caption style.
        Text(
            text = album.description,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.caption,
            color = Color.Gray,
        )
    }
}

@Composable
private fun PlaylistHeader(album: Album) {
    Column {
        Text(
            text = album.name,
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp),
            color = Color.White
        )

        // Lesson 57, screenshots 59-62: "artists • year", built from the album_info
        // string resource (%1$s • %2$s) so the layout stays translatable.
        Text(
            text = stringResource(id = R.string.album_info, album.artists, album.year),
            style = MaterialTheme.typography.body2,
            color = Color.LightGray,
        )
    }
}

@Composable
private fun PlaylistContent(
    playlist: List<Song>
) {
    // rememberLazyListState: survives recomposition, remembers the scroll position.
    val state = rememberLazyListState()
    LazyColumn(state = state) {
        // Lesson 57, screenshot 66: items(playlist) - the List<Song> overload from
        // androidx.compose.foundation.lazy.items. Importing the WRONG items() (the
        // non-extension one) gives "Type mismatch: Required Int, Found List<Song>"
        // (screenshot 67), because that overload expects a count.
        items(playlist) { song ->
            Song(song, false)
        }

        // A tail spacer so the last row can scroll clear of the bottom navigation bar.
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Song(song: Song, isPlaying: Boolean) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // weight(1f): the name/artist column takes all leftover width, pushing the
        // length text to the far right.
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = song.name,
                style = MaterialTheme.typography.body2,
                color = if (isPlaying) {
                    Color.Green
                } else {
                    Color.White
                },
            )

            Text(
                text = song.lyric,
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
            )
        }
        Text(
            text = song.length,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.body2,
            color = Color.LightGray,
        )
    }
}

/**
 * Lesson 57 - stateless preview: the stateless content composable with a fake album,
 * no ViewModel / backend needed (same trick as HomeScreenPreview).
 */
@Preview(showBackground = true, widthDp = 412, heightDp = 732)
@Composable
fun PlaylistScreenPreview() {
    MaterialTheme(colors = darkColors()) {
        PlaylistScreenContent(
            playlistUiState = PlaylistUiState(
                album = Album(
                    id = 1,
                    name = "Hexagonal",
                    year = "2008",
                    cover = "",
                    artists = "Lesssang",
                    description = "Leessang was a South Korean hip hop duo."
                ),
                isFavorite = false,
                playlist = listOf(
                    Song("HEXAGONAL (Intro)", "Bizzy", "", "3:47"),
                    Song("Carousel", "Gary", "", "4:13")
                )
            )
        )
    }
}
