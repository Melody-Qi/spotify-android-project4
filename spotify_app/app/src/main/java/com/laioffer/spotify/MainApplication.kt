package com.laioffer.spotify

import android.app.Application
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Lesson 55, rule 1: exactly one @HiltAndroidApp class per app. It triggers Hilt's code
 * generation and owns the SingletonComponent - the parent every other component can read
 * from. It must also be registered in AndroidManifest (android:name=".MainApplication").
 */
@HiltAndroidApp
class MainApplication : Application() {

    /**
     * OUR addition (not in the lesson): give coil an OkHttpClient that sends a normal
     * browser User-Agent.
     *
     * Why: the covers in our /feed point at upload.wikimedia.org, and Wikimedia rejects
     * requests without a recognisable User-Agent:
     *     no User-Agent / "okhttp/4.x"  -> HTTP 403 Forbidden
     *     "Dalvik/2.1.0 ..." / a browser UA -> 200 OK
     * Coil's default client is fine for most CDNs, but not for this one, so images came
     * back empty while the text (which comes from our own backend) displayed normally.
     *
     * This client still follows the emulator's system proxy, which is what lets the
     * emulator reach Wikimedia at all.
     */
    override fun onCreate() {
        super.onCreate()

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient {
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val withUserAgent: Request = chain.request()
                                .newBuilder()
                                .header(
                                    "User-Agent",
                                    "Mozilla/5.0 (Linux; Android 16; Pixel 7) " +
                                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                        "Chrome/140.0.0.0 Mobile Safari/537.36"
                                )
                                .build()
                            chain.proceed(withUserAgent)
                        }
                        .build()
                }
                .build()
        )
    }
}
