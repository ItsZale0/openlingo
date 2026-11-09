import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Versione centralizzata: version.properties è l'unica fonte di verità.
// bumpVersion incrementa versionCode e patch di versionName, poi allinea
// updates/version.json. Non modificare la versione a mano nei due posti.
val versionProps = Properties().apply {
    val f = rootProject.file("version.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val appVersionCode = versionProps.getProperty("VERSION_CODE", "203").toInt()
val appVersionName = versionProps.getProperty("VERSION_NAME", "1.43.0")

android {
    namespace = "com.alessandro.tedesco"
    compileSdk = 35

    val freeApiKey: String = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }.getProperty("FREE_API_KEY", "")

    defaultConfig {
        applicationId = "com.alessandro.tedesco"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("String", "FREE_API_KEY", "\"$freeApiKey\"")
    }

    signingConfigs {
        getByName("debug") {
            // Firma di sviluppo.
            //
            // Il keystore NON è più versionato: un keystore pubblico permette a
            // chiunque di firmare un APK che l'app accetterebbe come aggiornamento.
            // Per build riproducibili (CI o release) si fornisce un keystore
            // privato tramite variabili d'ambiente; altrimenti Gradle usa il
            // debug keystore locale generato automaticamente.
            val percorsoKeystore = System.getenv("TEDESCO_KEYSTORE_FILE")
            if (!percorsoKeystore.isNullOrBlank()) {
                storeFile = file(percorsoKeystore)
                storePassword = System.getenv("TEDESCO_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TEDESCO_KEY_ALIAS")
                keyPassword = System.getenv("TEDESCO_KEY_PASSWORD")
            }
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// ── Versionamento centralizzato ───────────────────────────────────────
// La versione vive in version.properties. bumpVersion incrementa versionCode
// e la patch di versionName, poi aggiorna updates/version.json.
// Uso: gradle bumpVersion -Pchangelog="Cosa è cambiato"
// Va eseguito PRIMA di ogni commit che modifica l'app o i metadati di release.
tasks.register("bumpVersion") {
    group = "release"
    description = "Incrementa versionCode e patch di versionName, aggiorna updates/version.json"
    doLast {
        val f = rootProject.file("version.properties")
        val props = Properties()
        if (f.exists()) f.inputStream().use { props.load(it) }
        val oldCode = props.getProperty("VERSION_CODE", "0").toIntOrNull()
            ?: throw GradleException("VERSION_CODE mancante o non numerico in version.properties")
        val oldName = props.getProperty("VERSION_NAME", "0.0.0")
        val parts = oldName.split(".")
        if (parts.size != 3) {
            throw GradleException("VERSION_NAME deve essere major.minor.patch, trovato: $oldName")
        }
        val major = parts[0].toIntOrNull()
            ?: throw GradleException("VERSION_NAME non valido: $oldName")
        val minor = parts[1].toIntOrNull()
            ?: throw GradleException("VERSION_NAME non valido: $oldName")
        val patch = parts[2].toIntOrNull()
            ?: throw GradleException("VERSION_NAME non valido: $oldName")
        val newName = "$major.$minor.${patch + 1}"
        val newCode = oldCode + 1
        f.writeText("VERSION_CODE=$newCode\nVERSION_NAME=$newName\n")

        val changelog = (project.findProperty("changelog") as String?)?.trim().orEmpty()
        val escaped = changelog
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        val apkUrl = "https://github.com/ItsZale0/tedesco-app/releases/download/v$newName/Deutsch-v$newName.apk"
        val json = """
            {
              "versionCode": $newCode,
              "versionName": "$newName",
              "changelog": "$escaped",
              "minVersionCode": 0,
              "apkUrl": "$apkUrl"
            }
        """.trimIndent()
        val out = rootProject.layout.projectDirectory.file("updates/version.json").asFile
        out.parentFile.mkdirs()
        out.writeText(json)
        println("Versione: $oldName ($oldCode) -> $newName ($newCode)")
        println("version.json aggiornato: $apkUrl")
    }
}

// Rigenera solo il manifest dalla versione corrente, senza incrementare.
// Utile se version.json è stato modificato a mano o è rimasto indietro.
tasks.register("generateVersionJson") {
    group = "release"
    description = "Allinea updates/version.json a version.properties"
    doLast {
        val props = Properties()
        val f = rootProject.file("version.properties")
        if (f.exists()) f.inputStream().use { props.load(it) }
        val vc = props.getProperty("VERSION_CODE", "0").toIntOrNull() ?: 0
        val vn = props.getProperty("VERSION_NAME", "0.0.0")
        val apkUrl = "https://github.com/ItsZale0/tedesco-app/releases/download/v$vn/Deutsch-v$vn.apk"
        val json = """
            {
              "versionCode": $vc,
              "versionName": "$vn",
              "changelog": "",
              "minVersionCode": 0,
              "apkUrl": "$apkUrl"
            }
        """.trimIndent()
        val out = rootProject.layout.projectDirectory.file("updates/version.json").asFile
        out.parentFile.mkdirs()
        out.writeText(json)
        println("generateVersionJson: versionCode=$vc versionName=$vn")
    }
}

// ── Verifica automatica della firma APK ──────────────────────────────
// Android pretende i certificati "at entry AndroidManifest.xml": quella e' la
// firma v1 (JAR). Un APK con la sola v2/v3 viene rifiutato come
// "missing certificates" se il file subisce una minima modifica in transito.
// Questo task fa fallire la build se la v1 non c'e': mai piu' APK non
// installabili per firma mancante.
val androidSdkDir: String = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: "/opt/data/toolchain/android-sdk"

val apksignerPath: String? = fileTree("$androidSdkDir/build-tools") {
    include("*/apksigner")
    include("*/apksigner.bat")
}
    .files
    .maxByOrNull { it.parentFile.name }
    ?.absolutePath

tasks.register("verificaFirmaApk") {
    group = "verification"
    description = "Verifica che l'APK debug sia firmato v1 (JAR) + v2 + v3"
    dependsOn("assembleDebug")
    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile
        val signer = apksignerPath
        if (signer == null) {
            println("apksigner non trovato in $androidSdkDir/build-tools: verifica firma saltata")
            return@doLast
        }
        if (!apk.exists()) throw GradleException("APK non trovato: ${apk.absolutePath}")
        project.exec {
            commandLine(
                signer,
                "verify",
                "--min-sdk-version", "23",
                "--verbose",
                apk.absolutePath
            )
        }
        println("Firma APK verificata: v1 (JAR) + v2 + v3 presenti")
    }
}

// matching() e' lazy: assembleDebug lo registra AGP piu' tardi, named() fallirebbe.
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy("verificaFirmaApk") }
