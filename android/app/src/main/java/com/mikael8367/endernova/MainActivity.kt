package com.mikael8367.endernova

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Native foundation for EnderNova.
 * This deliberately does not pretend Minecraft can launch yet: a compatible ARM64
 * JVM and a game-launch integration still need to be integrated and tested.
 */
class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private val green = Color.rgb(55, 230, 160)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10, 13, 18)
        window.navigationBarColor = Color.rgb(10, 13, 18)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(Color.rgb(10, 13, 18))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val title = TextView(this).apply {
            text = "ENDERNOVA"
            textSize = 30f
            setTextColor(green)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Minecraft Java • Android ARM64"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }
        status = TextView(this).apply {
            text = "Preparado. Verifique a conexão para consultar os metadados oficiais."
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
        }
        val check = Button(this).apply {
            text = "Verificar Minecraft 1.12.2"
            setOnClickListener { checkVersion() }
        }
        val note = TextView(this).apply {
            text = "Estado do motor\n• Manifesto e metadados: consulta de teste\n• Bibliotecas e assets: instalador ainda não implementado\n• Runtime Java ARM64: ainda não integrado\n• Iniciar jogo: indisponível até integrar e testar o runtime"
            textSize = 14f
            setTextColor(Color.rgb(190, 198, 210))
            setPadding(0, 24, 0, 0)
        }
        content.addView(title)
        content.addView(subtitle)
        content.addView(status, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        content.addView(check, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        content.addView(note)
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun checkVersion() {
        status.text = "Consultando manifesto oficial do Minecraft..."
        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
                    .openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 12000
                    requestMethod = "GET"
                }
                val code = connection.responseCode
                if (code !in 200..299) error("HTTP $code")
                val manifest = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val versions = manifest.getJSONArray("versions")
                var found = false
                var releaseType = ""
                var versionUrl = ""
                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)
                    if (item.optString("id") == "1.12.2") {
                        found = true
                        releaseType = item.optString("type")
                        versionUrl = item.optString("url")
                        break
                    }
                }
                if (!found || versionUrl.isBlank()) error("Versão 1.12.2 não encontrada no manifesto")
                runOnUiThread {
                    status.text = "Metadados encontrados: Minecraft 1.12.2 ($releaseType).\nURL do JSON de versão validada. Nenhum arquivo do jogo foi baixado."
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "Não foi possível consultar os metadados: ${e.message ?: "erro de rede"}"
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
