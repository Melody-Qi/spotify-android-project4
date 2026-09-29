package com.laioffer.spotify

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.laioffer.spotify.network.NetworkApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lesson 54 (screenshot 36) - the single Activity that hosts everything:
 *
 *   setContentView(R.layout.activity_main)     <- XML instead of setContent { }
 *   findViewById(nav_view)                     <- the BottomNavigationView
 *   findFragmentById(nav_host_fragment)        <- the NavHostFragment declared in the XML
 *   navController.setGraph(R.navigation.nav_graph)
 *   NavigationUI.setupWithNavController(...)   <- links tabs <-> destinations
 *
 * Lesson 55 adds `@AndroidEntryPoint` + `@Inject lateinit var api: NetworkApi`: the api is
 * no longer built by hand with NetworkModule.provideRetrofit().create(...), Hilt supplies
 * the instance that NetworkModule @Provides.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Field injection (Lesson 55): Hilt fills this in, we never construct NetworkApi here.
    @Inject
    lateinit var api: NetworkApi

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph)
        NavigationUI.setupWithNavController(navView, navController)

        // Known NavigationUI + BottomNavigationView quirk (the lesson links the same
        // StackOverflow thread): without this, re-selecting a tab can stack duplicates.
        navView.setOnItemSelectedListener {
            NavigationUI.onNavDestinationSelected(it, navController)
            navController.popBackStack(it.itemId, inclusive = false)
            true
        }

        // Lesson 54/55 sanity check: does Hilt's NetworkApi really hit our Ktor backend?
        // Watch Logcat with   package:mine tag:Network   -> [Section(sectionTitle=Top mixes, ...
        // (the same filter the lesson uses at the end of the network step)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getHomeFeed().execute().body()
                Log.d(TAG_NETWORK, response.toString())
            } catch (t: Throwable) {
                // The lesson's snippet has no try/catch; keep it so that simply forgetting
                // to start the Ktor backend does not crash the app.
                Log.w(TAG_NETWORK, "request failed: ${t.javaClass.simpleName}: ${t.message}")
            }
        }
    }

    private companion object {
        const val TAG_NETWORK = "Network"
    }
}
