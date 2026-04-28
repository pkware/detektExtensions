# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build, test, and lint everything
./gradlew build

# Format Kotlin code (use this, not ktlintCheck)
./gradlew ktlintFormat

# Run all tests for a specific module
./gradlew :import-extension:test
./gradlew :micronaut-extension:test

# Run a specific test class
./gradlew :import-extension:test --tests "com.pkware.detekt.extensions.rules.staticImport.EnforceStaticImportTest"

# Run a specific test method (supports wildcards)
./gradlew :import-extension:test --tests "*.EnforceStaticImportTest.rule*"
```

## Architecture

This is a Gradle multi-module project publishing two independent detekt rule extensions to Maven Central under `com.pkware.detekt`.

### Modules

- **`import-extension`** — `EnforceStaticImport` rule: reports method calls that should be statically imported. Requires type resolution (`RequiresAnalysisApi`).
- **`micronaut-extension`** — `RequireSecuredAnnotation` rule: ensures Micronaut `@Controller` endpoint methods carry a security annotation. PSI-only (no type resolution needed).

### Adding a New Rule

Each rule follows this three-part structure:

1. **Rule class** in `src/main/kotlin/.../rules/<domain>/` — extends `Rule(config, "message")`. If type information is needed (e.g., resolving overloads), also implement `RequiresAnalysisApi` and call `lintWithContext` in tests.

2. **RuleSetProvider** in the same package — annotated `@AutoService(RuleSetProvider::class)`, registers the rule by name. One provider per module.

3. **Test class** in `src/test/kotlin/` — uses detekt's test utilities. For rules that implement `RequiresAnalysisApi`, create a `KotlinEnvironmentContainer` in `@BeforeEach` and call `lintWithContext(env, code)`. For PSI-only rules, `lint(code)` suffices.

### Type Resolution vs PSI-only

- `RequiresAnalysisApi` + `lintWithContext` → resolves actual types, handles overloads and overrides. Required when annotation names alone are ambiguous or when matching method signatures.
- PSI-only + `lint` → checks annotation/function names as strings. Simpler, but can produce false positives/negatives if names collide across packages.

### Configuration

Rules expose config via `@Configuration`-annotated properties delegated to `by config(...)`. They are activated in `detekt.yml` under their `RuleSetId`:

```yaml
import:
  EnforceStaticImport:
    active: true
    methods:
      - 'com.google.common.truth.Truth.assertThat'
micronaut:
  RequireSecuredAnnotation:
    active: true
```

## Key Conventions

- All dependencies go in `gradle/libs.versions.toml` — never directly in `build.gradle.kts`.
- Commits must follow [Conventional Commits](https://www.conventionalcommits.org/): `feat`, `fix`, `chore`, `deps`, `docs`, `ci`, `test`, `refactor`. Each commit lands verbatim on `main` via rebase+merge.
- `RequireSecuredAnnotation` checks security annotations **per-method only** — class-level `@Secured` is intentionally not accepted (each endpoint must be explicitly annotated).
- KDoc is required on all public declarations. Focus on *why* the code is structured as it is.
- No section-separator comments — use `@Nested` classes in tests to group related cases.