plugins {
    id("com.android.application")

    // Lesson 55 - Hilt. Both ids are declared (with versions) in the root build file.
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")

    // Lesson 56 - Compose. The compiler plugin version is fixed to the Kotlin that
    // AGP 9.4.1 ships (2.2.10); do not bump it independently.
    id("org.jetbrains.kotlin.plugin.compose")

    // Lesson 57 - Safe Args (Java variant, exactly the id the lesson uses).
    // Generates HomeFragmentDirections / PlaylistFragmentArgs from nav_graph.xml,
    // so navigating with an Album argument is checked at compile time.
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.laioffer.spotify"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.laioffer.spotify"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Kotlin support is built into AGP 9 - no kotlin-android plugin, and JVM target is
    // kept in sync with the Java level automatically.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Lesson 56 - Compose is enabled for the Home screen (hosted inside HomeFragment
    // via ComposeView). The Compose compiler version is supplied by the
    // org.jetbrains.kotlin.plugin.compose plugin above; no kotlinCompilerExtensionVersion.
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ---------- Lesson 53: "android xml library" (pre-installed in the lesson template)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.core:core-ktx:1.16.0")

    // ---------- Lesson 54: Bottom Navigation (the versions the lesson gives)
    //   def nav_version = "2.5.3"
    //   implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
    //   implementation "androidx.navigation:navigation-ui-ktx:$nav_version"
    // Lesson 57: bumped 2.5.3 -> 2.10.2 to match the Safe Args plugin (the 2.5.3
    // plugin cannot run on AGP 9); 2.10.2 keeps the same NavigationUI/NavHostFragment APIs.
    val navVersion = "2.10.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ---------- Lesson 54: Network with Retrofit (lesson uses 2.9.0)
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    // ---------- Lesson 56: Compose (Material 2, matching the lesson's
    // MaterialTheme(colors = darkColors())).
    val composeBom = "androidx.compose:compose-bom:2026.09.00"
    implementation(platform(composeBom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    // Lesson 61 "X out button": Icons.Filled.Close, the built-in × vector used for the
    // clear button of the floating player bar (no drawable asset of ours needed).
    // Version comes from the Compose BOM above.
    implementation("androidx.compose.material:material-icons-core")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---------- Lesson 56: images inside Compose. coil-compose 2.2.2 brings coil core
    // (used by MainApplication's Coil.setImageLoader global UA interceptor) transitively,
    // so the plain io.coil-kt:coil line from Lesson 53 is no longer needed on its own.
    implementation("io.coil-kt:coil-compose:2.2.2")

    // ---------- Lesson 55: Hilt
    //   hilt-android          : the runtime annotations (@HiltAndroidApp, @Inject, @Module ...)
    //   hilt-android-compiler : build time only -> ksp(...), never shipped in the APK
    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")

    // ---------- Lesson 55: MVVM (lifecycle)
    //   lifecycle-viewmodel-ktx : ViewModel + viewModelScope
    //   lifecycle-runtime-ktx   : lifecycleScope / repeatOnLifecycle (kept for parity;
    //                             HomeScreen observes uiState via collectAsState instead)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    // ---------- Lesson 58: Room (local database)
    //   Lesson pins 2.4.3 with kapt. Two adaptations for this project (same story as
    //   Hilt/Safe Args):
    //   1. kapt -> ksp : kapt is legacy, the project already runs KSP for Hilt and
    //      Room 2.7+ generates its code through KSP2 (Kotlin 2.2.10 compatible).
    //   2. 2.4.3 -> 2.8.5 : Room 2.4.x predates KSP2 and would not compile on this
    //      Kotlin. 2.8.5 is the current stable with identical @Entity/@Dao/@Database APIs.
    //   room-runtime : the annotations (@Entity/@Dao/@Database) + Room class
    //   room-ktx     : coroutine support (suspend DAO functions, Flow support)
    //   room-compiler: build time only -> ksp(...), generates the DAO/DB implementations
    val roomVersion = "2.8.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // ---------- Lesson 60: ExoPlayer (media playback)
    //   DIFFERENCE FROM THE HANDOUT (important): the lesson pins the legacy
    //   "com.google.android.exoplayer2:exoplayer-core:2.18.2", but that artifact only
    //   ever lived on jcenter/bintray, which Google shut down - it is NOT on
    //   dl.google.com nor Maven Central anymore (verified 404 on both, 2026-10-01).
    //   androidx.media3 is ExoPlayer's official successor (same team, same Player /
    //   MediaItem / Player.Listener APIs, just a new package name), so we pin that.
    val media3Version = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    // OkHttp-backed DataSource so PlayerModule can bypass the emulator's system proxy
    // for mp3 loading (see PlayerModule for the full explanation).
    implementation("androidx.media3:media3-datasource-okhttp:$media3Version")

    testImplementation("junit:junit:4.13.2")
}
