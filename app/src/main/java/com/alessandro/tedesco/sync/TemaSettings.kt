package com.alessandro.tedesco.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.temaStore by preferencesDataStore(name = "tema_settings")

class TemaSettings(private val context: Context) {
    val colore: Flow<String> = context.temaStore.data.map { it[COLORE] ?: "teal" }
    val modalita: Flow<String> = context.temaStore.data.map { it[MODALITA] ?: "system" }

    suspend fun setColore(id: String) {
        context.temaStore.edit { it[COLORE] = id }
    }

    suspend fun setModalita(id: String) {
        context.temaStore.edit { it[MODALITA] = id }
    }

    companion object {
        val COLORE = stringPreferencesKey("tema_colore")
        val MODALITA = stringPreferencesKey("tema_modalita")
        val OPZIONI_MODALITA = listOf(
            "system" to "Sistema",
            "light" to "Chiaro",
            "dark" to "Scuro"
        )
        val OPZIONI = listOf(
            "teal" to "Teal",
            "navy" to "Blu navy",
            "indigo" to "Indaco",
            "forest" to "Verde",
            "rose" to "Rosa",
            "amber" to "Ambra",
            "purple" to "Viola",
            "pink" to "Rosa acceso",
            "orange" to "Arancione",
            "cyan" to "Ciano",
            "red" to "Rosso",
            "material" to "Material 3 (base)",
            "material_you" to "Material You (dinamico)"
        )
    }
}