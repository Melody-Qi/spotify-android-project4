# Spotify Android — Course Project (Project 4)

An Android music-streaming app built for a university software-engineering course
(Lessons 50–56). It mimics a simplified Spotify: a bottom-navigation shell with a
**Home** feed (album sections fetched from a backend) and a **Favorite** screen,
backed by a small Ktor server that serves a Deezer-derived chart feed.

The repo is a **monorepo** of three independent Gradle/Kotlin modules that grow
together across the lessons:

| Module | What it is | Stack |
|---|---|---|
| [`spotify_app/`](spotify_app) | The Android client. Home was rewritten in **Jetpack Compose** (Lesson 56) via a `ComposeView` bridge inside the existing `HomeFragment`; the rest of the shell stays View-based. | Kotlin, AGP 9.4.1, Jetpack (Fragment / Navigation / ViewModel / Compose), Hilt + MVVM, Retrofit, Coil |
| [`spotify_backend/`](spotify_backend) | A Ktor server exposing `/feed`, `/playlists`, `/playlist/{id}`, `/songs/*.mp3`. | Kotlin, Ktor 2.3, serialization |
| [`di_demo/`](di_demo) | A minimal Hilt/Dagger dependency-injection demo used while learning Lesson 55's DI wiring. | Kotlin, Hilt |

## Architecture (client)

```
Activity (setContentView + BottomNavigationView + nav_graph.xml)
   └── HomeFragment            @AndroidEntryPoint, hosts the UI
         ├── (Lesson 56) ComposeView → MaterialTheme { HomeScreen(viewModel) }   ← Compose
         └── (other screens)  View / XML
   └── HomeViewModel           @HiltViewModel, StateFlow<HomeUiState>
         └── HomeRepository     Retrofit (Call/Suspend) → backend on :8080
```

Key gotchas solved in-code (see each module's own README / comments):
- Coil cover images from `upload.wikimedia.org` need a **browser User-Agent** (set on
  the app's OkHttp in `MainApplication`) or they return 403.
- The emulator's global proxy must **not** intercept localhost backend calls — the
  Retrofit `OkHttpClient` is pinned to `Proxy.NO_PROXY` so the API hits the laptop
  directly while image loads still use the proxy.
- AGP 9 requires **Hilt ≥ 2.60.1** and `android.disallowKotlinSourceSets=false`.

## Run it

```bash
# 1) Start the Ktor backend (keep this terminal open)
cd spotify_backend
./gradlew installDist
./build/install/spotify_backend/bin/spotify_backend

# 2) In another terminal, build & install the app
cd spotify_app
./gradlew :app:assembleDebug
```

The Home screen shows a "could not load the feed" message until the backend is up
on port 8080 — that is the ViewModel's expected failure branch, not a bug.

> This is coursework. The backend serves static/synthetic chart data; no real
> Spotify API or credentials are involved.
