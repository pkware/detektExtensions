package com.pkware.detekt.extensions.rules.micronaut

import dev.detekt.api.SourceLocation
import dev.detekt.test.lint
import dev.detekt.test.location
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RequireSecuredAnnotationTest {
    private val rule = RequireSecuredAnnotation()

    @Test
    fun `reports endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(4, 17))
        assertThat(findings[0].message).isEqualTo(
            "Endpoint method 'getUsers' must have a security annotation (@Secured, @PermitAll, @RolesAllowed, or @DenyAll).",
        )
    }

    @Test
    fun `reports Post endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Post("/users")
                fun createUser() {
                    return Unit
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Put endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Put("/users/{id}")
                fun updateUser(id: String) {
                    return Unit
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Delete endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Delete("/users/{id}")
                fun deleteUser(id: String) {
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Patch endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Patch("/users/{id}")
                fun patchUser(id: String) {
                    return Unit
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Head endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Head("/users")
                fun checkUsers() {
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Options endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Options("/users")
                fun optionsUsers() {
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports Trace endpoint without security annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Trace("/users")
                fun traceUsers() {
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `does not report endpoint with Secured annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Secured("ROLE_ADMIN")
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with PermitAll annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @PermitAll
                @Get("/public")
                fun getPublicData() {
                    return listOf()
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with RolesAllowed annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @RolesAllowed("USER", "ADMIN")
                @Get("/protected")
                fun getProtectedData() {
                    return listOf()
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report endpoint with DenyAll annotation`() {
        val code = """
            @Controller("/api")
            class UserController {
                @DenyAll
                @Get("/forbidden")
                fun getForbiddenData() {
                    return listOf()
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report non-endpoint function in controller`() {
        val code = """
            @Controller("/api")
            class UserController {
                fun regularFunction() {
                    return "not an endpoint"
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `does not report function with unrelated annotation in controller`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Deprecated("Use newMethod instead")
                fun oldMethod() {
                    return "old"
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `reports multiple unsecured endpoints`() {
        val code = """
            @Controller("/api")
            class UserController {
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }

                @Post("/users")
                fun createUser() {
                    return Unit
                }

                @Delete("/users/{id}")
                fun deleteUser(id: String) {
                }
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(3)
        assertThat(findings.map { it.location.source }).containsExactly(
            SourceLocation(4, 17),
            SourceLocation(9, 17),
            SourceLocation(14, 17),
        )
    }

    @Test
    fun `reports only unsecured endpoints in mixed file`() {
        val code = """
            @Controller("/api")
            class UserController {
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
            }
        """

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].location.source).isEqualTo(SourceLocation(10, 17))
        assertThat(findings[0].message).isEqualTo(
            "Endpoint method 'getPublic' must have a security annotation (@Secured, @PermitAll, @RolesAllowed, or @DenyAll).",
        )
    }

    @Test
    fun `security annotation can be before or after HTTP annotation`() {
        val code1 = """
            @Controller("/api")
            class UserController {
                @Secured("ROLE_USER")
                @Get("/data")
                fun getData() {
                    return "data"
                }
            }
        """

        val code2 = """
            @Controller("/api")
            class UserController {
                @Get("/data")
                @Secured("ROLE_USER")
                fun getData() {
                    return "data"
                }
            }
        """

        assertThat(rule.lint(code1)).isEmpty()
        assertThat(rule.lint(code2)).isEmpty()
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

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Nested
    inner class NonControllerContexts {
        @Test
        fun `does not report endpoints in Client interface`() {
            val code = """
                @Client("/api")
                interface UserClient {
                    @Get("/users")
                    fun getUsers(): List<String>

                    @Post("/users")
                    fun createUser(): Unit

                    @Delete("/users/{id}")
                    fun deleteUser(id: String): Unit
                }
            """

            val findings = rule.lint(code)

            assertThat(findings).isEmpty()
        }

        @Test
        fun `does not report endpoints in class without Controller annotation`() {
            val code = """
                class NotAController {
                    @Get("/users")
                    fun getUsers() {
                        return listOf()
                    }
                }
            """

            val findings = rule.lint(code)

            assertThat(findings).isEmpty()
        }

        @Test
        fun `does not report top-level endpoint function`() {
            val code = """
                @Get("/users")
                fun getUsers() {
                    return listOf()
                }
            """

            val findings = rule.lint(code)

            assertThat(findings).isEmpty()
        }
    }

    @Nested
    inner class ClassLevelSecurityAnnotations {
        @Test
        fun `reports endpoint even when controller class has Secured annotation`() {
            val code = """
                @Secured("ROLE_ADMIN")
                @Controller("/api")
                class AdminController {
                    @Get("/users")
                    fun getUsers() {
                        return listOf()
                    }
                }
            """

            val findings = rule.lint(code)

            assertThat(findings).hasSize(1)
        }
    }
}
