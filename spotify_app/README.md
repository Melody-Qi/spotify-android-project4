# Spotify — 老师示范工程「View 体系」版（Lesson 52–55）

> ⚠️ **本笔记写于 Lesson 55（纯 View 体系）。Lesson 56 已把 Home 页用 Jetpack Compose
> 重写**——`ui/home/HomeFragment.kt` 现在返回 `ComposeView`，内部 `MaterialTheme { HomeScreen(viewModel) }`
> （`ui/home/HomeScreen.kt`），通过网络层 `StateFlow` 驱动；其余壳（BottomNavigationView /
> nav_graph / Favorite）仍是 View。工程当前**同时含 View 与 Compose**，并非「完全没有 @Composable」。

> 2026-09-27 重写。**这一版完全按讲义的 View 体系**：Activity + XML 布局 + Fragment +
> `BottomNavigationView` + `nav_graph.xml` + Hilt/MVVM，**不再有 `@Composable`、没有
> `setContent`、没有 `navigation-compose`**。
> 之前的 Compose 版已封存：`D:\SoftwareEngineering\project4\_archive\spotify_app_compose_20260927`。

---

## 1. 工程结构（与讲义截图 22 的老师工程一致）

```
app/src/main/
├── AndroidManifest.xml          INTERNET 权限 + networkSecurityConfig + .MainApplication
├── java/com/laioffer/spotify/
│   ├── MainApplication.kt       @HiltAndroidApp              (L55 规则1)
│   ├── MainActivity.kt          setContentView + 导航五件套 + @Inject NetworkApi (L54/L55)
│   ├── datamodel/
│   │   ├── Album.kt             data class + @SerializedName("album") -> name + Serializable + empty()
│   │   └── Section.kt           @SerializedName("section_title") -> sectionTitle + Serializable
│   ├── network/
│   │   ├── NetworkApi.kt        @GET("feed") fun getHomeFeed(): Call<List<Section>>
│   │   └── NetworkModule.kt     @Module @InstallIn(SingletonComponent) @Provides
│   ├── repository/
│   │   └── HomeRepository.kt    @Inject constructor + withContext(Dispatchers.IO)
│   └── ui/
│       ├── home/
│       │   ├── HomeFragment.kt  @AndroidEntryPoint + by viewModels() + repeatOnLifecycle
│       │   ├── HomeViewModel.kt @HiltViewModel + StateFlow<HomeUiState>
│       │   └── AlbumAdapter.kt  RecyclerView.ListAdapter + DiffUtil + coil.load()
│       └── favorite/FavoriteFragment.kt
└── res/
    ├── layout/     activity_main.xml / fragment_home.xml / fragment_favorite.xml / item_album.xml
    ├── navigation/ nav_graph.xml
    ├── menu/       bottom_nav_menu.xml
    ├── color/      bottom_nav_color.xml（选中白 / 未选灰）
    ├── values/     colors.xml / strings.xml / themes.xml
    ├── drawable/   老师素材包 6 个文件
    └── xml/        network_security_config.xml（放行 10.0.2.2 明文）
```

## 2. 讲义 → 本工程逐条对应

