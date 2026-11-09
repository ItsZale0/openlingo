package com.alessandro.tedesco.data.remote

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String = "",
    val minVersionCode: Int = 0,
    /**
     * SHA-256 esadecimale dell'APK pubblicato.
     *
     * Se presente, il download viene accettato solo se l'hash coincide: un
     * manifest o un trasporto manomesso non basta più a installare un file
     * arbitrario. Vuoto = verifica non disponibile (compatibilità con i
     * manifest già pubblicati).
     */
    val apkSha256: String = ""
)

/**
 * Verifica pura dell'integrità di un file APK.
 * Usata sia dal ViewModel sia dai test.
 */
object VerificaApk {

    private const val DIMENSIONE_MINIMA = 1_000_000L // 1 MB

    /**
     * Verifica che un file sia un APK valido controllando:
     * - dimensione > 1 MB
     * - primi 2 byte = 'P' 'K' (magic ZIP/APK)
     */
    fun isApkValido(file: File): Boolean = isApkValidoBase(file)

    /**
     * Verifica APK + hash SHA-256 atteso.
     *
     * @param shaAtteso hash esadecimale (minuscolo o maiuscolo, con o senza
     *   prefisso "sha256:"). Vuoto = salta il controllo dell'hash.
     */
    fun isApkValido(file: File, shaAtteso: String): Boolean {
        if (!isApkValidoBase(file)) return false
        val atteso = shaAtteso.trim().lowercase().removePrefix("sha256:")
        if (atteso.isEmpty()) return true
        val calcolato = sha256(file) ?: return false
        return calcolato == atteso
    }

    /** Controllo base: esistenza, dimensione > 1 MB, header PK. */
    private fun isApkValidoBase(file: File): Boolean {
        if (!file.exists()) return false
        if (file.length() < DIMENSIONE_MINIMA) return false
        val header = ByteArray(2)
        return try {
            file.inputStream().use { it.read(header) }
            header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
        } catch (e: Exception) {
            false
        }
    }

    /** SHA-256 esadecimale del file, oppure null in caso di errore di lettura. */
    fun sha256(file: File): String? = try {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        null
    }

    /**
     * Versione pura per test: verifica direttamente header e dimensione.
     */
    fun isApkValido(header: ByteArray, size: Long): Boolean {
        if (size < DIMENSIONE_MINIMA) return false
        if (header.size < 2) return false
        return header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
    }
}
