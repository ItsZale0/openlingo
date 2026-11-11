package com.alessandro.tedesco.data.remote

import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Regressione auto-update: la fallback /contents/version.json (base64) deve
 * restituire la stessa VersionInfo del raw, altrimenti il controllo fallisce.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VersioniFallbackTest {

    private val vm = UpdaterViewModel(ApplicationProvider.getApplicationContext())

    private fun rispostaApi(versionCode: Int): String {
        val versionJson = """
            {
                "versionCode": $versionCode,
                "versionName": "1.9.9",
                "apkUrl": "https://github.com/ItsZale0/openlingo/releases/download/v1.9.9/Tedesco-v1.9.9.apk",
                "changelog": "Fix",
                "minVersionCode": 1
            }
        """.trimIndent()
        return """{"name":"version.json","path":"version.json","content":"${
            Base64.encodeToString(versionJson.toByteArray(), Base64.NO_WRAP).replace("\n", "")
        }"}"""
    }

    @Test
    fun `decodifica contenuto base64 dell API GitHub`() {
        val info = vm.decodificaContenutoApi(rispostaApi(182))

        assertEquals(182, info.versionCode)
        assertEquals("1.9.9", info.versionName)
        assertEquals(
            "https://github.com/ItsZale0/openlingo/releases/download/v1.9.9/Tedesco-v1.9.9.apk",
            info.apkUrl
        )
    }

    @Test
    fun `decodifica con newline nel base64`() {
        val versionJson = """{"versionCode":1,"versionName":"1.0.0","apkUrl":"u","minVersionCode":1}"""
        val base64 = Base64.encodeToString(versionJson.toByteArray(), Base64.DEFAULT)
        val risposta = """{"content":"$base64"}"""

        assertEquals("1.0.0", vm.decodificaContenutoApi(risposta).versionName)
    }

    @Test
    fun `con cache CDN vecchia prende la versione piu recente`() {
        val vecchia = vm.decodificaContenutoApi(rispostaApi(181))
        val nuova = vm.decodificaContenutoApi(rispostaApi(182))

        val recente = vm.versionePiuRecente(listOf(vecchia, nuova))
        assertEquals(182, recente?.versionCode)
        val recenteAlContrario = vm.versionePiuRecente(listOf(nuova, vecchia))
        assertEquals(182, recenteAlContrario?.versionCode)
        val sola = vm.versionePiuRecente(listOf(vecchia))
        assertEquals(181, sola?.versionCode)
        org.junit.Assert.assertNull(vm.versionePiuRecente(emptyList()))
    }

    @Test
    fun `campo content assente lancia eccezione`() {
        assertThrows(java.io.IOException::class.java) {
            vm.decodificaContenutoApi("""{"name":"version.json"}""")
        }
    }
}
