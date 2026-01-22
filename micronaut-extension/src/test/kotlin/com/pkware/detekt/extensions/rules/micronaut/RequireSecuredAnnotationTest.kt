package com.pkware.detekt.extensions.rules.micronaut

import io.gitlab.arturbosch.detekt.api.SourceLocation
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.junit.jupiter.api.Test

class RequireSecuredAnnotationTest {
    private val rule = RequireSecuredAnnotation()

    @Test
    fun `reports endpoint without security annotation`() {
        val code = """
            @Get("/users")
            fun getUsers() {
                return listOf()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings)
            .hasSize(1)
            .hasStartSourceLocation(2, 13)
        assertThat(findings.first())
            .hasMessage(
                "Endpoint method 'getUsers' must have a security annotation (@Secured, @PermitAll, @RolesAllowed, or @DenyAll).",
            )
    }

    @Test
    fun `reports Post endpoint without security annotation`() {
        val code = """
            @Post("/users")
            fun createUser() {
                return User()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Put endpoint without security annotation`() {
        val code = """
            @Put("/users/{id}")
            fun updateUser(id: String) {
                return User()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Delete endpoint without security annotation`() {
        val code = """
            @Delete("/users/{id}")
            fun deleteUser(id: String) {
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Patch endpoint without security annotation`() {
        val code = """
            @Patch("/users/{id}")
            fun patchUser(id: String) {
                return User()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Head endpoint without security annotation`() {
        val code = """
            @Head("/users")
            fun checkUsers() {
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Options endpoint without security annotation`() {
        val code = """
            @Options("/users")
            fun optionsUsers() {
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Trace endpoint without security annotation`() {
        val code = """
            @Trace("/users")
            fun traceUsers() {
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `does not report endpoint with Secured annotation`() {
        val code = """
            @Secured("ROLE_ADMIN")
            @Get("/users")
            fun getUsers() {
                return listOf()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with PermitAll annotation`() {
        val code = """
            @PermitAll
            @Get("/public")
            fun getPublicData() {
                return listOf()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with RolesAllowed annotation`() {
        val code = """
            @RolesAllowed("USER", "ADMIN")
            @Get("/protected")
            fun getProtectedData() {
                return listOf()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with DenyAll annotation`() {
        val code = """
            @DenyAll
            @Get("/forbidden")
            fun getForbiddenData() {
                return listOf()
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report non-endpoint function`() {
        val code = """
            fun regularFunction() {
                return "not an endpoint"
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report function with unrelated annotation`() {
        val code = """
            @Deprecated("Use newMethod instead")
            fun oldMethod() {
                return "old"
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `reports multiple unsecured endpoints`() {
        val code = """
            @Get("/users")
            fun getUsers() {
                return listOf()
            }

            @Post("/users")
            fun createUser() {
                return User()
            }

            @Delete("/users/{id}")
            fun deleteUser(id: String) {
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(3)
        assertThat(findings).hasStartSourceLocations(
            SourceLocation(2, 13),
            SourceLocation(7, 13),
            SourceLocation(12, 13),
        )
    }

    @Test
    fun `reports only unsecured endpoints in mixed file`() {
        val code = """
            @Secured("ROLE_ADMIN")
            @Get("/admin")
            fun getAdmin() {
                return "admin"
            }

            @Get("/public")
            fun getPublic() {
                return "public"
            }

            fun regularFunction() {
                return "regular"
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings)
            .hasSize(1)
            .hasStartSourceLocation(8, 13)
        assertThat(findings.first())
            .hasMessage(
                "Endpoint method 'getPublic' must have a security annotation (@Secured, @PermitAll, @RolesAllowed, or @DenyAll).",
            )
    }

    @Test
    fun `security annotation can be before or after HTTP annotation`() {
        val code1 = """
            @Secured("ROLE_USER")
            @Get("/data")
            fun getData() {
                return "data"
            }
        """

        val code2 = """
            @Get("/data")
            @Secured("ROLE_USER")
            fun getData() {
                return "data"
            }
        """

        assertThat(rule.compileAndLint(code1)).isEmpty()
        assertThat(rule.compileAndLint(code2)).isEmpty()
    }

    @Test
    fun `reports endpoint in controller class`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `does not report secured endpoint in controller class`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Secured("ROLE_USER")
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }
            }
        """

        val findings = rule.compileAndLint(code)

        assertThat(findings).isEmpty()
    }
}
