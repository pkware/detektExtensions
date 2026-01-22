package com.pkware.detekt.extensions.rules.micronaut

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * The Micronaut extension rule set provider for this detekt extension.
 * Provides security and best practice rules for Micronaut applications.
 *
 * @see <a href="https://detekt.github.io/detekt/extensions.html">https://detekt.github.io/detekt/extensions.html</a>
 */
@AutoService(RuleSetProvider::class)
class MicronautExtensionProvider : RuleSetProvider {
    override val ruleSetId: String = "micronaut"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            RequireSecuredAnnotation(config),
        ),
    )
}
