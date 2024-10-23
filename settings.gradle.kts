pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven {url = uri("https://repo.eclipse.org/content/repositories/paho-releases/") }
        maven(url = "https://maven.testfairy.com")
    }
}

rootProject.name = "CCS2 Android HMI"
include(":app")
 