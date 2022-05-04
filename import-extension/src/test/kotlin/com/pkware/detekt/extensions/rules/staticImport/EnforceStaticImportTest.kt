package com.pkware.detekt.extensions.rules.staticImport

import com.google.common.truth.Truth
import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.api.SourceLocation
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.io.File

private const val METHODS = "methods"

class EnforceStaticImportTest {
    private lateinit var wrapper: KotlinCoreEnvironmentWrapper
    private lateinit var env: KotlinCoreEnvironment

    // We need to set up a custom KotlinCoreEnvironment and add the Truth and Arguments class paths to that environment,
    // so they are available to generate the BindingContext for detekt rules under test with the
    // EnforceStaticImportExtension.
    @BeforeEach
    fun setUp() {
        wrapper = createEnvironment(
            additionalRootPaths = listOf(
                File(Truth::class.java.protectionDomain.codeSource.location.path),
                File(Arguments::class.java.protectionDomain.codeSource.location.path)
            )
        )
        env = wrapper.env
    }

    @AfterEach
    fun tearDown() {
        wrapper.dispose()
    }

    @Test
    fun `rule report based on custom configuration`() {

        val code = """
            fun main() {
                val value = 3.3
                value = Math.floor(value)
                print("3")
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.lang.Math.floor", "kotlin.io.print")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings)
            .hasSize(1)
            .hasSourceLocation(3, 18)
    }

    @Test
    fun `rule report truth and arguments`() {

        val code = """
            import com.google.common.truth.Truth
            import com.google.common.truth.Truth.assertThat
            import org.junit.jupiter.params.provider.Arguments
            fun main() {
                val value = 3
                Truth.assertThat(value).isEqualTo(3)
                assertThat(value).isNotEqualTo(4)
                val args = Arguments.arguments("test", "test2")
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(
                mapOf(
                    METHODS to listOf(
                        "com.google.common.truth.Truth.assertThat",
                        "org.junit.jupiter.params.provider.Arguments.arguments"
                    )
                )
            )
        ).compileAndLintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(6, 11),
            SourceLocation(8, 26)
        )
    }

    @Test
    fun `rule report with called methods using their fully qualified names`() {

        val code = """
            fun main() {
                var value = 3.54
                value = java.lang.Math.floor(value)
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.lang.Math.floor")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings)
            .hasSize(1)
            .hasSourceLocation(3, 28)
    }

    @Test
    fun `rule report with multiple different methods when config is a string`() {

        val code = """
            fun main() {
                value = 3.7
                value = Math.floor(value)
                System.gc()
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to "java.lang.Math.floor, java.lang.System.gc"))
        ).compileAndLintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(3, 18),
            SourceLocation(4, 12)
        )
    }

    @Test
    fun `rule report both methods that do and do not use full signature`() {

        val code = """
            import java.time.Clock
            import java.time.LocalDate
            val clock = Clock.systemUTC()
            val date = LocalDate.now()
            val date2 = LocalDate.now(clock)
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.time.LocalDate.now")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(4, 22),
            SourceLocation(5, 23)
        )
    }

    @Test
    fun `rule report parameterless method when full signature matches`() {

        val code = """
                import java.time.Clock
                import java.time.LocalDate
                val clock = Clock.systemUTC()
                val date = LocalDate.now()
                val date2 = LocalDate.now(clock)
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.time.LocalDate.now()")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings)
            .hasSize(1)
            .hasSourceLocation(4, 22)
    }

    @Test
    fun `rule report method with parameter when full signature matches`() {

        val code = """
                import java.time.Clock
                import java.time.LocalDate
                val clock = Clock.systemUTC()
                val date = LocalDate.now()
                val date2 = LocalDate.now(clock)
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.time.LocalDate.now(java.time.Clock)")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings)
            .hasSize(1)
            .hasSourceLocation(5, 23)
    }

    @Test
    fun `rule report method with multiple parameters when full signature matches`() {

        val code = """
                import java.time.LocalDate
                val date = LocalDate.of(2020, 1, 1)
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.time.LocalDate.of(kotlin.Int, kotlin.Int, kotlin.Int)")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings)
            .hasSize(1)
            .hasSourceLocation(2, 22)
    }

    @Test
    fun `rule report method with spaces and commas`() {

        val code = """
                package com.pkware.test

                import com.pkware.test.Example.Companion.`some, test`

                class Example {
                    companion object {
                        fun `some, test`() = "String"
                    }
                }

                fun test() {
                    val s = `some, test`()
                    val s2 = Example.`some, test`()
                    val s3 = Example.Companion.`some, test`()
                }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("com.pkware.test.Example.Companion.`some, test`()")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(13, 22),
            SourceLocation(14, 32),
        )
    }

    @Test
    fun `rule report method with default parameters`() {

        val code = """
                package com.pkware.test

                import com.pkware.test.Example.Companion.defaultParamsMethod

                class Example {
                    companion object {
                        fun defaultParamsMethod(s: String, i: Int = 0) = s + i
                    }
                }

                fun test() {
                    val s = defaultParamsMethod("test")
                    val s2 = Example.defaultParamsMethod("test")
                    val s3 = Example.Companion.defaultParamsMethod("test")
                }
            """

        val findings = EnforceStaticImport(
            TestConfig(
                mapOf(
                    METHODS to listOf(
                        "com.pkware.test.Example.Companion.defaultParamsMethod(kotlin.String,kotlin.Int)"
                    )
                )
            )
        ).compileAndLintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(13, 22),
            SourceLocation(14, 32),
        )
    }

    @Test
    fun `no rule report if configuration methods are blank`() {

        val code = """
            fun main() {
                value = 3.7
                value = Math.floor(value)
                print("3")
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to "  "))
        ).compileAndLintWithContext(env, code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `no rule report if called methods do not match configuration methods`() {

        val code = """
            import java.lang.System
            fun main() {
                System.out.println("hello")
            }
            """

        val findings = EnforceStaticImport(
            TestConfig(mapOf(METHODS to listOf("java.lang.System.gc")))
        ).compileAndLintWithContext(env, code)

        assertThat(findings).isEmpty()
    }
}
