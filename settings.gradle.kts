include(
    "import-extension",
    "micronaut-extension"
)

rootProject.name = "DetektExtensions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version "4.4.0"
}

val isCiServer = System.getenv().containsKey("CI")

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        if (isCiServer) {
            tag("CI")
        }
    }
}

buildCache {
    local {
        // Disable on CI b/c local cache will always be empty and will be cleared after run
        isEnabled = !isCiServer
    }
}
