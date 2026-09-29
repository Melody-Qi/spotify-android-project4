// Root build file: declare plugin versions once, apply them in :app
//
// NOTE (AGP 9.0+): Kotlin support is built in - the old
// "org.jetbrains.kotlin.android" plugin must NOT be applied any more.
// AGP 9.4.1 ships Kotlin Gradle Plugin 2.2.10, so anything version-tied to Kotlin
// (KSP) has to match that number.
//
// This project follows the LESSON's View system (Activity + XML layouts +
// Fragment + BottomNavigationView), so there is NO compose plugin here.
plugins {
    id("com.android.application") version "9.4.1" apply false

    // Lesson 55 - Hilt (dependency injection).
    //   hilt-android-gradle-plugin : the Gradle plugin (byte-code rewrite of @AndroidEntryPoint)
    //   KSP                        : the annotation processor runner (replaces kapt, faster).
    //                                Version must match Kotlin: 2.2.10-2.0.2 <-> Kotlin 2.2.10.
    // NOTE: Hilt 2.57.x fails on AGP 9 with "Android BaseExtension not found" - its
    // Gradle plugin still calls the AGP 8 API removed in AGP 9. 2.60.1 is the first
    // release that works with AGP 9.
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false

    // Lesson 56 - Compose. The compiler plugin version MUST match the Kotlin that AGP
    // 9.4.1 ships (2.2.10); the compose compiler is aligned automatically from it.
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
