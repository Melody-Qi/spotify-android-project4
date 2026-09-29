# spotify_backend（Lesson 52/54 讲义重建版，2026-09-24）

按讲义重建的 Ktor 后端。旧版（第一次复现，含单文件 Application.kt 与 8888 端口）已封存到
`../_archive/spotify_backend_20260924_lesson52_first_repro/`。

## 端点（与讲义截图 41 一致，端口统一 8080）

| Endpoint | 返回 |
|---|---|
| `GET /` | text/plain 存活检查 |
| `GET /feed` | 3 个 section 的 JSON（Top mixes / Made for you / This Is: K-Music） |
| `GET /playlists` | 播放列表数组 |
| `GET /playlist/{id}` | 单个播放列表（含 songs），未找到返回 404 |
| `GET /songs/*.mp3` | static 音频（audio/mpeg） |

## 运行

```powershell
.\gradlew.bat run          # 开发运行，监听 http://127.0.0.1:8080
.\gradlew.bat installDist  # 产物在 build\install\spotify_backend\bin\
```

Android 端 BASE_URL：模拟器用 `http://10.0.2.2:8080/`（见讲义截图 59/63）。

## 结构

```
src/main/kotlin/com/laioffer/Application.kt      # embeddedServer(Netty, 8080) + ContentNegotiation(json)
src/main/kotlin/com/laioffer/plugins/Routing.kt # 4 类端点路由
src/main/resources/feed.json                    # Top mixes: Hexagonal(Leessang) / Still Fantasy(Jay Chou)
src/main/resources/playlists.json
src/main/resources/static/songs/*.mp3
```

## 音频文件说明

按讲义文件名就位；LeeSSang 系列 6 首复用 `LeeSSang_Hexagonal.mp3` 实体，
周杰伦 4 首复用《夜曲》片段，`Nxde.mp3`/`听妈妈的话.mp3`/`最长的电影.mp3` 为原文件。
仅本地教学用。

## 验证记录（2026-09-24）

```
/                 200 text/plain
/feed             200 application/json 1267B
/playlists        200 application/json 1054B
/playlist/1       200 application/json 1258B
/playlist/2       200 application/json  421B
/songs/solo.mp3   200 audio/mpeg 52079B
```
