import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
    kotlin("jvm") version "2.3.10" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    group = "com.pkware.detekt"

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {

            jvmTarget.set(JvmTarget.JVM_1_8)

            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-Xinline-classes",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
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
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = tasks.named<KotlinCompile>("compileKotlin").get().compilerOptions.jvmTarget.get().target
        parallel = true
        config.from(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
    }
}


