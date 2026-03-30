package com.pkware.detekt.extensions.rules.staticImport

import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import dev.detekt.psi.FunctionMatcher
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * This detekt extension rule allows to set a list of methods that should be statically imported. This can be used to
 * discourage redundant class and/or method calls.
 *
 * Detekt will then report all method invocations that that should be statically linked.
 *
 * This rule requires detekt to be run with type resolution enabled. This helps resolve incoming method
 * calls to be checked against the list of methods that should be statically imported.
 *
 * @param config The detekt configuration passed into this rule.
 * The configuration can override the default methods used here that should be statically linked
 */
class EnforceStaticImport(config: Config = Config.empty) :
    Rule(config, "Method should be imported statically."),
    RequiresAnalysisApi {

    @Configuration(
        "Comma separated list of fully qualified method signatures which should be statically imported. " +
            "Methods can be defined without full signature (i.e. `com.google.common.truth.Truth.assertThat`) which " +
            "will report calls of all methods with this name or with full signature " +
            "(i.e. `com.google.common.truth.Truth.assertThat(java.lang.String)`) which would only report method " +
            "calls with this concrete signature.",
    )
    private val methods: List<FunctionMatcher> by config(listOf("")) { it.map(FunctionMatcher::fromFunctionSignature) }

    /**
     * A dot-qualified expression triggers for method calls with an explicit receiver (e.g. `Math.floor(x)`).
     * We only report these — calls without an explicit receiver are already statically imported.
     */
    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val callExpression = expression.selectorExpression as? KtCallExpression ?: return

        analyze(callExpression) {
            val symbol = callExpression.resolveToCall()?.singleFunctionCallOrNull()?.symbol ?: return@analyze

            val allSymbols: List<KaCallableSymbol> = buildList {
                add(symbol)
                if (symbol is KaNamedFunctionSymbol) addAll(symbol.allOverriddenSymbols.toList())
            }

            val matcher = allSymbols.firstNotNullOfOrNull { sym -> methods.find { it.match(sym) } }
            if (matcher != null) {
                report(Finding(Entity.from(callExpression), "$matcher needs to be statically imported."))
            }
        }
    }
}
