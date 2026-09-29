package com.laioffer.spotify

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.laioffer.spotify.database.DatabaseDao
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.network.NetworkApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
 *
 * Lesson 58 (screenshots 18-19) adds `@Inject lateinit var databaseDao: DatabaseDao` +
 * a throwaway insertion, so the Database Inspector has something to show. Note the
 * comment the lesson repeats: "remember it runs everytime you start the app" - with
 * onConflict = REPLACE re-running it just overwrites the same row (id = 1).
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Field injection (Lesson 55): Hilt fills this in, we never construct NetworkApi here.
    @Inject
    lateinit var api: NetworkApi

    // Lesson 58: same trick, second dependency - Hilt walks DatabaseModule and hands us
    // the DatabaseDao of the "spotify_db" database.
    @Inject
    lateinit var databaseDao: DatabaseDao

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

        // Lesson 58, screenshot 18 - test the database by inserting one album on every
        // start ("remember it runs everytime you start the app"). Room writes must not
        // run on main, hence withContext(Dispatchers.IO) inside lifecycleScope (the
        // lesson's exact structure). Verify: App Inspection > Database Inspector >
        // spotify_db > Album should show the Hexagonal row (screenshot 19).
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val album = Album(
                    id = 1,
                    name = "Hexagonal",
                    year = "2008",
                    cover = "https://upload.wikimedia.org/wikipedia/en/6/6d/Leessang-Hexagonal_%28cover%29.jpg",
                    artists = "Lesssang",
                    description = "Leessang (Korean: 리쌍) was a South Korean hip hop duo, " +
                        "composed of Kang Hee-gun (Gary or Garie) and Gil Seong-joon (Gil)"
                )
                databaseDao.favoriteAlbum(album)
            }
        }
    }

    private companion object {
        const val TAG_NETWORK = "Network"
    }
}
