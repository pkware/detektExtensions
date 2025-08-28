plugins {
    kotlin("jvm")
    `maven-publish`
    signing
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
    testImplementation(libs.truth)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj)

    testRuntimeOnly(libs.junit.jupiter.engine)
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
}

// <editor-fold desc="Publishing and Signing">

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = pomArtifactId
            from(components["java"])
            pom {
                name.set(pomName)
                packaging = pomPackaging
                description.set(pomDescription)
                url.set("https://github.com/pkware/detektExtensions")
                setPkwareOrganization()

                developers {
                    developer {
                        id.set("all")
                        name.set("PKWARE, Inc.")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/pkware/detektExtensions.git")
                    developerConnection.set("scm:git:ssh://github.com/pkware/detektExtensions.git")
                    url.set("https://github.com/pkware/detektExtensions")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        distribution.set("repo")
                        url.set("https://github.com/pkware/detektExtensions/blob/master/LICENSE")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri(if (version.toString().isReleaseBuild) releaseRepositoryUrl else snapshotRepositoryUrl)
            credentials {
                username = repositoryUsername
                password = repositoryPassword
            }
        }
    }
}

signing {
    // Signing credentials are stored as secrets in GitHub.
    // See https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials for more information.

    useInMemoryPgpKeys(
        signingKeyId, // ID of the GPG key
        signingKey, // GPG key
        signingPassword, // Password for the GPG key
    )

    sign(publishing.publications["mavenJava"])
}

val String.isReleaseBuild
    get() = !contains("SNAPSHOT")

val Project.releaseRepositoryUrl: String
    get() =
        properties.getOrDefault(
            "RELEASE_REPOSITORY_URL",
            "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2",
        ).toString()

val Project.snapshotRepositoryUrl: String
    get() =
        properties.getOrDefault(
            "SNAPSHOT_REPOSITORY_URL",
            "https://central.sonatype.com/repository/maven-snapshots/",
        ).toString()

val Project.repositoryUsername: String
    get() = properties.getOrDefault("NEXUS_USERNAME", "").toString()

val Project.repositoryPassword: String
    get() = properties.getOrDefault("NEXUS_PASSWORD", "").toString()

val Project.signingKeyId: String
    get() = properties.getOrDefault("SIGNING_KEY_ID", "").toString()

val Project.signingKey: String
    get() = properties.getOrDefault("SIGNING_KEY", "").toString()

val Project.signingPassword: String
    get() = properties.getOrDefault("SIGNING_PASSWORD", "").toString()

val Project.pomPackaging: String
    get() = properties.getOrDefault("POM_PACKAGING", "jar").toString()

val Project.pomName: String?
    get() = properties["POM_NAME"]?.toString()

val Project.pomDescription: String?
    get() = properties["POM_DESCRIPTION"]?.toString()

val Project.pomArtifactId
    get() = properties.getOrDefault("POM_ARTIFACT_ID", name).toString()

fun MavenPom.setPkwareOrganization() {
    organization {
        name.set("PKWARE, Inc.")
        url.set("https://www.pkware.com")
    }
}
// </editor-fold>
