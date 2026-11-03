// 章節目錄：新增章節時只要改這裡，側欄與上下頁會自動更新
const CHAPTERS = [
  {
    group: "入門",
    items: [{ href: "index.html", title: "開始之前" }],
  },
  {
    group: "基礎排版",
    items: [
      { href: "layout.html", title: "Column、Row、Box" },
      { href: "modifier.html", title: "Modifier" },
    ],
  },
  {
    group: "狀態與資料",
    items: [
      { href: "state.html", title: "State 與重組" },
      { href: "lists.html", title: "Lazy 列表" },
      { href: "coroutines.html", title: "Coroutine 與非同步" },
    ],
  },
  {
    group: "動畫與特效",
    items: [
      { href: "animation.html", title: "動畫基礎" },
      { href: "gestures.html", title: "手勢與拖曳" },
      { href: "canvas.html", title: "Canvas 自訂繪圖" },
      { href: "effects.html", title: "graphicsLayer 視覺特效" },
    ],
  },
];

const currentPage = location.pathname.split("/").pop() || "index.html";

function renderSidebar() {
  const nav = document.getElementById("sidebar");
  if (!nav) return;
  nav.innerHTML = CHAPTERS.map((g) => `
    <div class="group">
      <p class="group-title">${g.group}</p>
      ${g.items.map((it) => it.soon
        ? `<span class="soon">${it.title}<span class="badge">即將推出</span></span>`
        : `<a href="${it.href}" class="${it.href === currentPage ? "active" : ""}">${it.title}</a>`
      ).join("")}
    </div>`).join("");
}

function renderPager() {
  const pager = document.querySelector(".pager");
  if (!pager) return;
  const pages = CHAPTERS.flatMap((g) => g.items).filter((it) => !it.soon);
  const i = pages.findIndex((p) => p.href === currentPage);
  const prev = pages[i - 1];
  const next = pages[i + 1];
  pager.innerHTML =
    (prev ? `<a class="prev" href="${prev.href}"><small>上一章</small>${prev.title}</a>` : "<span></span>") +
    (next ? `<a class="next" href="${next.href}"><small>下一章</small>${next.title}</a>` : "<span></span>");
}

// 把 <div class="example" data-id="..."> 展開成「代碼 + 實際呈現」兩欄。
// 必須在 composeApp.js 之前同步執行，Kotlin 端才找得到 [data-demo] 節點。
function renderExamples() {
  document.querySelectorAll(".example[data-id]").forEach((el) => {
    const id = el.dataset.id;
    const height = el.dataset.height || "300";
    const hint = el.dataset.hint ? `<p class="preview-hint">${el.dataset.hint}</p>` : "";
    el.innerHTML = `
      <div>
        <span class="part-label">代碼</span>
        <div class="code-panel">
          <div class="panel-head"><span>${id}.kt</span><span class="panel-actions"><button class="copy-btn" type="button">複製</button></span></div>
          <pre><code class="language-kotlin" data-snippet="${id}">載入中…</code></pre>
        </div>
      </div>
      <div class="preview-panel">
        <span class="part-label">實際呈現</span>
        <div class="device">
          <div class="screen">
            <div class="demo" data-demo="${id}" style="height:${height}px"></div>
            <div class="loading">Compose 載入中…</div>
          </div>
        </div>
        ${hint}
      </div>`;
  });
}

function showCode(code, text) {
  code.textContent = text;
  if (!window.hljs) return;
  delete code.dataset.highlighted;
  hljs.highlightElement(code);
}

async function loadSnippets() {
  // 直接寫在 HTML 裡、不需要載入的靜態代碼區塊
  if (window.hljs) document.querySelectorAll(".code-panel code:not([data-snippet])").forEach((c) => hljs.highlightElement(c));
  const blocks = document.querySelectorAll("code[data-snippet]");
  await Promise.all([...blocks].map(async (code) => {
    try {
      const res = await fetch(`snippets/${code.dataset.snippet}.kt`);
      if (!res.ok) throw new Error(res.status);
      const text = await res.text();
      // 片段開頭的 import 預設收合，避免蓋過重點；「複製」永遠包含完整內容
      const lines = text.split("\n");
      const n = lines.findIndex((l) => !l.startsWith("import ") && l.trim() !== "");
      const imports = n > 0 ? lines.slice(0, n).filter((l) => l.startsWith("import ")).length : 0;
      code.fullText = text;
      code.bodyText = n > 0 ? lines.slice(n).join("\n") : text;
      if (imports > 0) {
        const btn = document.createElement("button");
        btn.className = "imports-btn";
        btn.type = "button";
        btn.textContent = `顯示 import（${imports}）`;
        btn.addEventListener("click", () => {
          const showing = code.textContent === code.fullText;
          showCode(code, showing ? code.bodyText : code.fullText);
          btn.textContent = showing ? `顯示 import（${imports}）` : "隱藏 import";
        });
        code.closest(".code-panel").querySelector(".panel-actions").prepend(btn);
      }
      showCode(code, code.bodyText);
    } catch (e) {
      code.textContent = `// 無法載入代碼片段：${code.dataset.snippet}`;
    }
  }));
}

