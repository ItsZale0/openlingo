package com.alessandro.openlingo.di
import android.content.Context
import com.alessandro.openlingo.data.ProfileManager
import com.alessandro.openlingo.data.WordRepository
import com.alessandro.openlingo.data.remote.FeedService
import com.alessandro.openlingo.data.remote.GeneratoreEsercizi
import com.alessandro.openlingo.data.remote.TutorService
import com.alessandro.openlingo.data.remote.VocabPublishService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
object AppModule {
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    /** Anche il feed usa il client condiviso: un solo pool per tutta l'app. */
    fun provideOkHttp(): OkHttpClient = HttpCondiviso.client
    fun provideFeedService(okHttp: OkHttpClient): FeedService {
        return object : FeedService {
            override suspend fun getRaw(url: String): okhttp3.Response {
                val request = okhttp3.Request.Builder().url(url).build()
                return okHttp.newCall(request).execute()
            }
            override suspend fun head(url: String): okhttp3.Response {
                val request = okhttp3.Request.Builder().url(url).head().build()
                return okHttp.newCall(request).execute()
            }
        }
    }
    fun provideProfileManager(
        context: Context,
        json: Json,
        io: CoroutineDispatcher
    ): ProfileManager = ProfileManager(context, json, io)
    fun provideWordRepository(
        context: Context,
        service: FeedService,
        json: Json,
        io: CoroutineDispatcher,
        profileManager: ProfileManager
    ): WordRepository = WordRepository(context, service, json, io, profileManager)
    fun provideTutorService(): TutorService = TutorService()
    fun provideGeneratoreEsercizi(): GeneratoreEsercizi = GeneratoreEsercizi()
    fun provideVocabPublishService(): VocabPublishService = VocabPublishService()
}