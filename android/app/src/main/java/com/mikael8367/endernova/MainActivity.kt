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
            "Diagnóstico" -> crashDiagnostics()
            else -> home()
        }
        // Add a compact navigation link to the two additional screens represented in the reference.
        if (page == "Início") {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(actionButton("Loja de Mods") { showPage("Loja de Mods") }, LinearLayout.LayoutParams(0, dp(42), 1f))
            row.addView(space(8), LinearLayout.LayoutParams(dp(8), 1))
            row.addView(actionButton("Otimização") { showPage("Otimização") }, LinearLayout.LayoutParams(0, dp(42), 1f))
            body.addView(row)
            body.addView(actionButton("Ver erros e copiar relatório") { showPage("Diagnóstico") })
        }
        // Soft staggered entrance animation for each screen's content.
        val animationsEnabled = getSharedPreferences("endernova", MODE_PRIVATE)
            .getBoolean("setting_Animações da interface", true)
        for (index in 0 until body.childCount) {
            val child = body.getChildAt(index)
            if (animationsEnabled) {
                child.alpha = 0f
                child.translationY = dp(10).toFloat()
                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay((index * 35L).coerceAtMost(280L))
                    .setDuration(240L)
                    .start()
            } else {
                child.alpha = 1f
                child.translationY = 0f
            }
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
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        val ramMb = prefs.getInt("ram_mb", 2048)
        val instanceCount = if (prefs.getString("selected_version", null) != null) 1 else 0
        metrics.addView(metric("◉", "RAM configurada", String.format("%.1f GB", ramMb / 1024.0), "Limite configurado"), LinearLayout.LayoutParams(0, -2, 1f))
        metrics.addView(space(10), LinearLayout.LayoutParams(dp(10), 1))
        metrics.addView(metric("▤", "Versão selecionada", instanceCount.toString(), if (instanceCount == 1) prefs.getString("selected_version", "") ?: "" else "Nenhuma"), LinearLayout.LayoutParams(0, -2, 1f))
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
        pageHeading("Minhas Instâncias", "Perfis separados por versão. A inicialização do jogo ainda depende do motor Java.")
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        fun readInstances(): org.json.JSONArray = try {
            org.json.JSONArray(prefs.getString("instances_json", "[]") ?: "[]")
        } catch (_: Exception) { org.json.JSONArray() }

        body.addView(actionButton("+ Criar instância") {
            val versions = prefs.getString("installed_versions_json", "[]") ?: "[]"
            val installed = try { org.json.JSONArray(versions) } catch (_: Exception) { org.json.JSONArray() }
            val selected = prefs.getString("selected_version", null)
            val nameInput = android.widget.EditText(this).apply {
                hint = "Nome da instância"
                setSingleLine(true)
                setTextColor(Color.WHITE)
                setHintTextColor(muted)
                background = rounded(panel, 10, mint)
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }
            val versionInput = android.widget.EditText(this).apply {
                hint = "Versão (ex.: 1.12.2)"
                setSingleLine(true)
                setTextColor(Color.WHITE)
                setHintTextColor(muted)
                setText(selected ?: "")
                background = rounded(panel, 10, mint)
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }
            val contentBox = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(8), dp(18), dp(4))
                addView(nameInput)
                addView(space(8))
                addView(versionInput)
                addView(text("Use uma versão já baixada para evitar downloads duplicados.", 11f, muted))
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Nova instância")
                .setView(contentBox)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Criar") { _, _ ->
                    val name = nameInput.text.toString().trim()
                    val version = versionInput.text.toString().trim()
                    if (name.isBlank() || !version.matches(Regex("^[A-Za-z0-9._-]{1,40}$"))) {
                        info("Informe um nome e uma versão válida.")
                    } else {
                        val all = readInstances()
                        val duplicate = (0 until all.length()).any { all.optJSONObject(it)?.optString("name")?.equals(name, true) == true }
                        if (duplicate) {
                            info("Já existe uma instância com esse nome.")
                        } else {
                            all.put(JSONObject().put("id", java.util.UUID.randomUUID().toString())
                                .put("name", name).put("version", version).put("createdAt", System.currentTimeMillis()))
                            prefs.edit().putString("instances_json", all.toString()).apply()
                            info("Instância criada. Para jogar, ainda falta integrar o motor Java.")
                            showPage("Instâncias")
                        }
                    }
                }.show()
        })

        val all = readInstances()
        if (all.length() == 0) {
            val empty = card()
            empty.addView(text("Ainda não há instâncias", 15f, Color.WHITE, true))
            empty.addView(text("Crie um perfil para organizar o nome e a versão do Minecraft.", 12f, muted))
            body.addView(empty)
            return
        }
        for (i in 0 until all.length()) {
            val instance = all.optJSONObject(i) ?: continue
            val id = instance.optString("id")
            val name = instance.optString("name", "Instância")
            val version = instance.optString("version", "desconhecida")
            val c = card()
            c.addView(text(name, 16f, Color.WHITE, true))
            c.addView(text("Minecraft $version", 12f, mint, true))
            c.addView(text("Perfil salvo neste dispositivo • inicialização ainda não integrada", 10f, muted))
            c.addView(actionButton("Definir versão selecionada") {
                prefs.edit().putString("selected_version", version).putString("selected_instance_id", id).apply()
                info("Versão $version selecionada.")
            })
            c.addView(actionButton("Excluir instância") {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Excluir $name?")
                    .setMessage("Isso remove apenas o perfil da instância, não apaga os arquivos baixados.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->
                        val updated = org.json.JSONArray()
                        val current = readInstances()
                        for (j in 0 until current.length()) {
                            val item = current.optJSONObject(j) ?: continue
                            if (item.optString("id") != id) updated.put(item)
                        }
                        prefs.edit().putString("instances_json", updated.toString()).apply()
                        showPage("Instâncias")
                    }.show()
            })
            body.addView(c)
        }
    }

    private fun versions() {
        pageHeading("Versões do Minecraft", "Todas as versões listadas no manifesto oficial, das antigas às atuais.")
        body.addView(actionButton("Carregar / atualizar versões") { loadVersions() })
        loadVersions()
    }

    private fun loadVersions() {
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        val channel = prefs.getString("version_channel", "all") ?: "all"
        val channelName = when (channel) {
            "release" -> "Release"
            "snapshot" -> "Snapshot"
            "old_beta" -> "Beta"
            "old_alpha" -> "Alfa"
            else -> "Todas as versões"
        }
        Thread {
            try {
                val conn = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").openConnection()
                conn.connectTimeout = 12000
                conn.readTimeout = 12000
                val json = JSONObject(conn.getInputStream().bufferedReader().use { it.readText() })
                val list = json.getJSONArray("versions")
                val versions = ArrayList<Pair<String, String>>()
                for (i in 0 until list.length()) {
                    val v = list.getJSONObject(i)
                    val type = v.optString("type", "unknown")
                    if (channel == "all" || type == channel) versions.add(v.getString("id") to type)
                }
                runOnUiThread {
                    if (currentPage != "Versões") return@runOnUiThread
                    body.removeAllViews()
                    pageHeading("Versões • $channelName", "${versions.size} versões no catálogo oficial")
                    val channels = listOf(
                        "Todas" to "all",
                        "Release" to "release",
                        "Snapshot" to "snapshot",
                        "Beta antiga" to "old_beta",
                        "Alfa antiga" to "old_alpha"
                    )
                    val picker = android.widget.Spinner(this).apply {
                        adapter = android.widget.ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, channels.map { it.first })
                        setSelection(channels.indexOfFirst { it.second == channel }.coerceAtLeast(0))
                        onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val next = channels[position].second
                                if (next != channel) {
                                    prefs.edit().putString("version_channel", next).apply()
                                    loadVersions()
                                }
                            }
                        }
                    }
                    body.addView(text("Mostrar tipo", 14f, Color.WHITE, true))
                    body.addView(picker, LinearLayout.LayoutParams(-1, dp(48)))
                    body.addView(actionButton("Atualizar catálogo") { loadVersions() })
                    if (versions.isEmpty()) body.addView(text("Não foram encontradas versões desse tipo.", 12f, muted))
                    versions.forEach { v ->
                        val c = card()
                        c.addView(text("Minecraft ${v.first}", 14f, Color.WHITE, true))
                        val typeLabel = when (v.second) {
                            "release" -> "Release"
                            "snapshot" -> "Snapshot / pré-lançamento"
                            "old_beta" -> "Beta antiga"
                            "old_alpha" -> "Alfa antiga"
                            else -> v.second
                        }
                        c.addView(text(typeLabel, 11f, muted))
                        c.addView(actionButton("Selecionar e baixar") {
                            prefs.edit().putString("selected_version", v.first).apply()
                            downloadMinecraftVersion(v.first)
                        })
                        body.addView(c)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    if (currentPage == "Versões") {
                        body.removeAllViews()
                        pageHeading("Versões do Minecraft", "Não foi possível carregar o catálogo. Verifique a internet.")
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

                // Download version libraries and assets using Mojang's official metadata.
                val minecraftRoot = File(filesDir, ".minecraft")
                val librariesRoot = File(minecraftRoot, "libraries")
                val libraries = metadata.optJSONArray("libraries")
                if (libraries != null) {
                    for (i in 0 until libraries.length()) {
                        val lib = libraries.optJSONObject(i) ?: continue
                        val downloads = lib.optJSONObject("downloads") ?: continue
                        val artifact = downloads.optJSONObject("artifact") ?: continue
                        val relativePath = artifact.optString("path", "")
                        val artifactUrl = artifact.optString("url", "")
                        if (relativePath.isBlank() || artifactUrl.isBlank()) continue
                        val destination = File(librariesRoot, relativePath)
                        if (!destination.exists() || !isVerifiedFile(destination, artifact.optString("sha1", ""), artifact.optLong("size", -1L))) {
                            downloadVerified(artifactUrl, destination, artifact.optString("sha1", ""), artifact.optLong("size", -1L))
                        }
                    }
                }

                val assetIndex = metadata.optJSONObject("assetIndex")
                if (assetIndex != null) {
                    val assetId = assetIndex.getString("id")
                    val indexesDir = File(minecraftRoot, "assets/indexes")
                    indexesDir.mkdirs()
                    val indexFile = File(indexesDir, "$assetId.json")
                    val indexUrl = assetIndex.getString("url")
                    if (!indexFile.exists() || !isVerifiedFile(indexFile, assetIndex.optString("sha1", ""), assetIndex.optLong("size", -1L))) {
                        downloadVerified(indexUrl, indexFile, assetIndex.optString("sha1", ""), assetIndex.optLong("size", -1L))
                    }
                    val objects = JSONObject(indexFile.readText()).optJSONObject("objects")
                    val objectRoot = File(minecraftRoot, "assets/objects")
                    if (objects != null) {
                        val keys = objects.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = objects.getJSONObject(key)
                            val hash = obj.getString("hash")
                            if (hash.length < 2) continue
                            val relative = hash.substring(0, 2) + "/" + hash
                            val destination = File(objectRoot, relative)
                            val size = obj.optLong("size", -1L)
                            if (!destination.exists() || !isVerifiedFile(destination, hash, size)) {
                                downloadVerified("https://resources.download.minecraft.net/$relative", destination, hash, size)
                            }
                        }
                    }
                }

                val versionPrefs = getSharedPreferences("endernova", MODE_PRIVATE)
                val installedVersions = try {
                    org.json.JSONArray(versionPrefs.getString("installed_versions_json", "[]") ?: "[]")
                } catch (_: Exception) { org.json.JSONArray() }
                var alreadyListed = false
                for (i in 0 until installedVersions.length()) {
                    if (installedVersions.optString(i) == versionId) alreadyListed = true
                }
                if (!alreadyListed) installedVersions.put(versionId)
                versionPrefs.edit()
                    .putString("installed_versions_json", installedVersions.toString())
                    .putString("selected_version", versionId)
                    .putBoolean("version_downloaded_" + versionId, true).apply()
                runOnUiThread {
                    info("Minecraft " + versionId + ": cliente, metadados, bibliotecas e assets baixados e verificados. O motor JVM e a preparação específica de natives ainda não estão integrados.")
                    showPage("Versões")
                }
            } catch (e: Exception) {
                runOnUiThread { info("Falha ao baixar " + versionId + ": " + (e.message ?: "erro de rede")) }
            }
        }.start()
    }

    private fun isVerifiedFile(file: File, expectedSha1: String, expectedSize: Long): Boolean {
        if (!file.isFile()) return false
        if (expectedSize > 0L && file.length() != expectedSize) return false
        if (expectedSha1.isBlank()) return expectedSize > 0L && file.length() == expectedSize
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val actual = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        return actual.equals(expectedSha1, ignoreCase = true)
    }

    private fun downloadVerified(address: String, destination: File, expectedSha1: String, expectedSize: Long) {
        destination.parentFile?.mkdirs()
        val temporary = File(destination.parentFile, destination.name + ".part")
        try {
            val connection = URL(address).openConnection()
            connection.connectTimeout = 20000
            connection.readTimeout = 120000
            connection.setRequestProperty("User-Agent", "EnderNova-Launcher")
            connection.getInputStream().use { input ->
                temporary.outputStream().use { output -> input.copyTo(output) }
            }
            if (!isVerifiedFile(temporary, expectedSha1, expectedSize)) {
                throw IllegalStateException("Verificação do arquivo falhou: " + destination.name)
            }
            if (!temporary.renameTo(destination)) {
                temporary.copyTo(destination, overwrite = true)
                temporary.delete()
            }
        } catch (e: Exception) {
            temporary.delete()
            throw e
        }
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
        pageHeading("Loja de Mods", "Pesquisa real no Modrinth. O mod precisa ser compatível com a versão e o loader.")
        val query = android.widget.EditText(this).apply {
            hint = "Buscar mods (ex.: minimap, performance)"
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            background = rounded(panel, 10, mint)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        body.addView(query, LinearLayout.LayoutParams(-1, dp(48)))
        body.addView(actionButton("Pesquisar no Modrinth") { searchModrinth(query.text.toString().trim()) })
        val selectedVersion = getSharedPreferences("endernova", MODE_PRIVATE).getString("selected_version", null)
        body.addView(text("Versão-alvo: " + (selectedVersion ?: "não selecionada"), 12f, muted))
        body.addView(text("Arquivos são salvos em .minecraft/mods. Isso não instala Forge/Fabric nem garante compatibilidade.", 11f, muted))
    }

    private fun searchModrinth(query: String) {
        if (query.isBlank()) { info("Digite o nome ou o tipo de mod para pesquisar."); return }
        info("Pesquisando mods no Modrinth…")
        Thread {
            try {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = URL("https://api.modrinth.com/v2/search?query=" + encoded + "&facets=%5B%5B%22project_type%3Amod%22%5D%5D&limit=20")
                val conn = url.openConnection()
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "EnderNovaLauncher/1.0 (Android)")
                val results = JSONObject(conn.getInputStream().bufferedReader().use { it.readText() }).getJSONArray("hits")
                runOnUiThread {
                    if (currentPage != "Loja de Mods") return@runOnUiThread
                    body.removeAllViews()
                    pageHeading("Resultados do Modrinth", results.length().toString() + " projetos encontrados")
                    body.addView(actionButton("Nova pesquisa") { showPage("Loja de Mods") })
                    for (i in 0 until results.length()) {
                        val project = results.getJSONObject(i)
                        val title = project.optString("title", project.optString("slug", "Mod"))
                        val projectId = project.optString("project_id", "")
                        val description = project.optString("description", "")
                        val c = card()
                        c.addView(text(title, 15f, Color.WHITE, true))
                        c.addView(text(description, 11f, muted))
                        c.addView(actionButton("Baixar versão compatível") { downloadModrinthProject(projectId, title) })
                        body.addView(c)
                    }
                }
            } catch (e: Exception) { runOnUiThread { info("Falha na pesquisa Modrinth: " + (e.message ?: "erro de rede")) } }
        }.start()
    }

    private fun downloadModrinthProject(projectId: String, title: String) {
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        val gameVersion = prefs.getString("selected_version", null)
        if (gameVersion.isNullOrBlank()) { info("Primeiro selecione uma versão na aba Versões."); return }
        info("Buscando arquivo compatível de " + title + "…")
        Thread {
            try {
                val encodedGame = java.net.URLEncoder.encode("[\"" + gameVersion + "\"]", "UTF-8")
                val url = URL("https://api.modrinth.com/v2/project/" + projectId + "/version?game_versions=" + encodedGame)
                val conn = url.openConnection()
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "EnderNovaLauncher/1.0 (Android)")
                val versions = org.json.JSONArray(conn.getInputStream().bufferedReader().use { it.readText() })
                if (versions.length() == 0) throw IllegalStateException("Nenhuma versão declara compatibilidade com Minecraft " + gameVersion)
                val chosen = versions.getJSONObject(0)
                val files = chosen.getJSONArray("files")
                var file: JSONObject? = null
                for (i in 0 until files.length()) { val candidate = files.getJSONObject(i); if (candidate.optBoolean("primary", false)) { file = candidate; break } }
                if (file == null && files.length() > 0) file = files.getJSONObject(0)
                val selected = file ?: throw IllegalStateException("O projeto não oferece arquivo para baixar.")
                val fileName = File(selected.getString("filename")).name
                if (!fileName.endsWith(".jar", ignoreCase = true)) throw IllegalStateException("O arquivo principal não é um JAR de mod.")
                val expectedHash = selected.optJSONObject("hashes")?.optString("sha512", "") ?: ""
                val targetDir = File(filesDir, ".minecraft/mods/" + gameVersion)
                targetDir.mkdirs()
                val target = File(targetDir, fileName)
                val temp = File(targetDir, fileName + ".part")
                val download = URL(selected.getString("url")).openConnection()
                download.connectTimeout = 20000
                download.readTimeout = 120000
                download.setRequestProperty("User-Agent", "EnderNovaLauncher/1.0 (Android)")
                download.getInputStream().use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
                if (expectedHash.isNotBlank()) {
                    val digest = MessageDigest.getInstance("SHA-512")
                    temp.inputStream().use { input -> val buffer = ByteArray(8192); while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) } }
                    val actual = digest.digest().joinToString("") { b -> "%02x".format(b) }
                    if (!actual.equals(expectedHash, true)) throw IllegalStateException("O hash do arquivo não confere.")
                }
                if (!temp.renameTo(target)) { temp.copyTo(target, overwrite = true); temp.delete() }
                runOnUiThread { info("Mod baixado e verificado: " + fileName + ". Ele só será carregado quando um loader compatível estiver instalado.") }
            } catch (e: Exception) { runOnUiThread { info("Não foi possível instalar " + title + ": " + (e.message ?: "erro de rede")) } }
        }.start()
    }

    private fun accounts() {
        pageHeading("Perfis", "Perfil offline local; contas online exigem OAuth oficial.")
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        val savedName = prefs.getString("player_name", "") ?: ""
        val input = android.widget.EditText(this).apply {
            hint = "Nome offline (3–16 caracteres)"
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            setText(savedName)
            background = rounded(panel, 10, mint)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        body.addView(text("Perfil offline", 16f, Color.WHITE, true))
        body.addView(input, LinearLayout.LayoutParams(-1, dp(48)))
        body.addView(actionButton("Salvar perfil offline") {
            val name = input.text.toString().trim()
            if (!name.matches(Regex("^[A-Za-z0-9_]{3,16}$"))) {
                info("Use de 3 a 16 caracteres: letras, números ou sublinhado.")
            } else {
                prefs.edit().putString("player_name", name).putString("account_type", "offline").apply()
                info("Perfil offline '$name' salvo neste dispositivo.")
            }
        })
        body.addView(space(10))
        listOf("Microsoft" to "Login OAuth e validação do perfil Minecraft ainda não integrados.", "Ely.by" to "OAuth Ely.by ainda não integrado; requer configuração oficial do aplicativo.").forEach {
            val c = card()
            c.addView(text(it.first, 16f, Color.WHITE, true))
            c.addView(text(it.second, 11f, muted))
            c.addView(actionButton("Indisponível nesta versão") {
                info("Este login ainda não funciona. Não insira senha ou token aqui.")
            })
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
        body.addView(actionButton("Testar conexão com a internet") {
            info("Testando conexão…")
            Thread {
                val result = try {
                    val conn = URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json").openConnection()
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    conn.getInputStream().use { it.read() }
                    "Conexão com o catálogo oficial do Minecraft OK."
                } catch (e: Exception) {
                    "Falha de conexão: " + (e.message ?: "verifique a internet")
                }
                runOnUiThread { info(result) }
            }.start()
        })
        body.addView(actionButton("Ver contas") { showPage("Contas") })
        body.addView(actionButton("Ver diagnóstico") { showPage("Diagnóstico") })
        body.addView(actionButton("Refazer configuração inicial") {
            prefs.edit().putBoolean("setup_done", false).apply()
            showPage("Primeiros passos")
        })
        body.addView(actionButton("Limpar preferências") {
            val confirm = android.widget.CheckBox(this).apply {
                text = "Também apagar preferências e perfil local"
                setTextColor(Color.WHITE)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                isChecked = false
            }
            val dialogContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(8), dp(20), dp(4))
                addView(text("Isso remove nome e configurações do launcher. Os arquivos baixados não serão apagados.", 14f, Color.WHITE))
                addView(confirm)
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Confirmar limpeza")
                .setView(dialogContent)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar") { _, _ ->
                    if (confirm.isChecked) {
                        prefs.edit().clear().apply()
                        info("Preferências locais apagadas.")
                    } else {
                        info("Marque a confirmação para apagar as preferências.")
                    }
                    showPage("Configurações")
                }
                .show()
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
        pageHeading("Otimização", "Salva preferências para uso futuro; não altera o jogo até o motor estar integrado.")
        val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
        listOf(
            "Reduzir animações do launcher" to "Menos movimento na interface",
            "Modo econômico" to "Preferência para reduzir efeitos visuais quando suportado",
            "Limitar FPS a 60" to "Limite desejado para o jogo",
            "Reduzir partículas" to "Preferência gráfica para o jogo",
            "Distância de renderização baixa" to "Preferência para economizar memória"
        ).forEach { item ->
            val key = "optimization_" + item.first
            val toggle = android.widget.Switch(this).apply {
                text = item.first
                setTextColor(Color.WHITE)
                isChecked = prefs.getBoolean(key, item.first == "Reduzir animações do launcher")
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean(key, checked).apply()
                    if (item.first == "Reduzir animações do launcher") {
                        prefs.edit().putBoolean("setting_Animações da interface", !checked).apply()
                    }
                }
            }
            body.addView(toggle)
            body.addView(text(item.second, 11f, muted))
        }
        body.addView(actionButton("Ver arquivos de mods baixados") { showDownloadedMods() })
    }

    private fun showDownloadedMods() {
        val root = File(filesDir, ".minecraft/mods")
        val mods = root.walkTopDown().filter { it.isFile && it.extension.equals("jar", true) }.toList()
        body.removeAllViews()
        pageHeading("Mods baixados", mods.size.toString() + " arquivos JAR guardados")
        body.addView(actionButton("Voltar à otimização") { showPage("Otimização") })
        if (mods.isEmpty()) { body.addView(text("Nenhum mod baixado ainda.", 13f, muted)); return }
        mods.forEach { file ->
            val c = card()
            c.addView(text(file.name, 14f, Color.WHITE, true))
            c.addView(text(file.parentFile?.name ?: "", 11f, muted))
            c.addView(actionButton("Remover arquivo") {
                android.app.AlertDialog.Builder(this).setTitle("Remover mod?")
                    .setMessage("O arquivo será removido do armazenamento do launcher.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Remover") { _, _ ->
                        if (file.delete()) info("Arquivo removido.") else info("Não foi possível remover o arquivo.")
                        showDownloadedMods()
                    }.show()
            })
            body.addView(c)
        }
    }

    private fun crashDiagnostics() {
        pageHeading("Diagnóstico e crashes", "Relatórios locais do launcher e do Minecraft, quando existirem.")
        val root = File(filesDir, ".minecraft")
        val reports = File(root, "crash-reports")
        val latestReport = reports.listFiles()?.filter { it.isFile && it.extension == "txt" }?.maxByOrNull { it.lastModified() }
        val latestLog = File(root, "logs/latest.log")
        val selected = latestReport?.takeIf { it.exists() } ?: latestLog.takeIf { it.exists() }
        val reportText = if (selected != null) {
            try { selected.readText().takeLast(60000) } catch (e: Exception) { "Não foi possível ler o relatório: " + (e.message ?: "erro") }
        } else {
            "Nenhum relatório encontrado ainda.\n\nQuando o motor do Minecraft estiver integrado, o launcher poderá acompanhar a execução e apresentar o erro aqui. Por enquanto, esta tela procura relatórios já existentes no diretório privado do aplicativo."
        }
        body.addView(text(if (latestReport != null) "Último crash: " + latestReport.name else if (latestLog.exists()) "Último log: latest.log" else "Nenhum crash detectado", 14f, mint, true))
        body.addView(actionButton("Copiar relatório") {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Relatório EnderNova", reportText))
            info("Relatório copiado. Você pode colar e enviar para análise.")
        })
        body.addView(actionButton("Atualizar relatório") { showPage("Diagnóstico") })
        val logBox = android.widget.TextView(this).apply {
            text = reportText
            textSize = 11f
            setTextColor(Color.rgb(210, 230, 220))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.rgb(2, 8, 7), 10, Color.rgb(19, 62, 49))
            setTextIsSelectable(true)
        }
        body.addView(logBox, LinearLayout.LayoutParams(-1, dp(360)))
        body.addView(text("Observação: o início do jogo e a captura automática da saída do processo ainda dependem da integração do motor Java.", 11f, muted))
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
        c.isClickable = true
        c.isFocusable = true
        c.addView(text(title, 14f, Color.WHITE, true))
        c.addView(text(detail, 11f, muted))
        c.addView(text("Toque para abrir", 10f, mint, true))
        c.setOnClickListener {
            when (title) {
                "Aparência" -> {
                    val options = arrayOf("Verde e preto (padrão)", "Animações ligadas", "Animações desligadas")
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Aparência")
                        .setItems(options) { _, which ->
                            val prefs = getSharedPreferences("endernova", MODE_PRIVATE)
                            when (which) {
                                0 -> info("O tema verde e preto já está ativo.")
                                1 -> {
                                    prefs.edit().putBoolean("setting_Animações da interface", true)
                                        .putBoolean("optimization_Reduzir animações do launcher", false).apply()
                                    info("Animações ativadas.")
                                    showPage("Configurações")
                                }
                                2 -> {
                                    prefs.edit().putBoolean("setting_Animações da interface", false)
                                        .putBoolean("optimization_Reduzir animações do launcher", true).apply()
                                    info("Animações desativadas.")
                                    showPage("Configurações")
                                }
                            }
                        }.setNegativeButton("Fechar", null).show()
                }
                "Contas" -> showPage("Contas")
                "Sobre" -> android.app.AlertDialog.Builder(this)
                    .setTitle("EnderNova Launcher")
                    .setMessage("Launcher Android para Minecraft Java.\n\nAlgumas funções ainda dependem da integração do motor Java e precisam ser testadas.\n\nVersão experimental.")
                    .setPositiveButton("OK", null).show()
                else -> info(detail)
            }
        }
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
