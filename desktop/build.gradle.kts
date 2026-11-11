import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.desktop)
}

// Stessa versione dell'app Android: version.properties e' l'unica fonte.
val versionProps = Properties().apply {
    val f = rootProject.file("version.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val appVersionName = versionProps.getProperty("VERSION_NAME", "1.0.0")

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// La versione mostrata nelle Impostazioni e' quella del file condiviso.
tasks.withType<ProcessResources>().configureEach {
    from(rootProject.file("version.properties"))
}

dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.serialization.json)
}

compose.desktop {
    application {
        mainClass = "com.alessandro.openlingo.desktop.MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "OpenLingo"
            packageVersion = appVersionName
            description = "OpenLingo - language learning with real didactics and spaced repetition"
            vendor = "OpenLingo"
            modules("java.net.http", "jdk.crypto.ec")
        }
    }
}
