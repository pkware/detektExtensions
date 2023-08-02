package com.pkware.detekt.extensions.rules.staticImport

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * The import extension rule set provider for this detekt extension.
 * @see <a href="https://detekt.github.io/detekt/extensions.html">https://detekt.github.io/detekt/extensions.html</a>
 */
@AutoService(RuleSetProvider::class)
class ImportExtensionProvider : RuleSetProvider {

    override val ruleSetId: String = "import"

    override fun instance(config: Config): RuleSet = RuleSet(ruleSetId, listOf(EnforceStaticImport(config)))
}
