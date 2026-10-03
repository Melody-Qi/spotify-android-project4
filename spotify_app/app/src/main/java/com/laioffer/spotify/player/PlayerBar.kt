package com.laioffer.spotify.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.laioffer.spotify.R
import com.laioffer.spotify.ui.theme.TransparentBlack

/**
 * Lesson 61 - the floating player bar.
 *
 * WHERE it lives (screenshot 02): the layout is edited in activity_main.xml, NOT
 * in each Fragment. The player has to sit on top of every destination, so it
 * belongs to the host of the fragments - MainActivity. Adding it per-Fragment
 * would mean re-creating it on every navigation and losing the position.
 *
 * WHY it stays correct across pages (screenshot 05): MainActivity injects the
 * SAME PlayerViewModel the fragments use. The fragments use
 * `by activityViewModels()` (Lesson 60) precisely so all of them - and now the
 * bar - share one player instance and one state.
 *
 * Structure:
 *   PlayerBar          - public entry point. Collects the state, decides
 *                        visibility, forwards the events.
 *   PlayerBarContent   - stateless layout; receives the state + callbacks so it
 *                        can be previewed and tested without a ViewModel.
 *   SeekBar            - its own composable, because it needs local `remember`
 *                        state while the user drags the thumb.
 */
@Composable
fun PlayerBar(viewModel: PlayerViewModel) {
    // Screenshot 06: the same "StateFlow -> Compose State" line as HomeScreen /
    // PlaylistScreen (Lesson 56-57). One second of polling inside the ViewModel
    // turns into a recomposition here.
    val uiState by viewModel.uiState.collectAsState()

    // Screenshot 12: the bar should not be on screen before the user has picked
    // a song, otherwise the app opens with an empty black rectangle. The bar
    // animates in as soon as both the album and the song are known.
    val isVisible = uiState.album != null && uiState.song != null

    AnimatedVisibility(isVisible) {
        PlayerBarContent(
            uiState = uiState,
            // Screenshots 09 and 13: the Composable holds NO logic, it only
            // forwards "play/pause" and "seek" to the ViewModel, which owns the
            // ExoPlayer. The branch on isPlaying lives here because deciding
            // WHICH call to make is a UI-level choice (it mirrors the icon).
            togglePlay = {
                if (uiState.isPlaying) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            },
            // Lesson 61 "X out button" section: the × button. `viewModel::clear`
            // is a Kotlin function reference - the function itself is handed over
            // instead of wrapping it in another lambda ({ viewModel.clear() } would
            // do the same thing). See PlayerViewModel.clear() for what it does.
            clear = viewModel::clear,
            seekTo = { viewModel.seekTo(it) }
        )
    }
}

/**
 * Stateless layout: state in, events out. Screenshot 07-08 and 15.
 *
 * The whole bar is wrapped in a Box (Lesson 61, "X out button"): the × button has
 * to sit at the TOP-RIGHT of the bar, which is what Box gives us - Box stacks its
 * children, and only children of a Box can use Modifier.align(Alignment.TopEnd).
 */
@Composable
private fun PlayerBarContent(
    uiState: PlayerUiState,
    togglePlay: () -> Unit,
    clear: () -> Unit,
    seekTo: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TransparentBlack)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Screenshot 07: 60dp square cover. contentDescription = null because
                // it is decorative - the song title right next to it carries the meaning.
                AsyncImage(
                    model = uiState.album?.cover,
                    contentDescription = null,
                    modifier = Modifier
                        .width(60.dp)
                        // aspectRatio keeps the thumbnail square no matter what the
                        // album cover's intrinsic aspect is - a fixed height(60.dp)
                        // would distort a non-square image.
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillBounds
                )

                // Screenshot 08: weight(1f) makes this column eat all the remaining
                // horizontal space, which pushes the play button to the far right
                // regardless of how long the song title is. Without it, a long title
                // would squeeze the icon off the edge.
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.song?.name ?: "",
                        style = MaterialTheme.typography.body2,
                        color = Color.White
                    )
                    // `lyric` is the backend's field name for the artist line.
                    Text(
                        text = uiState.song?.lyric ?: "",
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                }

                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            togglePlay()
                        },
                    painter = painterResource(
                        id = if (uiState.isPlaying) {
                            R.drawable.ic_pause_24
                        } else {
                            R.drawable.ic_play_arrow_24
                        }
                    ),
                    tint = Color.White,
                    contentDescription = ""
                )
            }

            // Lesson 11 final version: named arguments, and the outer seekTo is
            // forwarded as-is (same type (Long) -> Unit, no wrapper lambda needed).
            SeekBar(
                currentMs = uiState.currentMs.toFloat(),
                durationValue = uiState.durationMs.toFloat(),
                seekTo = seekTo
            )
        }

        // Lesson 61 "X out button": close/clear button, floating half outside the
        // bar's top-right corner. Three modifiers, each doing one job:
        //   align(TopEnd)  - put it at the top-right of the Box (Box-only modifier)
        //   offset(10, -20)- nudge it out of the bar so it does not cover the row
        //                    (this is why the Row keeps its original end = 16.dp)
        //   size(40.dp)    - the button's own size
        // Icons.Filled.Close is a built-in Material vector (material-icons-core), so
        // unlike the play/pause icons it needs no drawable asset of ours.
        // NOTE: the handout leaves contentDescription = ""; for accessibility a
        // stringResource(R.string.clear_current_song) would be the better answer
        // (the same rule the IDE tooltip shows for this parameter).
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-20).dp)
                .size(40.dp),
            onClick = clear
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                tint = Color.White,
                contentDescription = ""
            )
        }
    }
}

/**
 * Lesson 61, screenshot 14 - the progress slider.
 *
 * @param currentMs    live position from the ViewModel's 1s poll, in ms
 * @param durationValue track length in ms (Slider needs Float, 0f..durationValue)
 * @param seekTo       called ONLY when the drag ends, not on every pixel
 */
@Composable
private fun SeekBar(currentMs: Float, durationValue: Float, seekTo: (Long) -> Unit) {
    // While the user drags, the slider must follow the THUMB, not the poll.
    // Otherwise the 1s tick would yank the thumb back to the old position
    // mid-drag and the drag would feel impossible. `seeking` is the flag that
    // suspends the sync; the value then lives in local state.
    var seekBarPosition by remember { mutableStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }
    if (!seeking) {
        seekBarPosition = currentMs
    }
    Slider(
        modifier = Modifier.height(24.dp),
        value = seekBarPosition,
        // A 0f..0f range makes the Slider throw, which is exactly what we want
        // before a song is loaded (duration is still 0).
        valueRange = 0f..durationValue,
        onValueChange = {
            // Fires continuously while dragging: only update the LOCAL position.
            seeking = true
            seekBarPosition = it
        },
        onValueChangeFinished = {
            // Fires once when the finger lifts: this is the only moment we tell
            // the player to actually move. Seeking on every onValueChange would
            // issue hundreds of seekTo calls per second.
            seekTo(seekBarPosition.toLong())
            seeking = false
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            inactiveTrackColor = Color.LightGray,
            activeTrackColor = Color.Green
        )
    )
}
