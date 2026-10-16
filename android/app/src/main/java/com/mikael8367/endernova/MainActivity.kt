package com.mikael8367.endernova

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.SeekBar
import org.json.JSONObject
import java.net.URL
import java.security.MessageDigest
import java.io.File
import java.util.zip.ZipInputStream

/**
 * EnderNova Android UI prototype. It mirrors the green-on-black dashboard concept.
 * Game launching is intentionally not advertised as working until the Android JVM,
 * libraries/assets and mod-loader installation are integrated and tested.
 */
class MainActivity : Activity() {
    private val bg = Color.rgb(5, 12, 12)
    private val panel = Color.rgb(8, 25, 22)
    private val panel2 = Color.rgb(9, 35, 30)
    private val green = Color.rgb(52, 236, 112)
    private val mint = Color.rgb(76, 242, 172)
    private val muted = Color.rgb(151, 177, 168)
    private lateinit var body: LinearLayout
    private lateinit var currentPage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        currentPage = if (getSharedPreferences("endernova", MODE_PRIVATE).getBoolean("setup_done", false)) "Início" else "Primeiros passos"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        root.addView(makeHeader())
        val scroll = ScrollView(this).apply { setFillViewport(true) }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(18))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(makeBottomNav())
        setContentView(root)
        showPage(currentPage)
    }

    private fun makeHeader(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            setBackgroundColor(Color.rgb(4, 10, 10))
        }
        val mark = TextView(this).apply {
            text = "▦"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(green)
            background = rounded(panel2, 15, green)
        }
        bar.addView(mark, LinearLayout.LayoutParams(dp(48), dp(48)))
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        titles.addView(text("EnderNova", 20f, Color.WHITE, true))
        titles.addView(text("L A U N C H E R  •  MINECRAFT JAVA", 9f, mint, true))
        bar.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = TextView(this).apply {
            text = "⚙"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(mint)
            setOnClickListener { showPage("Configurações") }
        }
        bar.addView(settings, LinearLayout.LayoutParams(dp(40), dp(48)))
        return bar
    }

    private fun makeBottomNav(): View {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(6), dp(3), dp(7))
            setBackgroundColor(Color.rgb(3, 10, 9))
        }
        listOf("Início" to "⌂", "Instâncias" to "▣", "Versões" to "▤", "Modloaders" to "⚒", "Contas" to "♙", "Config." to "⚙").forEach { item ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(1), dp(3), dp(1), dp(3))
                setOnClickListener { showPage(if (item.first == "Config.") "Configurações" else item.first) }
            }
            cell.addView(text(item.second, 19f, if (currentPage == item.first || (item.first == "Config." && currentPage == "Configurações")) mint else muted, true).apply { gravity = Gravity.CENTER })
            cell.addView(text(item.first, 9f, if (currentPage == item.first || (item.first == "Config." && currentPage == "Configurações")) mint else muted, true).apply { gravity = Gravity.CENTER })
            nav.addView(cell, LinearLayout.LayoutParams(0, -2, 1f))
        }
        return nav
    }

    private fun showPage(page: String) {
        currentPage = page
        body.removeAllViews()
        when (page) {
            "Primeiros passos" -> onboarding()
            "Início" -> home()
            "Instâncias" -> instances()
            "Versões" -> versions()
            "Modloaders" -> modloaders()
            "Contas" -> accounts()
            "Loja de Mods" -> mods()
            "Configurações" -> settings()
            "Otimização" -> optimization()
            else -> home()
        }
        // Add a compact navigation link to the two additional screens represented in the reference.
        if (page == "Início") {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(actionButton("Loja de Mods") { showPage("Loja de Mods") }, LinearLayout.LayoutParams(0, dp(42), 1f))
            row.addView(space(8), LinearLayout.LayoutParams(dp(8), 1))
            row.addView(actionButton("Otimização") { showPage("Otimização") }, LinearLayout.LayoutParams(0, dp(42), 1f))
            body.addView(row)
        }
        // Soft staggered entrance animation for each screen's content.
        for (index in 0 until body.childCount) {
            val child = body.getChildAt(index)
            child.alpha = 0f
            child.translationY = dp(10).toFloat()
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 35L).coerceAtMost(280L))
                .setDuration(240L)
                .start()
        }
    }

    private fun onboarding() {
        pageHeading("Primeira configuração", "Escolha as runtimes que deseja preparar.")
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        listOf("Java 8", "Java 17", "Java 21").forEach { runtime ->
            val key = "runtime_" + runtime.replace(" ", "")
            val check = android.widget.CheckBox(this).apply {
                text = runtime
                textSize = 15f
                setTextColor(Color.WHITE)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                isChecked = prefs.getBoolean(key, runtime == "Java 8")
                setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
            }
            body.addView(check)
        }
        body.addView(space(10))
        body.addView(text("Nome do perfil offline", 15f, Color.WHITE, true))
        val player = android.widget.EditText(this).apply {
            hint = "Seu nome de jogador"
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            setText(prefs.getString("player_name", ""))
            background = rounded(panel, 10, mint)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        body.addView(player, LinearLayout.LayoutParams(-1, dp(48)))
        body.addView(space(10))
        val warning = card()
        warning.addView(text("Download do motor", 15f, Color.WHITE, true))
        warning.addView(text("A seleção fica salva, mas esta versão ainda não inclui pacotes Java para baixar nem o motor de execução do Minecraft. O jogo não inicia até essa integração ser feita.", 12f, muted))
        body.addView(warning)
        body.addView(actionButton("Salvar e continuar") {
            prefs.edit().putString("player_name", player.text.toString().trim()).putBoolean("setup_done", true).apply()
            showPage("Início")
        })
    }

    private fun home() {
        body.addView(text("SEU MUNDO. SEU DESEMPENHO.", 10f, mint, true))
        body.addView(text("Olá, jogador!", 28f, Color.WHITE, true))
        body.addView(text("Sua próxima aventura começa aqui.", 13f, muted))
        body.addView(space(16))
        val device = card()
        device.addView(text("▣  DISPOSITIVO", 11f, mint, true))
        device.addView(text("Samsung Galaxy A12  •  Android 12", 14f, Color.WHITE, true))
        device.addView(text("Arquitetura ARM64  •  Interface de demonstração", 11f, muted))
        body.addView(device)
        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        metrics.addView(metric("◉", "RAM configurada", "2 GB", "3,7 GB total"), LinearLayout.LayoutParams(0, -2, 1f))
        metrics.addView(space(10), LinearLayout.LayoutParams(dp(10), 1))
        metrics.addView(metric("▤", "Instâncias", "1", "Salva localmente"), LinearLayout.LayoutParams(0, -2, 1f))
        body.addView(metrics)
        body.addView(sectionTitle("Minhas Instâncias", "VER TODAS") { showPage("Instâncias") })
        val empty = card()
        empty.addView(text("Nenhuma instância criada", 15f, Color.WHITE, true))
        empty.addView(text("Escolha uma versão na aba Versões para começar.", 12f, muted))
        body.addView(empty)
        body.addView(space(8))
        body.addView(actionButton("▶   JOGAR  •  MOTOR AINDA NÃO INTEGRADO") {
            info("O motor Java para Android ainda não está integrado. Este botão não inicia o jogo.")
        })
        body.addView(space(12))
        val note = card()
        note.addView(text("ESTADO DO PROJETO", 11f, mint, true))
        note.addView(text("A interface está em desenvolvimento. Instalação completa, runtimes Java, modloaders e inicialização do Minecraft ainda precisam ser integrados.", 12f, muted))
        body.addView(note)
    }

    private fun instances() {
        pageHeading("Minhas Instâncias", "Gerencie suas configurações locais.")
        val empty = card()
        empty.addView(text("Ainda não há instâncias", 15f, Color.WHITE, true))
        empty.addView(text("Nenhuma instância foi adicionada automaticamente.", 12f, muted))
        body.addView(empty)
        body.addView(actionButton("+  Criar instância a partir de uma versão") { showPage("Versões") })
    }

    private fun versions() {
        pageHeading("Versões do Minecraft", "Buscando o manifesto oficial de versões…")
        body.addView(actionButton("Carregar / atualizar versões") { loadVersions() })
        loadVersions()
    }

    private fun loadVersions() {
        Thread {
            try {
                val conn = URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json").openConnection()
                conn.connectTimeout = 12000
                conn.readTimeout = 12000
                val json = JSONObject(conn.getInputStream().bufferedReader().use { it.readText() })
                val list = json.getJSONArray("versions")
                val versions = ArrayList<Pair<String, String>>()
                for (i in 0 until list.length()) {
                    val v = list.getJSONObject(i)
                    versions.add(v.getString("id") to v.getString("type"))
                }
                runOnUiThread {
                    if (currentPage != "Versões") return@runOnUiThread
                    body.removeAllViews()
                    pageHeading("Versões do Minecraft", "${versions.size} versões no manifesto oficial")
                    body.addView(actionButton("Atualizar catálogo") { loadVersions() })
                    versions.forEach { v ->
                        val c = card()
                        c.addView(text("Minecraft ${v.first}", 14f, Color.WHITE, true))
                        c.addView(text(v.second, 11f, muted))
                        c.addView(actionButton("Baixar versão") {
                            getSharedPreferences("endernova", MODE_PRIVATE).edit().putString("selected_version", v.first).apply()
                            downloadMinecraftVersion(v.first)
                        })
                        body.addView(c)
                    }
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (currentPage == "Versões") {
                        body.removeAllViews()
                        pageHeading("Versões do Minecraft", "Não foi possível carregar o manifesto. Verifique a internet.")
                        body.addView(actionButton("Tentar novamente") { loadVersions() })
                    }
                }
            }
        }.start()
    }


    private fun downloadMinecraftVersion(versionId: String) {
        info("Preparando download do Minecraft " + versionId + "…")
        Thread {
            try {
                fun fetchText(address: String): String {
                    val conn = URL(address).openConnection()
                    conn.connectTimeout = 20000
                    conn.readTimeout = 60000
                    conn.setRequestProperty("User-Agent", "EnderNova-Launcher")
                    return conn.getInputStream().bufferedReader().use { it.readText() }
                }
                val manifest = JSONObject(fetchText("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                val versions = manifest.getJSONArray("versions")
                var metadataUrl: String? = null
                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)
                    if (item.getString("id") == versionId) {
                        metadataUrl = item.getString("url")
                        break
                    }
                }
                val selectedUrl = metadataUrl ?: throw IllegalStateException("Versão não encontrada no manifesto oficial.")
                val metadataText = fetchText(selectedUrl)
                val metadata = JSONObject(metadataText)
                val client = metadata.getJSONObject("downloads").getJSONObject("client")
                val jarUrl = client.getString("url")
                val expectedSha1 = client.optString("sha1", "")
                val expectedSize = client.optLong("size", -1L)
                val versionDir = File(filesDir, ".minecraft/versions/" + versionId)
                versionDir.mkdirs()
                val jsonFile = File(versionDir, versionId + ".json")
                val jarFile = File(versionDir, versionId + ".jar")
                val tempJar = File(versionDir, versionId + ".jar.part")
                val connection = URL(jarUrl).openConnection()
                connection.connectTimeout = 20000
                connection.readTimeout = 120000
                connection.setRequestProperty("User-Agent", "EnderNova-Launcher")
                connection.getInputStream().use { input ->
                    tempJar.outputStream().use { output -> input.copyTo(output) }
                }
                if (expectedSize > 0 && tempJar.length() != expectedSize) {
                    tempJar.delete()
                    throw IllegalStateException("O tamanho do arquivo não confere; tente novamente.")
                }
                if (expectedSha1.isNotBlank()) {
                    val digest = MessageDigest.getInstance("SHA-1")
                    tempJar.inputStream().use { input ->
                        val buffer = ByteArray(8192)
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            digest.update(buffer, 0, n)
                        }
                    }
                    val actual = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
                    if (!actual.equals(expectedSha1, ignoreCase = true)) {
                        tempJar.delete()
                        throw IllegalStateException("Verificação SHA-1 falhou; arquivo rejeitado.")
                    }
                }
                if (!tempJar.renameTo(jarFile)) {
                    tempJar.copyTo(jarFile, overwrite = true)
                    tempJar.delete()
                }
                jsonFile.writeText(metadataText)
                getSharedPreferences("endernova", MODE_PRIVATE).edit()
                    .putString("selected_version", versionId)
                    .putBoolean("version_downloaded_" + versionId, true).apply()
                runOnUiThread {
                    info("Minecraft " + versionId + ": cliente e metadados oficiais baixados e verificados. Bibliotecas e assets ainda precisam ser integrados.")
                    showPage("Versões")
                }
            } catch (e: Exception) {
                runOnUiThread { info("Falha ao baixar " + versionId + ": " + (e.message ?: "erro de rede")) }
            }
        }.start()
    }

    private fun modloaders() {
        pageHeading("Escolha o Modloader", "Selecione o perfil desejado.")
        listOf("Forge" to "Mais compatibilidade com mods", "Fabric" to "Leve e moderno", "OptiFine" to "Opções gráficas", "Forge + OptiFine" to "Perfil combinado").forEach {
            val c = card()
            c.addView(text("⚒  ${it.first}", 17f, Color.WHITE, true))
            c.addView(text(it.second, 12f, muted))
            c.addView(actionButton("Selecionar") { info("${it.first}: instalação ainda não integrada.") })
            body.addView(c)
        }
    }

    private fun mods() {
        pageHeading("Loja de Mods", "Catálogo demonstrativo.")
        listOf("BetterFps" to "Desempenho", "OptiFine" to "Gráficos", "JourneyMap" to "Mapas", "JEI" to "Utilitários", "Create" to "Tecnologia").forEach {
            val c = card()
            c.addView(text("▦  ${it.first}", 15f, Color.WHITE, true))
            c.addView(text(it.second, 11f, muted))
            c.addView(actionButton("Instalar") { info("A instalação de mods ainda não está conectada.") })
            body.addView(c)
        }
    }

    private fun accounts() {
        pageHeading("Perfis", "Escolha o tipo de conta.")
        listOf("Offline" to "Perfil local de demonstração", "Microsoft" to "Autenticação não conectada", "Ely.by" to "Autenticação não conectada").forEach {
            val c = card()
            c.addView(text("♙  ${it.first}", 16f, Color.WHITE, true))
            c.addView(text(it.second, 11f, muted))
            c.addView(actionButton("Selecionar / Entrar") { info("A autenticação real ainda não foi integrada.") })
            body.addView(c)
        }
    }

    private fun settings() {
        pageHeading("Configurações", "Personalize o launcher.")
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        val currentRam = prefs.getInt("ram_mb", 2048)
        body.addView(text("Memória RAM: ${currentRam} MB", 15f, Color.WHITE, true))
        val seek = SeekBar(this).apply {
            max = 3072
            progress = (currentRam - 512).coerceIn(0, 3072)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) prefs.edit().putInt("ram_mb", progress + 512).apply()
                }
                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) { showPage("Configurações") }
            })
        }
        body.addView(seek)
        body.addView(space(10))
        listOf("Java 8", "Java 17", "Java 21").forEach { runtime ->
            val key = "runtime_" + runtime.replace(" ", "")
            val folder = File(filesDir, "runtimes/java" + runtime.removePrefix("Java "))
            val ready = File(folder, ".download_complete").exists()
            val c = card()
            c.addView(text("$runtime  •  " + if (ready) "Baixado" else "Não instalado", 15f, if (ready) mint else Color.WHITE, true))
            c.addView(text("Runtime Android ARM64 • armazenado no app", 11f, muted))
            c.addView(actionButton(if (ready) "Baixar novamente" else "Baixar $runtime") {
                downloadRuntime(runtime)
            })
            body.addView(c)
            val check = android.widget.CheckBox(this).apply {
                text = "Preferir $runtime para versões compatíveis"
                setTextColor(Color.WHITE)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                isChecked = prefs.getBoolean(key, runtime == "Java 8")
                isEnabled = ready
                setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
            }
            body.addView(check)
        }
        listOf("Animações da interface", "Notificações", "Confirmar antes de limpar dados").forEach { label ->
            val toggle = android.widget.Switch(this).apply {
                text = label
                setTextColor(Color.WHITE)
                isChecked = prefs.getBoolean("setting_" + label, true)
                setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("setting_" + label, checked).apply() }
            }
            body.addView(toggle)
        }
        body.addView(actionButton("Refazer configuração inicial") {
            prefs.edit().putBoolean("setup_done", false).apply()
            showPage("Primeiros passos")
        })
        body.addView(actionButton("Limpar preferências") {
            prefs.edit().clear().apply()
            info("Preferências locais apagadas.")
            showPage("Configurações")
        })
        settingRow("Aparência", "Tema verde e preto")
        settingRow("Contas", "Offline, Microsoft e Ely.by")
        settingRow("Sobre", "EnderNova Launcher • Mikael Dev")
    }

    private fun downloadRuntime(runtime: String) {
        val version = runtime.removePrefix("Java ")
        val artifact = when (version) {
            "8" -> "jre8-pojav"
            "17" -> "jre17-pojav"
            "21" -> "jre21-pojav"
            else -> { info("Runtime não suportada."); return }
        }
        val workflow = if (version == "8") "buildjre8" else "buildjre17-21"
        val url = "https://nightly.link/PojavLauncherTeam/android-openjdk-build-multiarch/workflows/build/$workflow/$artifact.zip"
        val target = File(filesDir, "runtimes/java$version")
        val temp = File(cacheDir, "$artifact.zip.part")
        info("Baixando Java $version para Android. Mantenha a internet conectada.")
        Thread {
            try {
                val conn = URL(url).openConnection()
                conn.connectTimeout = 20000
                conn.readTimeout = 60000
                conn.setRequestProperty("User-Agent", "EnderNova-Launcher")
                conn.getInputStream().use { input ->
                    temp.parentFile?.mkdirs()
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                if (!temp.exists() || temp.length() < 1024 * 1024) throw IllegalStateException("O pacote recebido está incompleto.")
                val staging = File(filesDir, "runtimes/java$version-staging")
                staging.deleteRecursively()
                staging.mkdirs()
                ZipInputStream(temp.inputStream().buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val out = File(staging, entry.name)
                        if (!out.canonicalPath.startsWith(staging.canonicalPath + File.separator)) {
                            throw SecurityException("Caminho inválido dentro do pacote.")
                        }
                        if (entry.isDirectory) out.mkdirs() else {
                            out.parentFile?.mkdirs()
                            out.outputStream().use { output -> zip.copyTo(output) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                if (staging.listFiles().isNullOrEmpty()) throw IllegalStateException("O pacote não contém arquivos.")
                target.deleteRecursively()
                if (!staging.renameTo(target)) throw IllegalStateException("Não foi possível guardar o runtime.")
                File(target, ".download_complete").writeText("source=nightly.link\nversion=$version\n")
                runOnUiThread {
                    info("Java $version baixado e guardado no app. A integração para executar a JVM ainda precisa ser feita.")
                    showPage("Configurações")
                }
            } catch (e: Exception) {
                temp.delete()
                runOnUiThread { info("Falha ao baixar Java $version: ${e.message ?: "erro de rede"}. Tente novamente mais tarde.") }
            } finally {
                temp.delete()
            }
        }.start()
    }

    private fun optimization() {
        pageHeading("Otimização de Performance", "Opções de interface; ainda não aplicadas ao jogo.")
        listOf("FPS Booster", "Menos animações", "Texturas otimizadas").forEach { settingRow(it, "Preferência demonstrativa") }
        settingRow("Limite de FPS", "60 FPS • preferência visual")
        settingRow("Renderização", "Rápido • preferência visual")
        body.addView(actionButton("Aplicar configurações") { info("Estas opções ainda não alteram o Minecraft.") })
    }

    private fun pageHeading(title: String, subtitle: String) {
        body.addView(text(title, 25f, Color.WHITE, true))
        body.addView(text(subtitle, 12f, muted))
        body.addView(space(14))
    }

    private fun sectionTitle(title: String, action: String, click: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, dp(8))
        }
        row.addView(text(title, 17f, Color.WHITE, true), LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(text(action, 10f, mint, true).apply { setOnClickListener { click() } })
        return row
    }

    private fun instanceRow(name: String, detail: String, action: String) {
        val c = card().apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val icon = TextView(this).apply {
            text = "▦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(mint)
            background = rounded(panel2, 12, null)
        }
        c.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(5), 0)
            addView(text(name, 14f, Color.WHITE, true))
            addView(text(detail, 10f, muted))
        }
        c.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        c.addView(text(action, 20f, green, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(8), dp(6), dp(8))
            setOnClickListener { info("A inicialização do Minecraft ainda não está disponível nesta versão.") }
        })
        body.addView(c)
    }

    private fun metric(icon: String, title: String, value: String, detail: String): View {
        val c = card()
        c.addView(text(icon, 22f, green, true))
        c.addView(text(title, 10f, muted, true))
        c.addView(text(value, 23f, Color.WHITE, true))
        c.addView(text(detail, 10f, muted))
        return c
    }

    private fun settingRow(title: String, detail: String) {
        val c = card()
        c.addView(text(title, 14f, Color.WHITE, true))
        c.addView(text(detail, 11f, muted))
        body.addView(c)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(13), dp(14), dp(13))
        background = rounded(panel, 14, Color.rgb(19, 62, 49))
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        layoutParams = lp
    }

    private fun actionButton(label: String, click: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 11f
        isAllCaps = false
        setTextColor(Color.rgb(2, 24, 12))
        background = rounded(green, 10, null)
        setOnClickListener { click() }
        val lp = LinearLayout.LayoutParams(-1, dp(44))
        lp.topMargin = dp(8)
        layoutParams = lp
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(2), 0, dp(2))
    }

    private fun rounded(color: Int, radius: Int, stroke: Int?): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun space(height: Int): View = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(height)) }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun info(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}
