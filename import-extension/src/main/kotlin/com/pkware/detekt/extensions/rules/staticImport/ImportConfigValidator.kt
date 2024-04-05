package com.pkware.detekt.extensions.rules.staticImport

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Config.InvalidConfigurationError
import io.gitlab.arturbosch.detekt.api.ConfigValidator
import io.gitlab.arturbosch.detekt.api.Notification

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
    override fun validate(config: Config): Collection<Notification> {
        val result = mutableListOf<Notification>()
        try {
            config.subConfig("import")
                .subConfig("EnforceStaticImport")
                .valueOrNull<Boolean>("active")
        } catch (expected: InvalidConfigurationError) {
            result.add(Message("'active' property must be of type boolean."))
        }
        return result
    }
}

/**
 * Provides a message string and a notification level for the detekt linter.
 */
class Message(
    override val message: String,
    override val level: Notification.Level = Notification.Level.Error,
) : Notification
