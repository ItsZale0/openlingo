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

    suspend fun setColore(id: String) {
        context.temaStore.edit { it[COLORE] = id }
    }

    companion object {
        val COLORE = stringPreferencesKey("tema_colore")
        val OPZIONI = listOf(
            "teal" to "Teal",
            "navy" to "Blu navy",
            "indigo" to "Indaco",
            "forest" to "Verde",
            "rose" to "Rosa",
            "amber" to "Ambra"
        )
    }
}