package com.laioffer.spotify.ui.favorite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laioffer.spotify.R
import com.laioffer.spotify.datamodel.Album

/**
 * Lesson 59 (Android 9) - the Favorites screen, written in Jetpack Compose. It is the
 * same incremental-migration step Home took in Lesson 56 and Playlist took in Lesson 57:
 * the Fragment (FavoriteFragment) stays the View-system shell; the rendering moves here.
 *
 * Structure (mirrors HomeScreen / PlaylistScreen):
 *   FavoriteScreen        - stateful entry: collects FavoriteUiState from the ViewModel
 *   FavoriteScreenContent - stateless scroll container: header + one row per saved album
 *   FavoriteAlbumRow      - one album (rounded cover + name + "artists • year"), tappable
 *
 * State comes from Room via FavoriteViewModel (Lesson 58): every INSERT/DELETE on the
 * Album table re-emits the full list, so this screen keeps itself up to date for free -
 * there is no fetch call to fire (contrast HomeFragment.fetchHomeScreen()).
 *
 * The tap is forwarded UP (album -> onTap -> FavoriteFragment -> navigate), exactly the
 * same "event goes up, state goes down" contract the home covers use in Lesson 57.
 */
@Composable
fun FavoriteScreen(viewModel: FavoriteViewModel, onTap: (Album) -> Unit) {
    // StateFlow -> Compose State; every new list the ViewModel emits re-composes the tree.
    val uiState by viewModel.uiState.collectAsState()
    FavoriteScreenContent(albums = uiState.albums, onTap = onTap)
}

@Composable
private fun FavoriteScreenContent(albums: List<Album>, onTap: (Album) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        // The "Favorite" title, same h4 + white as HomeScreen's header (Lesson 56).
        item {
            Text(
                text = stringResource(id = R.string.menu_favorite),
                style = MaterialTheme.typography.h4,
                color = Color.White
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // One FavoriteAlbumRow per saved album; the whole list is the Room Flow's output.
        items(albums) { album ->
            FavoriteAlbumRow(album = album, onTap = onTap)
        }
    }
}

@Composable
private fun FavoriteAlbumRow(album: Album, onTap: (Album) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap(album) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lesson 59 requirement: the cover has an 8dp rounded corner.
        // A plain AsyncImage(model = album.cover) would also work because MainApplication
        // installs a global Coil UA interceptor, but we re-add the per-request User-Agent
        // like HomeScreen does - belt and braces so Wikimedia covers never 403.
        Box(modifier = Modifier.size(60.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.cover)
                    .addHeader("User-Agent", "Mozilla/5.0 (Android Emulator) Chrome/120.0")
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillBounds
            )
        }

        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            // "artists • year", built from album_info so it stays translatable - same
            // string the PlaylistHeader uses.
            Text(
                text = stringResource(id = R.string.album_info, album.artists, album.year),
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
    }
}
