plugins {
    kotlin("jvm")
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.ksp)
}

val detektExtensionVersion: String by project
version = detektExtensionVersion

ksp {
    arg("autoserviceKsp.verify", "true")
}

dependencies {
    ksp(libs.auto.service.ksp)
    implementation(libs.auto.service.annotations)
    implementation(libs.detekt.tooling)
    implementation(libs.detekt.api)

    testImplementation(libs.detekt.test)
    testImplementation(libs.detekt.parser)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
