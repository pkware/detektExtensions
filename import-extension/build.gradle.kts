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
            url = uri(if (version.toString().isReleaseBuild) releaseRepositoryUrl else snapshotRepositoryUrl)
            credentials {
                username = repositoryUsername
                password = repositoryPassword
            }
        }
    }
}

signing {
    // Signing credentials are stored locally in the user's global gradle.properties file.
    // See https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials for more information.
    sign(publishing.publications["mavenJava"])
}

val String.isReleaseBuild
    get() = !contains("SNAPSHOT")

val Project.releaseRepositoryUrl: String
    get() = properties.getOrDefault(
        "RELEASE_REPOSITORY_URL",

        "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    ).toString()

val Project.snapshotRepositoryUrl: String
    get() = properties.getOrDefault(
        "SNAPSHOT_REPOSITORY_URL",
        "https://oss.sonatype.org/content/repositories/snapshots"
    ).toString()

val Project.repositoryUsername: String
    get() = properties.getOrDefault("NEXUS_USERNAME", "").toString()

val Project.repositoryPassword: String
    get() = properties.getOrDefault("NEXUS_PASSWORD", "").toString()

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
