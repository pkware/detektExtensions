# detektExtensions
This project contains extensions for the Detekt linter written in Kotlin.
See [Extending Detekt](https://detekt.github.io/detekt/extensions.html) for more information.

The following Detekt rule extensions have been written for the Detekt linter:
* EnforceStaticImport-
  * This rule extension looks for methods that should be statically imported and issues a code smell if they
    are found.
  * This rule extension requires that the classpath of Detekt be set properly to provide a binding context for the
    methods that should be statically imported.
     * See [Detekt Type Resolution](https://detekt.github.io/detekt/type-resolution.html) for details
     * An example of setting the classpath of detekt within `build.gradle.kts` is:
    ```kotlin
    tasks.withType<Detekt>().configureEach {
        val paths = mutableListOf(
                project.configurations.getByName("detekt")
            )
            classpath.setFrom(paths)
    }
    ```
  * This rule can be configured in a `detekt.yml` file under the import set of rules,
    see [detekt.yml](detekt.yml) for more information

In order to import this extension into your project that is also using detekt do the following:
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
