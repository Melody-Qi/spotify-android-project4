package com.laioffer.spotify.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import javax.inject.Singleton

/**
 * Lesson 54: this started life as a hand-written `object NetworkModule` that MainActivity
 * called directly (`NetworkModule.provideRetrofit().create(...)`).
 * Lesson 55 turns it into a Hilt module: Retrofit is a third-party object that also needs
 * configuration, which is exactly the case `@Module + @Provides` is for.
 *
 * `@InstallIn(SingletonComponent)` + `@Singleton` => one Retrofit, one NetworkApi for the
 * whole app. `provideApi(retrofit: Retrofit)` shows the dependency graph at work: Hilt
 * calls provideRetrofit() first and feeds the result in.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 10.0.2.2 is the emulator's alias for the host machine's localhost, where our
    // Ktor backend runs (Lesson 54 rebuild: D:\SoftwareEngineering\project4\spotify_backend).
    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                // OUR addition (not in the lesson): .proxy(Proxy.NO_PROXY) tells OkHttp to
                // talk to 10.0.2.2 DIRECTLY and ignore the emulator's global HTTP proxy.
                //
                // Why: the cover images come from upload.wikimedia.org, which needs the
                // proxy. We set that proxy with:
                //     adb shell settings put global http_proxy 10.0.2.2:7890
                // but that setting is device-wide, so our own backend call would ALSO be
                // sent to the proxy - and the proxy refuses it ("unexpected end of stream
                // on http://10.0.2.2:8080/"). This one line keeps the two apart:
                //   API (this client)  -> direct to the laptop
                //   images (coil)      -> through the proxy, because coil builds its own
                //                         OkHttp client and still follows the system proxy
                OkHttpClient.Builder()
                    .proxy(Proxy.NO_PROXY)
                    .build()
            )
            .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): NetworkApi =
        retrofit.create(NetworkApi::class.java)
}
