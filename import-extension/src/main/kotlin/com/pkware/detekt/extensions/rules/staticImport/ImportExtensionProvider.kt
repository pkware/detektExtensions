package com.pkware.detekt.extensions.rules.staticImport

import com.google.auto.service.AutoService
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

/**
 * The import extension rule set provider for this detekt extension.
 * @see <a href="https://detekt.github.io/detekt/extensions.html">https://detekt.github.io/detekt/extensions.html</a>
 */
@AutoService(RuleSetProvider::class)
class ImportExtensionProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("import")

    override fun instance(): RuleSet =
        RuleSet(ruleSetId, mapOf(RuleName("EnforceStaticImport") to ::EnforceStaticImport))
}