| 讲义步骤 | 本工程 |
|---|---|
| L53 建工程（Empty Compose Activity → 后改 XML） | 直接 View 体系：`MainActivity : AppCompatActivity` + `setContentView(R.layout.activity_main)` |
| L54 依赖 `nav_version = "2.5.3"`（fragment-ktx / ui-ktx） | `app/build.gradle.kts` 原样使用 **2.5.3** |
| L54 `activity_main.xml` 三块（FrameLayout+FragmentContainerView / 1dp 分隔线 / BottomNavigationView） | 完全一致，id 沿用 `nav_host_fragment`、`nav_view` |
| L54 `bottom_nav_menu.xml`（item id 必须 = nav_graph 的 destination id） | `homeFragment` / `favoriteFragment` 两处同名 |
| L54 `bottom_nav_color.xml`（selector：checked→white） | 一致 |
| L54 `ui/home/HomeFragment`、`ui/favorite/FavoriteFragment` | 一致（包名 `ui.home` / `ui.favorite`） |
| L54 `nav_graph.xml`（startDestination = homeFragment） | 一致 |
| L54 MainActivity 接线五件套 + `setOnItemSelectedListener` 修正 | 一致 |
| L54 retrofit **2.9.0** + converter-gson | 一致 |
| L54 `datamodel/Album.kt`（`@SerializedName("album")`）、`Section.kt` | 一致（含 `Album.empty()`） |
| L54 `network/NetworkApi.kt`、`NetworkModule.kt`（BASE_URL `http://10.0.2.2:8080/`） | NetworkModule 已是 L55 的 Hilt 版 `@Provides @Singleton` |
| L54 CLEARTEXT 报错 → `res/xml/network_security_config.xml` | 一致（只放行 10.0.2.2） |
| L55 `MainApplication` + Manifest 注册 + `@AndroidEntryPoint` | 一致 |
| L55 `repository/HomeRepository.kt`（`@Inject` + suspend + IO） | 一致，方法名跟讲义最终版叫 **`getHomeSections()`**（Retrofit 那层仍叫 `getHomeFeed()`） |
| L55 `HomeUiState(feed, isLoading)` + `_uiState` 私有可变 / `uiState` 只读 | 字段一致；用 StateFlow 承载（见下方差异 1） |
| L55 `fetchHomeScreen()` 事件 + `viewModelScope` | 事件名一致；ViewModel 内用 `viewModelScope` |
| L55 Fragment 里 `by viewModels()`、`onViewCreated` 里 isLoading 才请求 | 一致 |
| L55 依赖 `androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1` | 未引入，原因见下方差异 4 |

**五处与讲义字面不同，每一处都有理由**（都能在代码注释里就地看到注释）：

1. `HomeViewModel` 的 `uiState`：讲义草稿写 `var uiState = HomeUiState(...)`——赋值不会被 UI 感知（没人通知界面），且 `getHomeFeed()` 是 `suspend`，必须在协程里调。所以**保留名字 `uiState` 和两个字段**，底层换成 `StateFlow` + `viewModelScope`（讲义自己也在这块打了 `// todo`）。
2. `MainActivity` 的网络自检：照讲义用 `GlobalScope.launch(Dispatchers.IO)`（这是 Activity 里的一次性演示调用，不做就看不到 `tag:Network` 那条日志），但**多包一层 try/catch**——讲义没有，而后端忘记启动时会直接崩。
3. `HomeFragment` 渲染：讲义后续才接 UI；本工程用 `RecyclerView` + `ListAdapter`（纯 View 体系，不用 Compose）。
4. `lifecycle-viewmodel-compose`：这个包只提供 Compose 里用的 `viewModel()`。我们是 XML + Fragment，等价物是 `lifecycle-viewmodel-ktx` + fragment-ktx 的 `by viewModels()`；引入 compose 版只会白白把 compose.runtime 拖进工程（注释行已留在 `app/build.gradle.kts`，将来改 Compose 时解开即可）。
5. `material/appcompat/constraintlayout` 用较新的稳定版（讲义的 1.7.0/1.5.1/2.1.4 与 compileSdk 37 组合有风险）；navigation **2.5.3** 与 retrofit **2.9.0** 保持讲义原版。
6. **`OkHttpClient` 加了 `.proxy(Proxy.NO_PROXY)`**（`NetworkModule`）：模拟器一旦开了全局代理（为了下载封面图），我们自己的后端请求也会被送去代理，然后被代理拒绝（表现为 `IOException: unexpected end of stream on http://10.0.2.2:8080/`）。这一行让**接口直连笔记本、图片走代理**，两者互不干扰（coil 用的是它自己的 OkHttp，仍然跟随系统代理）。
7. **`MainApplication` 给 coil 换了一个带浏览器 User-Agent 的 OkHttpClient**：`upload.wikimedia.org` 对「没有 UA」或 `okhttp/4.x` 这类库默认 UA 一律回 **HTTP 403**，而对 `Dalvik/...`、浏览器 UA 正常返回 200。这是封面图一直空白的真正原因（文字数据来自自己的后端，所以一直正常）。
8. `AlbumAdapter` 的 coil 调用加了 `listener(...)`：图片下载失败本来是**静默**的，加了之后 Logcat 过滤 `CoilLoad` 就能看到 `ok <歌名>` / `FAIL <歌名>: ...`，排障全靠它。

