package com.laioffer.spotify.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.laioffer.spotify.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lesson 54, screenshot 32 - the second tab. For now it is still the lesson's
 * placeholder screen; @AndroidEntryPoint is added so Hilt is ready when this screen
 * grows (a favourites list would get its own ViewModel + Repository the same way Home did).
 */
@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }
}
