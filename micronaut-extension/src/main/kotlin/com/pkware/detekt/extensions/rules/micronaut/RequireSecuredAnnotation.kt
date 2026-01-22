package com.pkware.detekt.extensions.rules.micronaut

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * This detekt rule ensures that all Micronaut controller endpoint methods have security annotations.
 *
 * Micronaut HTTP endpoint methods (those annotated with @Get, @Post, @Put, @Delete, @Patch, @Head,
 * @Options, or @Trace) must have one of the following security annotations:
 * - @Secured (io.micronaut.security.annotation.Secured)
 * - @PermitAll (jakarta.annotation.security.PermitAll)
 * - @RolesAllowed (jakarta.annotation.security.RolesAllowed)
 * - @DenyAll (jakarta.annotation.security.DenyAll)
 *
 * This helps prevent accidentally creating unsecured endpoints that could expose sensitive data or operations.
 *
 * @param config The detekt configuration passed into this rule.
 */
class RequireSecuredAnnotation(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Security,
            "Micronaut endpoint methods must have a security annotation (@Secured, @PermitAll, @RolesAllowed, or @DenyAll).",
            Debt.FIVE_MINS,
        )

    /**
     * HTTP method annotations that mark Micronaut controller endpoints.
     * These all have the @HttpMethodMapping meta-annotation.
     */
    private val httpMethodAnnotations = setOf(
        "Get",
        "Post",
        "Put",
        "Delete",
        "Patch",
        "Head",
        "Options",
        "Trace",
    )

    /**
     * Security annotations that indicate authorization rules are defined.
     */
    private val securityAnnotations = setOf(
        "Secured", // io.micronaut.security.annotation.Secured
        "PermitAll", // jakarta.annotation.security.PermitAll
        "RolesAllowed", // jakarta.annotation.security.RolesAllowed
        "DenyAll", // jakarta.annotation.security.DenyAll
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        // Check if this function is a Micronaut endpoint (has an HTTP method annotation)
        val hasHttpMethodAnnotation = function.annotationEntries.any { annotation ->
            annotation.shortName?.identifier in httpMethodAnnotations
        }

        if (!hasHttpMethodAnnotation) {
            // Not an endpoint method, no need to check for security annotations
            return
        }

        // Check if the function has a security annotation
        val hasSecurityAnnotation = function.annotationEntries.any { annotation ->
            annotation.shortName?.identifier in securityAnnotations
        }

        if (!hasSecurityAnnotation) {
            val functionName = function.name ?: "unknown"
            val message = "Endpoint method '$functionName' must have a security annotation " +
                "(@Secured, @PermitAll, @RolesAllowed, or @DenyAll)."
            report(CodeSmell(issue, Entity.from(function), message))
        }
    }
}
