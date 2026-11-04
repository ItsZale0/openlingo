package com.alessandro.tedesco.desktop

import java.util.Properties

/**
 * Versione dell'app, letta da `version.properties`: lo stesso file che usa
 * l'app Android, quindi le due versioni non possono divergere.
 */
object Versione {

    private val props: Properties by lazy {
        val p = Properties()
        runCatching {
            object {}.javaClass.classLoader
                ?.getResourceAsStream("version.properties")
                ?.use { p.load(it) }
        }
        p
    }

    /** Solo il nome, es. "1.48.64". */
    val nome: String get() = props.getProperty("VERSION_NAME", "0.0.0")

    /** Nome con build, es. "1.48.64 (build 297)". */
    val conBuild: String
        get() = "$nome (build ${props.getProperty("VERSION_CODE", "0")})"
}
