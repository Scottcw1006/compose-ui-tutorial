# Compose 實戰教學

給 Android 開發者的 Jetpack Compose 教學網站。每一節都包含 **說明、代碼、實際呈現**，其中「實際呈現」是同一段 Compose 代碼透過 Compose Multiplatform 編譯成 WebAssembly、直接在瀏覽器中執行。

線上版：<https://scottcw1006.github.io/compose-ui-tutorial/>（每次 push 到 `main` 會由 GitHub Actions 自動建置並部署，見 `.github/workflows/deploy-pages.yml`）

## 本機執行

需要 JDK 17 以上（可直接用 Android Studio 內建的 JBR）。

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

開發模式（修改後自動重新建置，網址 http://localhost:8080）：

```bash
./gradlew wasmJsBrowserDevelopmentRun --continuous
```

建置正式版並以靜態伺服器瀏覽（網址 http://localhost:8000）：

```bash
./gradlew wasmJsBrowserDistribution && python3 -m http.server 8000 -d build/dist/wasmJs/productionExecutable
```

## 專案結構

```
src/wasmJsMain/
├── kotlin/
│   ├── Main.kt              # demo id → Composable 對照表，並掛載到網頁上
│   └── demos/*.kt           # 每章的示範 Composable
└── resources/
    ├── *.html               # 各章節頁面（說明文字寫在這裡）
    ├── css/site.css
    └── js/site.js           # 側欄目錄、代碼片段載入、上下頁
```

## 新增一個小節

1. 在 `demos/` 的 Kotlin 檔中寫好 Composable，用標記包起來，標記內的代碼會原封不動顯示在網頁上：

   ```kotlin
   // [snippet:my-demo]
   @Composable
   fun MyDemo() { ... }
   // [/snippet]
   ```

2. 在 `Main.kt` 的 `demos` 對照表加上 `"my-demo" to { MyDemo() }`。
3. 在章節 HTML 中加入：

   ```html
   <section class="lesson" id="my-demo">
     <h2>標題</h2>
     <span class="part-label">說明</span>
     <p>說明文字……</p>
     <div class="example" data-id="my-demo" data-height="260" data-hint="試試看：……"></div>
   </section>
   ```

   `site.js` 會把 `.example` 展開成代碼區與手機框預覽。

新增章節時，記得在 `js/site.js` 的 `CHAPTERS` 加上連結。

## 注意事項

- Demo 中的文字請用英文：Compose for Web 預設字型不含中文字，要顯示中文需另外打包字型（數 MB）。
- 依賴 Android 平台的 API（`LocalContext`、`R.drawable` 等）無法在網頁上示範。
