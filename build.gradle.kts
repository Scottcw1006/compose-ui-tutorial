import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.4.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
    id("org.jetbrains.compose") version "1.12.2"
}

val composeVersion = "1.12.2"
val material3Version = "1.9.0"

// 從 demo 原始碼中擷取 `// [snippet:id]` ... `// [/snippet]` 區塊，
// 輸出成 snippets/<id>.kt，讓網頁上顯示的代碼與實際執行的 demo 永遠一致。
val extractSnippets by tasks.registering {
    val srcDir = layout.projectDirectory.dir("src/wasmJsMain/kotlin")
    val outDir = layout.buildDirectory.dir("generated/snippets")
    inputs.dir(srcDir)
    outputs.dir(outDir)
    doLast {
        val out = outDir.get().dir("snippets").asFile
        out.deleteRecursively()
        out.mkdirs()
        val start = Regex("""^\s*// \[snippet:([\w-]+)]\s*$""")
        val end = Regex("""^\s*// \[/snippet]\s*$""")
        srcDir.asFile.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            var id: String? = null
            val buf = mutableListOf<String>()
            file.readLines().forEach { line ->
                val m = start.find(line)
                when {
                    m != null -> { id = m.groupValues[1]; buf.clear() }
                    id != null && end.matches(line) -> {
                        File(out, "$id.kt").writeText(buf.joinToString("\n").trimIndent().trim() + "\n")
                        id = null
                    }
                    id != null -> buf += line
                }
            }
            check(id == null) { "Unclosed snippet '$id' in ${file.name}" }
        }
    }
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            resources.srcDir(extractSnippets)
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
                implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
                implementation("org.jetbrains.compose.ui:ui:$composeVersion")
                implementation("org.jetbrains.compose.material3:material3:$material3Version")
            }
        }
    }
}
