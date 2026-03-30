package com.pkware.detekt.extensions.rules.staticImport

import com.google.auto.service.AutoService
import dev.detekt.api.Config
import dev.detekt.api.Config.InvalidConfigurationError
import dev.detekt.api.ConfigValidator
import dev.detekt.api.Notification

/**
 * Validates the config setup for the detekt custom import ruleset within detekt.yml.
 *
 * The format of the config should be as follows:
 *
 *  ```yml
 * import:
 *   EnforceStaticImport:
 *     active: true #true for enabled, false for disabled
 *     methods: #optional methods to statically import
 *       - 'com.google.common.truth.Truth.assertThat'
 *       - 'org.junit.jupiter.params.provider.Arguments.arguments'
 * ```
 */
@AutoService(ConfigValidator::class)
class ImportConfigValidator : ConfigValidator {
    override val id: String = "ImportConfigValidator"

    override fun validate(config: Config): Collection<Notification> {
        val result = mutableListOf<Notification>()
        try {
            config.subConfig("import")
                .subConfig("EnforceStaticImport")
                .valueOrNull("active")
        } catch (expected: InvalidConfigurationError) {
            result.add(Notification("'active' property must be of type boolean.", Notification.Level.Error))
        }
        return result
    }
}
