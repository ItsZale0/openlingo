pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Gradle rileva da solo gradle/libs.versions.toml e crea `libs`.
// Dichiararlo a mano qui dentro darebbe errore "from chiamato piu' di una volta".
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Deutsch"
include(":app")
include(":core")
include(":desktop")
