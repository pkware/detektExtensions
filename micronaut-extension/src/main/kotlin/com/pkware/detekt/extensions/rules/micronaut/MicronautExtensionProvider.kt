package com.pkware.detekt.extensions.rules.micronaut

import com.google.auto.service.AutoService
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

/**
 * The Micronaut extension rule set provider for this detekt extension.
 * Provides security and best practice rules for Micronaut applications.
 *
 * @see <a href="https://detekt.github.io/detekt/extensions.html">https://detekt.github.io/detekt/extensions.html</a>
 */
@AutoService(RuleSetProvider::class)
class MicronautExtensionProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("micronaut")

    override fun instance(): RuleSet =
        RuleSet(ruleSetId, mapOf(RuleName("RequireSecuredAnnotation") to ::RequireSecuredAnnotation))
}
