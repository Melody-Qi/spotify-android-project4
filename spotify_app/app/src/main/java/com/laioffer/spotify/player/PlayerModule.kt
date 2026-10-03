package com.laioffer.spotify.player

import android.content.Context
// androidx.media3 = the official successor of the legacy com.google.android.exoplayer2
// (same ExoPlayer class name, new package - see build.gradle.kts for why we migrated).
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.net.Proxy
import javax.inject.Singleton

/**
 * Lesson 60, screenshots 06-07 - make the ExoPlayer injectable.
 *
 * Hilt can't construct an ExoPlayer by itself (it has no @Inject constructor), so
 * this module teaches it how: whenever someone asks for an ExoPlayer, build one with
 * the application context and hand back the SAME instance every time (@Singleton).
 *
 * Why singleton: one music player for the whole app. Home -> Playlist navigation
 * recreates fragments, but the music must not restart - the player survives because
 * it lives in the SingletonComponent (as long as the process lives).
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providesPlayer(@ApplicationContext context: Context): ExoPlayer {
        // LOCAL DEVIATION from the handout (2026-10-03): the emulator on this machine
        // has a GLOBAL http proxy (10.0.2.2:7890 -> Clash on the host) so that album
        // covers from blocked sites (upload.wikimedia.org) can load. But the mp3 URLs
        // point at 10.0.2.2:8080 (the host's Ktor backend) - routed through Clash the
        // request dies, because on the HOST 10.0.2.2 means nothing, and Clash replies
        // 502 after a long timeout ("Source error" in Logcat, play button appears dead).
        // The emulator's proxy exclusion list is not honored by media3's default
        // HttpURLConnection stack, so we give ExoPlayer an OkHttp data source that
        // bypasses the proxy entirely. Covers still load through the proxy via Coil;
        // only the media path goes direct.
        val directClient = OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .build()
        val mediaSourceFactory = DefaultMediaSourceFactory(
            OkHttpDataSource.Factory(directClient)
        )
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }
}
