package com.alessandro.openlingo.sync

import android.content.Context
import com.alessandro.openlingo.data.BackupCloud
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar

private val Context.notificheStore by preferencesDataStore(name = "notifiche_settings")

/**
 * Sistema di notifiche: due promemoria giornalieri, entrambi 100% locali
 * (WorkManager, nessuna rete).
 *
 * - ripasso: il worker [ReminderWorker] già esistente, ora controllabile
 *   dall'utente e programmato a un'ora fissa invece che "1 minuto dopo l'avvio".
 * - ascolto passivo serale: [ImmersioneSeraleWorker], il "bonus pre-sonno" del
 *   metodo: gira solo se il profilo attivo ha la Modalità Immersione.
 */
class NotificheSettings(private val context: Context) {

    suspend fun ripassoAttivo(): Boolean =
        context.notificheStore.data.first()[RIPASSO_ATTIVO] ?: true

    suspend fun ripassoOra(): Int =
        context.notificheStore.data.first()[RIPASSO_ORA] ?: 20

    suspend fun immersioneSeraleAttiva(): Boolean =
        context.notificheStore.data.first()[IMMERSIONE_ATTIVA] ?: false

    suspend fun immersioneSeraleOra(): Int =
        context.notificheStore.data.first()[IMMERSIONE_ORA] ?: 21

    suspend fun setRipasso(attivo: Boolean, ora: Int? = null) {
        context.notificheStore.edit {
            it[RIPASSO_ATTIVO] = attivo
            ora?.let { o -> it[RIPASSO_ORA] = o }
        }
    }

    suspend fun setImmersioneSerale(attiva: Boolean, ora: Int? = null) {
        context.notificheStore.edit {
            it[IMMERSIONE_ATTIVA] = attiva
            ora?.let { o -> it[IMMERSIONE_ORA] = o }
        }
    }

    suspend fun leggi(): BackupCloud.NotificheBackup = BackupCloud.NotificheBackup(
        ripassoAttivo = ripassoAttivo(),
        ripassoOra = ripassoOra(),
        immersioneSeraleAttiva = immersioneSeraleAttiva(),
        immersioneSeraleOra = immersioneSeraleOra()
    )

    suspend fun applica(b: BackupCloud.NotificheBackup) {
        setRipasso(b.ripassoAttivo, b.ripassoOra)
        setImmersioneSerale(b.immersioneSeraleAttiva, b.immersioneSeraleOra)
    }

    companion object {
        val RIPASSO_ATTIVO = booleanPreferencesKey("promemoria_ripasso_attivo")
        val RIPASSO_ORA = intPreferencesKey("promemoria_ripasso_ora")
        val IMMERSIONE_ATTIVA = booleanPreferencesKey("promemoria_immersione_attivo")
        val IMMERSIONE_ORA = intPreferencesKey("promemoria_immersione_ora")

        /**
         * Millisecondi che mancano alla prossima occorrenza dell'ora [ora]
         * (fuso del dispositivo). Se l'ora di oggi è già passata, conta quella
         * di domani. [adesso] è iniettabile per i test.
         */
        fun ritardoFinoAlle(ora: Int, adesso: Calendar = Calendar.getInstance()): Long {
            val obiettivo = adesso.clone() as Calendar
            obiettivo.set(Calendar.HOUR_OF_DAY, ora.coerceIn(0, 23))
            obiettivo.set(Calendar.MINUTE, 0)
            obiettivo.set(Calendar.SECOND, 0)
            obiettivo.set(Calendar.MILLISECOND, 0)
            if (!obiettivo.after(adesso)) obiettivo.add(Calendar.HOUR_OF_DAY, 24)
            return obiettivo.timeInMillis - adesso.timeInMillis
        }
    }
}
