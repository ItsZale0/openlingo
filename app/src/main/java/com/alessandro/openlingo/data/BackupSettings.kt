package com.alessandro.openlingo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val BACKUP_DATASTORE = "backup_cloud"
private val Context.backupDataStore by preferencesDataStore(name = BACKUP_DATASTORE)

private object BackupKeys {
    val TOKEN = stringPreferencesKey("github_token")
    val REPO = stringPreferencesKey("github_repo")
    val ULTIMO_BACKUP = stringPreferencesKey("ultimo_backup_ts")
    val AUTO = booleanPreferencesKey("auto_abilitato")
    val ULTIMO_HASH = stringPreferencesKey("ultimo_hash")
    val ULTIMO_AUTO = stringPreferencesKey("ultimo_auto_ts")
}

/**
 * Impostazioni del backup cloud, GLOBALI (non per profilo): il token e il repo
 * valgono per tutta l'app, e il backup contiene tutti i profili.
 */
class BackupSettings(private val context: Context) {

    val tokenFlow: Flow<String> = context.backupDataStore.data.map { it[BackupKeys.TOKEN] ?: "" }
    val repoFlow: Flow<String> = context.backupDataStore.data.map { it[BackupKeys.REPO] ?: "" }
    val ultimoBackupFlow: Flow<Long> = context.backupDataStore.data.map {
        it[BackupKeys.ULTIMO_BACKUP]?.toLongOrNull() ?: 0L
    }

    suspend fun salvaToken(token: String) {
        context.backupDataStore.edit { it[BackupKeys.TOKEN] = token.trim() }
    }

    suspend fun salvaRepo(repo: String) {
        context.backupDataStore.edit { it[BackupKeys.REPO] = repo.trim() }
    }

    suspend fun registraBackup(ts: Long) {
        context.backupDataStore.edit { it[BackupKeys.ULTIMO_BACKUP] = ts.toString() }
    }

    // ---- Backup automatico ----

    val autoFlow: Flow<Boolean> = context.backupDataStore.data.map { it[BackupKeys.AUTO] ?: true }
    val ultimoAutoFlow: Flow<Long> = context.backupDataStore.data.map {
        it[BackupKeys.ULTIMO_AUTO]?.toLongOrNull() ?: 0L
    }

    suspend fun salvaAuto(abilitato: Boolean) {
        context.backupDataStore.edit { it[BackupKeys.AUTO] = abilitato }
    }

    suspend fun autoAbilitato(): Boolean =
        context.backupDataStore.data.map { it[BackupKeys.AUTO] ?: true }.first()

    /** Hash del contenuto dell'ultimo backup: evita di ricaricare se nulla è cambiato. */
    suspend fun ultimoHash(): String =
        context.backupDataStore.data.map { it[BackupKeys.ULTIMO_HASH] ?: "" }.first()

    suspend fun salvaHash(hash: String) {
        context.backupDataStore.edit { it[BackupKeys.ULTIMO_HASH] = hash }
    }

    suspend fun registraAuto(ts: Long) {
        context.backupDataStore.edit { it[BackupKeys.ULTIMO_AUTO] = ts.toString() }
    }
}
