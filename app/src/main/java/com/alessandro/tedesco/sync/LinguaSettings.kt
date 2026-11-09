package com.alessandro.tedesco.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.linguaStore by preferencesDataStore(name = "lingua_settings")

class LinguaSettings(private val context: Context) {
    val lingua: Flow<String> = context.linguaStore.data.map { it[LINGUA] ?: defaultLingua() }

    suspend fun setLingua(id: String) {
        context.linguaStore.edit { it[LINGUA] = id }
    }

    companion object {
        val LINGUA = stringPreferencesKey("lingua")
        val OPZIONI = listOf(
            "it" to "Italiano",
            "en" to "English"
        )

        private fun defaultLingua(): String =
            if (java.util.Locale.getDefault().language == "en") "en" else "it"
    }
}
