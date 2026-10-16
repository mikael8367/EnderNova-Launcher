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
        currentPage = "Início"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        root.addView(makeHeader())
        val scroll = ScrollView(this).apply { fillViewport = true }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(18))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(makeBottomNav())
        setContentView(root)
        showPage("Início")
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
        instanceRow("Minha Série", "Minecraft 1.12.2 • Forge", "▶")
        instanceRow("Vanilla", "Minecraft 1.20.4 • Vanilla", "▶")
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
        instanceRow("Minha Série", "Minecraft 1.12.2 • Forge", "⋮")
        instanceRow("Vanilla", "Minecraft 1.20.4 • Vanilla", "⋮")
        body.addView(actionButton("+  Nova instância") { info("Criação real de instâncias será ligada ao instalador.") })
    }

    private fun versions() {
        pageHeading("Versões do Minecraft", "Catálogo e opções de instalação.")
        listOf("1.20.4" to "Lançamento oficial", "1.19.4" to "Lançamento oficial", "1.18.2" to "Lançamento oficial", "1.12.2" to "Versão selecionada • Java 8").forEach {
            val c = card()
            c.addView(text("▦  Minecraft ${it.first}", 15f, Color.WHITE, true))
            c.addView(text(it.second, 11f, muted))
            c.addView(actionButton("Selecionar") { info("Versão ${it.first} selecionada na prévia. A instalação completa ainda não está disponível.") })
            body.addView(c)
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
        pageHeading("Configurações", "Preferências do launcher.")
        settingRow("Aparência", "Tema escuro e verde")
        settingRow("Memória RAM", "2 GB • preferência visual")
        settingRow("Java", "Runtime ainda não integrado")
        settingRow("Limpar dados", "Histórico e preferências locais")
        settingRow("Exportar configurações", "Salvar / restaurar")
        settingRow("Sobre", "EnderNova Launcher • Mikael Dev")
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
