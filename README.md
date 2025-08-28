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

## Releasing:
1. Make and checkout a release branch on github.
2. Change the version in gradle.properties to a non-SNAPSHOT version.
3. Update the CHANGELOG.md for the impending release.
4. Run `git commit -am "Release X.Y.Z."` (where X.Y.Z is the new version) in the terminal or command
line.
5. Make a PR with your changes.
6. Merge the release PR after approval, tag the commit on the main branch with
`git tag -a X.Y.Z -m "X.Y.Z"`(X.Y.Z is the new version).
7. Run `git push --tags`.
8. Visit [Sonatype Nexus](https://central.sonatype.com/) and promote the artifact.
9. Update `gradle.properties` to the next SNAPSHOT version.
10. Run `git commit -am "Prepare next development version."`
11. Make a PR with your changes.
12. Merge the next version PR after approval.
