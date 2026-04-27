# detektExtensions
This project contains extensions for the Detekt linter written in Kotlin.
See [Extending Detekt](https://detekt.github.io/detekt/extensions.html) for more information.

## The following Detekt rule extensions have been written for the Detekt linter:
* EnforceStaticImport-
  * This rule extension looks for methods that should be statically imported and issues a code smell if they
    are found.
  * The extension requires that the classpath of Detekt be set properly to provide a binding context for the
    methods that should be statically imported.
     * See [Detekt Type Resolution](https://detekt.github.io/detekt/type-resolution.html) for details
     * Examples of setting the classpath of detekt within `build.gradle.kts` are:
    ```kotlin
    tasks.withType<Detekt>().configureEach {
        val paths = mutableListOf(
                project.configurations.getByName("detekt")
            )
        classpath.setFrom(paths)
    }
    ```
    or
    ```kotlin
    tasks.withType<Detekt>().configureEach {
        dependsOn("detektMain", "detektTest")
    }
    ```
  * Rule configuration can be done in the `detekt.yml` file under the import set of rules.
    * See [detekt.yml](detekt.yml) for more information.

## In order to import this extension into your project that is also using detekt do the following:
* ensure your `settings.gradle.kts` file can pull dependencies from maven central.
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```
* in your top level `build.gradle.kts` file you need to let detekt know about the extension.
```kotlin
dependencies {
    detektPlugins("com.pkware.detekt:import-extension:x.y")
}
```
  * Where `x.y` corresponds to the version of this detekt-extensions defined in the `gradle.properties` file of this project.
* in your `detekt.yml` config file add the following code to enable the extension:
```
import:
  EnforceStaticImport:
    active: true
    methods:
      - 'com.google.common.truth.Truth.assertThat'
      - 'org.junit.jupiter.params.provider.Arguments.arguments'
```

## Releasing
Releases are automated via [release-please](https://github.com/googleapis/release-please). As commits
accumulate on `main`, release-please maintains an open release PR that updates `CHANGELOG.md` and
bumps the version in `gradle.properties`. Merging that PR triggers the publish workflow, which signs
and publishes artifacts to Maven Central and creates a GitHub Release with auto-generated release notes.
