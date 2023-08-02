package com.pkware.detekt.extensions.rules.staticImport

import io.github.detekt.tooling.api.FunctionMatcher
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall

/**
 * This detekt extension rule allows to set a list of methods that should be statically imported. This can be used to
 * discourage redundant class and/or method calls.
 *
 * Detekt will then report all method invocations that that should be statically linked.
 *
 * This rule requires detekt to be run with a context defined for type resolution. This helps resolve incoming method
 * calls to be checked against the list of methods that should be statically imported.
 *
 * @param config The detekt configuration passed into this rule.
 * The configuration can override the default methods used here that should be statically linked
 */
@RequiresTypeResolution
class EnforceStaticImport(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Method should be imported statically.",
        Debt.TEN_MINS,
    )

    @Configuration(
        "Comma separated list of fully qualified method signatures which should be statically imported. " +
            "Methods can be defined without full signature (i.e. `com.google.common.truth.Truth.assertThat`) which " +
            "will report calls of all methods with this name or with full signature " +
            "(i.e. `com.google.common.truth.Truth.assertThat(java.lang.String)`) which would only report method " +
            "calls with this concrete signature.",
    )
    private val methods: List<FunctionMatcher> by config(listOf("")) { it.map(FunctionMatcher::fromFunctionSignature) }

    /**
     * A call expression is triggered for a method and/or function call.
     * @see <a href="https://kotlinlang.org/spec/expressions.html#call-and-property-access-expressions">https://kotlinlang.org/spec/expressions.html#call-and-property-access-expressions</a>
     */
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        check(expression)
    }

    /**
     * Analyze an incoming [KtExpression] and generate a detekt code smell error if the expression's resolved method
     * matches a method in our list that should be statically linked.
     *
     * This method will not detect any code smells if there is no binding context setup before it is called.
     * @see EnforceStaticImport class definition
     *
     * @param expression The incoming [KtExpression] to analyze
     */
    private fun check(expression: KtExpression) {
        if (bindingContext == BindingContext.EMPTY) return

        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return

        val isStaticImport = resolvedCall.call.explicitReceiver == null
        if (isStaticImport) return

        val descriptors = resolvedCall.resultingDescriptor.let {
            listOf(it) + it.overriddenDescriptors
        }

        for (descriptor in descriptors) {
            methods.find { it.match(descriptor) }?.let { functionMatcher ->
                val message = "$functionMatcher needs to be statically imported."
                report(CodeSmell(issue, Entity.from(expression), message))
            }
        }
    }
}
