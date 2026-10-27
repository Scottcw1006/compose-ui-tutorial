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
          <div class="panel-head"><span>${id}.kt</span><button class="copy-btn" type="button">複製</button></div>
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

async function loadSnippets() {
  const blocks = document.querySelectorAll("code[data-snippet]");
  await Promise.all([...blocks].map(async (code) => {
    try {
      const res = await fetch(`snippets/${code.dataset.snippet}.kt`);
      if (!res.ok) throw new Error(res.status);
      code.textContent = await res.text();
      if (window.hljs) hljs.highlightElement(code);
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
      await navigator.clipboard.writeText(code.textContent);
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
loadSnippets();
