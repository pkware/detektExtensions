import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
    kotlin("jvm") version "1.7.21" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    group = "com.pkware.detekt"

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val kotlinJvmTarget = "1.8"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmTarget
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict",

                // Ensure assertions don't add performance cost. See https://youtrack.jetbrains.com/issue/KT-22292
                "-Xassertions=jvm"
            )
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        // Do not run tests in parallel for this project as it breaks with "duplicate registrations of
        // org.jetbrains.kotlin.diagnosticSuppressor" errors. This is probably due to the setup of the embedded kotlin
        // compiler done for the tests.
    }

    dependencies {
        detektPlugins(project(":import-extension"))
        // Work around to get libs.detekt.rules.libraries in subprojects:
        // see https://github.com/gradle/gradle/issues/16634
        detektPlugins(rootProject.libs.detekt.rules.libraries)
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = kotlinJvmTarget
        parallel = true
        config.from(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
    }
}


