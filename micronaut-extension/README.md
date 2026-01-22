# Micronaut Extension for Detekt

A detekt extension that provides security and best practice rules for Micronaut applications.

## Rules

### RequireSecuredAnnotation

**Severity:** Security
**Debt:** 5 minutes

Ensures that all Micronaut controller endpoint methods have security annotations to prevent accidentally creating unsecured endpoints.

#### Description

This rule checks that every method annotated with HTTP method annotations (`@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`, `@Trace`) also has one of the following security annotations:

- `@Secured` (io.micronaut.security.annotation.Secured)
- `@PermitAll` (jakarta.annotation.security.PermitAll)
- `@RolesAllowed` (jakarta.annotation.security.RolesAllowed)
- `@DenyAll` (jakarta.annotation.security.DenyAll)

#### Noncompliant Code

```kotlin
@Controller("/api/users")
class UserController {
    // ❌ Missing security annotation
    @Get("/{id}")
    fun getUser(id: String): User {
        return userService.findById(id)
    }
}
```

#### Compliant Code

```kotlin
@Controller("/api/users")
class UserController {
    // ✅ Has @Secured annotation
    @Secured("ROLE_USER")
    @Get("/{id}")
    fun getUser(id: String): User {
        return userService.findById(id)
    }

    // ✅ Public endpoint explicitly marked with @PermitAll
    @PermitAll
    @Get("/public")
    fun getPublicInfo(): PublicInfo {
        return publicInfoService.getInfo()
    }

    // ✅ Admin-only endpoint
    @RolesAllowed("ADMIN")
    @Delete("/{id}")
    fun deleteUser(id: String) {
        userService.delete(id)
    }

    // ✅ Explicitly denied
    @DenyAll
    @Get("/forbidden")
    fun forbiddenEndpoint() {
        // This endpoint is always forbidden
    }
}
```

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    detektPlugins("com.pkware.detekt:micronaut-extension:1.3.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    detektPlugins 'com.pkware.detekt:micronaut-extension:1.3.0'
}
```

## Configuration

Add to your `detekt.yml`:

```yaml
micronaut:
  RequireSecuredAnnotation:
    active: true
```

## Suppressing the Rule

If you need to suppress this rule for a specific method:

```kotlin
@Suppress("RequireSecuredAnnotation")
@Get("/special-case")
fun specialCase() {
    // This endpoint won't be checked
}
```

## Why This Rule Matters

Accidentally exposing unsecured endpoints is a common security vulnerability. This rule helps prevent:

- **Data leaks**: Sensitive data exposed through unsecured endpoints
- **Unauthorized operations**: Critical operations (delete, update) accessible without authentication
- **Privilege escalation**: Admin-only endpoints accessible to regular users

By requiring explicit security annotations on all endpoints, this rule enforces a "secure by default" approach where developers must consciously choose the security level for each endpoint.

## References

- [Micronaut Security Documentation](https://micronaut-projects.github.io/micronaut-security/latest/guide/)
- [Jakarta Security Annotations](https://jakarta.ee/specifications/security/)
