package com.alessandro.openlingo.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionInfoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserializza versione reale di version json`() {
        val jsonReale = """
            {
                "versionCode": 8,
                "versionName": "1.6.1",
                "apkUrl": "https://github.com/ItsZale0/openlingo/releases/download/v1.6.1/Tedesco-v1.6.1.apk",
                "changelog": "Bugfix e miglioramenti",
                "minVersionCode": 1
            }
        """.trimIndent()

        val info = json.decodeFromString<VersionInfo>(jsonReale)

        assertEquals(8, info.versionCode)
        assertEquals("1.6.1", info.versionName)
        assertEquals(
            "https://github.com/ItsZale0/openlingo/releases/download/v1.6.1/Tedesco-v1.6.1.apk",
            info.apkUrl
        )
        assertEquals(1, info.minVersionCode)
    }

    @Test
    fun `ignora campi sconosciuti nel JSON`() {
        val jsonConExtra = """
            {
                "versionCode": 9,
                "versionName": "2.0.0",
                "apkUrl": "https://example.com/app.apk",
                "changelog": "Nuove funzionalità",
                "minVersionCode": 2,
                "campoSconosciuto": "valore",
                    "altroExtra": 123
            }
        """.trimIndent()

        val info = json.decodeFromString<VersionInfo>(jsonConExtra)

        assertEquals(9, info.versionCode)
        assertEquals("2.0.0", info.versionName)
        assertEquals(2, info.minVersionCode)
    }

    @Test(expected = kotlinx.serialization.SerializationException::class)
    fun `JSON malformato lancia eccezione`() {
        val jsonMalformato = """{ "versionCode": "otto", "versionName": }"""

        json.decodeFromString<VersionInfo>(jsonMalformato)
    }

    @Test
    fun `VerificaApk riconosce APK valido`() {
        val headerValido = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        val sizeGrande = 18_543_523L

        assertTrue(VerificaApk.isApkValido(headerValido, sizeGrande))
    }

    @Test
    fun `VerificaApk rifiuta header non PK`() {
        val headerNonPk = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val sizeGrande = 18_543_523L

        assertFalse(VerificaApk.isApkValido(headerNonPk, sizeGrande))
    }

    @Test
    fun `VerificaApk rifiuta dimensione troppo piccola`() {
        val headerValido = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        val sizePiccola = 500_000L

        assertFalse(VerificaApk.isApkValido(headerValido, sizePiccola))
    }

    @Test
    fun `deserializza apkSha256 quando presente`() {
        val conHash = """
            {
                "versionCode": 306,
                "versionName": "1.48.73",
                "apkUrl": "https://example.com/Deutsch-v1.48.73.apk",
                "apkSha256": "ABCDEF0123456789"
            }
        """.trimIndent()

        val info = json.decodeFromString<VersionInfo>(conHash)

        assertEquals("ABCDEF0123456789", info.apkSha256)
    }

    @Test
    fun `sha256 di un file corrisponde al digest atteso`() {
        val file = java.io.File.createTempFile("verifica-apk", ".bin")
        try {
            file.writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, 0x7F, 0x22))
            val atteso = java.security.MessageDigest.getInstance("SHA-256")
                .digest(file.readBytes())
                .joinToString("") { "%02x".format(it) }

            assertEquals(atteso, VerificaApk.sha256(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `version json minimale senza changelog ne minVersionCode`() {
        // Regressione: il version.json pubblicato conteneva solo 3 campi,
        // la deserializzazione falliva e l'auto-updater non vedeva mai l'update.
        val jsonMinimale = """
            {
                "versionCode": 109,
                "versionName": "1.35.1",
                "apkUrl": "https://github.com/ItsZale0/openlingo/releases/download/v1.35.1/Tedesco-v1.35.1.apk"
            }
        """.trimIndent()

        val info = json.decodeFromString<VersionInfo>(jsonMinimale)

        assertEquals(109, info.versionCode)
        assertEquals("1.35.1", info.versionName)
        assertEquals("", info.changelog)
        assertEquals(0, info.minVersionCode)
    }
}