## 3. 依赖版本

| 组件 | 版本 |
|---|---|
| Gradle / AGP / Kotlin | 9.6.1 / 9.4.1 / 2.2.10（AGP 内建） |
| compileSdk · targetSdk · minSdk | 37 · 37 · 24 |
| navigation-fragment-ktx / ui-ktx | **2.5.3**（讲义原版） |
| retrofit + converter-gson | **2.9.0**（讲义原版） |
| coil（ImageView 版） | 2.2.2 |
| material / appcompat / constraintlayout | 1.12.0 / 1.7.0 / 2.2.1 |
| recyclerview | 1.4.0 |
| Hilt / KSP | **2.60.1** / 2.2.10-2.0.2 |

> AGP 9 两个必须记住的坑：Hilt 必须 ≥ 2.60.1（2.57.x 报 `Android BaseExtension not found`）；
> `gradle.properties` 必须 `android.disallowKotlinSourceSets=false`，否则 KSP 报
> `kotlin.sourceSets DSL is not allowed with built-in Kotlin`。

## 4. 验证记录（2026-09-27 真机/模拟器实测）

- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL，0 warning**，APK **7.99 MB**。
- 前置：先起后端 `spotify_backend\build\install\spotify_backend\bin\spotify_backend.bat`（8080 返回 200）。
- Logcat `tag:Network` 输出（与讲义截图 62 一致）：
  ```
  D Network: [Section(sectionTitle=Top mixes, albums=[Album(id=1, name=Hexagonal, year=2008,
  cover=https://upload.wikimedia.org/.../Leessang-Hexagonal_(cover).jpg, artists=Lessang, ...
  ```
- 界面文本（uiautomator dump）：状态行 `Top mixes · Made for you · This Is: K-Music`，
  列表 `Hexagonal / Lessang · 2008`、`Still Fantasy / Jay Chou · 2006`、`Dangerous /
  Michael Jackson · 1991`、`Kite / Stefanie Sun · 2001`，底部导航 **Home / Favorite**。
  截图：讲义 L55 目录 `36_本机运行_View体系版_Home页真实后端数据.png`。
- 2026-09-27 封面图（第二轮复测的同一轮）：开「允许局域网连接」+ `adb shell settings put global
  http_proxy 10.0.2.2:7890` 后，Logcat `CoilLoad` 四行全部 `ok`：
  ```
  D CoilLoad: ok   Hexagonal      D CoilLoad: ok   Dangerous
  D CoilLoad: ok   Still Fantasy  D CoilLoad: ok   Kite
  ```
  coil 磁盘缓存出现当日新文件，截图见讲义 L55 目录
  `36b_本机运行_封面图代理生效后.png`。**前提：电脑代理要开「允许局域网连接」，否则模拟器连不上 7890。**
- 2026-09-27 第二轮「逐行对齐讲义」后复测：`assembleDebug` 仍 **0 warning**，
  Logcat `tag:Network` 同样打出 `[Section(sectionTitle=Top mixes, albums=[Album(id=1, …`，
  界面文本不变（`Top mixes · Made for you · This Is: K-Music` + 4 张专辑 + Home/Favorite）。

## 5. 跑起来的顺序（别忘了后端）

```bash
# 1) 起 Ktor 后端（这个黑窗口要一直开着）
cd D:\SoftwareEngineering\project4\spotify_backend\build\install\spotify_backend\bin
.\spotify_backend.bat

# 2) 编译安装
cd D:\SoftwareEngineering\project4\spotify_app
.\gradlew :app:assembleDebug
```

后端没开时的表现：Home 状态行显示
`Could not load the feed. Is spotify_backend running on port 8080?`——这是 ViewModel 的
失败分支按预期工作，不是代码错。
