package com.laioffer.spotify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laioffer.spotify.datamodel.Section
import com.laioffer.spotify.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lesson 55, screenshot 24 / teacher's final version - one snapshot of what the screen
 * should show. "UI = f(state)": the Fragment decides nothing, it only renders this.
 *
 *   data class HomeUiState(val feed: List<Section>, val isLoading: Boolean)
 *
 * feed       = the sections that came back from /feed
 * isLoading  = is a request still running? (the lesson uses it in the Fragment to avoid
 *              firing the request twice, e.g. after a screen rotation)
 */
data class HomeUiState(
    val feed: List<Section>,
    val isLoading: Boolean
)

/**
 * Lesson 55, screenshots 25-27 - the ViewModel: the owner of the state.
 *
 * Teacher's final version:
 *
 *   @HiltViewModel
 *   class HomeViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {
 *       private val _uiState = MutableStateFlow(HomeUiState(feed = emptyList(), isLoading = true))
 *       val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
 *
 *       fun fetchHomeScreen() {
 *           viewModelScope.launch {
 *               val sections = repository.getHomeSections()
 *               _uiState.value = HomeUiState(feed = sections, isLoading = false)
 *               Log.d("HomeViewModel", _uiState.value.toString())
 *           }
 *       }
 *   }
 *
 * The identifier `uiState` stays the lesson's name, but it is backed by a StateFlow:
 * a plain `var uiState = ...` cannot be *observed* - assigning a new value would change
 * nothing on screen, because nobody is told. A StateFlow is a value + a notification
 * channel, which is what makes the Fragment redraw.
 *
 * viewModelScope is cancelled when this screen goes away, so a slow request can never
 * leak past the screen's lifetime - this replaces the GlobalScope of the Lesson 54 demo.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {

    // private MUTABLE -> only this class may change it
    private val _uiState = MutableStateFlow(HomeUiState(feed = emptyList(), isLoading = true))
    // public READ-ONLY -> the Fragment can only observe it
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Event sent by the View (Lesson 55: only fired while isLoading). */
    fun fetchHomeScreen() {
        viewModelScope.launch {
            try {
                val sections = repository.getHomeSections()
                _uiState.value = HomeUiState(feed = sections, isLoading = false)
                // Lesson 55: the fastest way to see "did the state really change?" without
                // touching the UI - filter Logcat by tag HomeViewModel.
                Log.d(TAG, _uiState.value.toString())
            } catch (t: Throwable) {
                // OUR addition, not in the lesson: without it a failed request (backend not
                // running, wrong port, malformed JSON) kills the coroutine and crashes the
                // app. The lesson's demo always has its backend up, so it never needed one.
                Log.w(TAG, "fetchHomeScreen failed: ${t.javaClass.simpleName}: ${t.message}")
                _uiState.value = HomeUiState(feed = emptyList(), isLoading = false)
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
