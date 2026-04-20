// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SmartPos Multi-Client + Multi-Environment Flavor System
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * TWO flavor dimensions are applied to EVERY Android module (library + app)
 * from this single place. This guarantees:
 *   • All modules always use the same variant — you cannot accidentally
 *     run app on "acmeCafeDev" while domain is on "commonProd".
 *   • Android Studio's variant selector only appears on the :app module;
 *     all library modules follow it automatically.
 *
 * Dimension 1 — "client"  (first entry = default)
 *   smartPos      ★ DEFAULT — standard SmartPos build, no client customisation
 *   jevnarSweets  → Jevnar Sweets client
 *   (add more clients here as you onboard them)
 *
 * Dimension 2 — "environment"  (first entry = default)
 *   dev   ★ DEFAULT — development / daily testing server
 *   uat   → user-acceptance testing server
 *   prod  → live production server
 *
 * Resulting build variants (× debug/release build types) — e.g.:
 *   smartPosDevDebug         smartPosDevRelease
 *   smartPosUatDebug         smartPosUatRelease
 *   smartPosProdDebug        smartPosProdRelease
 *   jevnarSweetsDevDebug     jevnarSweetsDevRelease
 *   jevnarSweetsUatDebug     ...
 *   jevnarSweetsProdRelease  ...
 *
 * To ADD a new client:
 *   1. Add an entry in CLIENT_FLAVORS below.
 *   2. Add its BASE_URL entries in data/build.gradle.kts (CLIENT_BASE_URLS map).
 *   3. Create app/src/<clientName>/ source set for custom icons / resources / code.
 *
 * To ADD a new environment:
 *   1. Add an entry in ENV_FLAVORS below.
 *   2. Add matching BASE_URL entries in data/build.gradle.kts.
 * ─────────────────────────────────────────────────────────────────────────────
 */

/** Names of every client flavor. Add new clients here. The FIRST entry is the default. */
val CLIENT_FLAVORS = listOf("smartPos", "jevnarSweets")

/** Names of every environment flavor. The FIRST entry is the default. */
val ENV_FLAVORS = listOf("dev", "uat", "prod")

/**
 * Default variant used by library modules when no specific variant is requested
 * (e.g. local Maven publishing, composite builds, IDE resolution).
 * Must be: <firstClientFlavor><firstEnvFlavor capitalized>Debug
 */
val DEFAULT_PUBLISH_CONFIG = "smartPosDevDebug"

fun com.android.build.gradle.BaseExtension.applySmartPosFlavors() {
    // Both dimensions must be declared before any productFlavors block.
    flavorDimensions("client", "environment")

    productFlavors {
        // ── client dimension ────────────────────────────────────────────────
        CLIENT_FLAVORS.forEach { clientName ->
            create(clientName) { dimension = "client" }
        }

        // ── environment dimension ────────────────────────────────────────────
        ENV_FLAVORS.forEach { envName ->
            create(envName) { dimension = "environment" }
        }
    }
}

subprojects {
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            applySmartPosFlavors()
            // When a consumer doesn't specify a variant (e.g. local publish, IDE),
            // always fall back to the smartPos default.
            defaultPublishConfig = DEFAULT_PUBLISH_CONFIG
        }
    }
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.gradle.AppExtension> {
            applySmartPosFlavors()
        }
    }
}