function setupCopyButtons() {
  document.addEventListener("click", async (e) => {
    const btn = e.target.closest(".copy-btn");
    if (!btn) return;
    const code = btn.closest(".code-panel").querySelector("code");
    try {
      await navigator.clipboard.writeText(code.fullText ?? code.textContent);
      btn.textContent = "已複製";
    } catch {
      btn.textContent = "複製失敗";
    }
    setTimeout(() => (btn.textContent = "複製"), 1500);
  });
}

function renderToc() {
  const toc = document.getElementById("toc");
  const sections = [...document.querySelectorAll(".lesson[id]")];
  if (!toc || sections.length === 0) return;
  toc.innerHTML = `<div class="title">本頁內容</div>` + sections
    .map((s) => `<a href="#${s.id}">${s.querySelector("h2").textContent}</a>`)
    .join("");
  const links = new Map([...toc.querySelectorAll("a")].map((a) => [a.hash.slice(1), a]));
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      links.forEach((a) => a.classList.remove("active"));
      links.get(entry.target.id)?.classList.add("active");
    });
  }, { rootMargin: "-80px 0px -70% 0px" });
  sections.forEach((s) => observer.observe(s));
}

// 即時範例需要瀏覽器支援 WebAssembly GC（Chrome / Edge 119+、Firefox 120+、Safari 18.2+）。
// 這是一個只宣告一個 GC struct 型別的最小模組：能通過驗證就代表支援。
function supportsWasmGC() {
  try {
    return WebAssembly.validate(new Uint8Array([0, 97, 115, 109, 1, 0, 0, 0, 1, 5, 1, 95, 1, 120, 0]));
  } catch {
    return false;
  }
}

// 把還沒載入完成的範例，從「Compose 載入中…」換成說明文字
function showDemoMessage(text) {
  document.querySelectorAll(".device .demo:not([data-ready]) + .loading").forEach((el) => {
    el.textContent = text;
    el.classList.add("failed");
  });
}

function watchDemoLoading() {
  if (!document.querySelector("[data-demo]")) return;
  if (!supportsWasmGC()) {
    showDemoMessage("你的瀏覽器不支援 WebAssembly GC，無法執行即時範例");
    const notice = document.createElement("aside");
    notice.className = "warn";
    notice.innerHTML =
      "<p><strong>無法執行即時範例：</strong>這個瀏覽器不支援 WebAssembly GC。代碼與說明仍可正常閱讀；" +
      "想操作範例，請改用 Chrome / Edge 119、Firefox 120 或 Safari 18.2 以上的版本。</p>";
    document.querySelector(".content h1")?.after(notice);
    return;
  }
  // 支援但載入失敗（網路中斷、檔案錯誤）或太久沒完成時，也不要讓使用者一直看著「載入中」
  const nothingReady = () => !document.querySelector("[data-demo][data-ready]");
  const onFail = () => nothingReady() && showDemoMessage("範例載入失敗，請重新整理頁面");
  window.addEventListener("error", (e) => {
    if (e.target?.src?.includes("composeApp") || e.filename?.includes("composeApp")) onFail();
  }, true);
  window.addEventListener("unhandledrejection", onFail);
  setTimeout(() => nothingReady() && showDemoMessage("範例載入時間比預期久，請稍候或重新整理頁面"), 30000);
}

function setupMenu() {
  document.querySelector(".menu-btn")?.addEventListener("click", () => {
    document.body.classList.toggle("nav-open");
  });
}

renderSidebar();
renderPager();
renderExamples();
renderToc();
setupMenu();
setupCopyButtons();
watchDemoLoading();
loadSnippets();
