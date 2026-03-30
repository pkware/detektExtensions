package com.pkware.detekt.extensions.rules.staticImport

import com.google.common.truth.Truth.assertThat
import dev.detekt.api.SourceLocation
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.location
import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val METHODS = "methods"

class EnforceStaticImportTest {
    private lateinit var env: KotlinEnvironmentContainer

    @BeforeEach
    fun setUp() {
        env = createEnvironment()
    }

    @Test
    fun `rule report based on custom configuration`() {
        val code = """
            fun main() {
                var value = 3.3
                value = Math.floor(value)
                print("3")
            }
            """

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.lang.Math.floor", "kotlin.io.print"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(4, 30))
    }

    @Test
    fun `rule report with multiple equivalent static import methods`() {
        val code = """
            import java.lang.Math.floor
            fun main() {
                val value = 3.0
                Math.floor(value)
                floor(value)
                java.time.LocalDate.now()
            }
            """

        val findings =
            EnforceStaticImport(
                TestConfig(
                    Pair(
                        METHODS,
                        listOf(
                            "java.lang.Math.floor",
                            "java.time.LocalDate.now",
                        ),
                    ),
                ),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(5, 22),
            SourceLocation(7, 37),
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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.lang.Math.floor"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(4, 40))
    }

    @Test
    fun `rule report with multiple different methods`() {
        val code = """
            fun main() {
                var value = 3.7
                value = Math.floor(value)
                System.gc()
            }
            """

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.lang.Math.floor", "java.lang.System.gc"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(4, 30),
            SourceLocation(5, 24),
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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.time.LocalDate.now"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(5, 34),
            SourceLocation(6, 35),
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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.time.LocalDate.now()"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(5, 38))
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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.time.LocalDate.now(java.time.Clock)"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(6, 39))
    }

    @Test
    fun `rule report method with multiple parameters when full signature matches`() {
        val code = """
                import java.time.LocalDate
                val date = LocalDate.of(2020, 1, 1)
            """

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.time.LocalDate.of(kotlin.Int, kotlin.Int, kotlin.Int)"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(3, 38))
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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("com.pkware.test.Example.Companion.`some, test`()"))),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(14, 38),
            SourceLocation(15, 48),
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

        val findings =
            EnforceStaticImport(
                TestConfig(
                    Pair(
                        METHODS,
                        listOf(
                            "com.pkware.test.Example.Companion.defaultParamsMethod(kotlin.String,kotlin.Int)",
                        ),
                    ),
                ),
            ).lintWithContext(env, code)

        assertThat(findings).hasSize(2)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(14, 38),
            SourceLocation(15, 48),
        )
    }

    @Test
    fun `no rule report if configuration methods are blank`() {
        val code = """
            fun main() {
                var value = 3.7
                value = Math.floor(value)
                print("3")
            }
            """

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("  "))),
            ).lintWithContext(env, code)

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

        val findings =
            EnforceStaticImport(
                TestConfig(Pair(METHODS, listOf("java.lang.System.gc"))),
            ).lintWithContext(env, code)

        assertThat(findings).isEmpty()
    }
}
