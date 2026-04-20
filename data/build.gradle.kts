// Data module - data sources, repositories, and DI
plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * BASE_URL configuration — per client × per environment.
 *
 * Structure:
 *   CLIENT_BASE_URLS[clientName][environmentName] = "https://..."
 *
 * Fallback chain (first non-null wins):
 *   1. Exact client + env match  (e.g. acmeCafe + prod)
 *   2. "common" + env match      (shared default for that environment)
 *   3. Hard-coded sentinel URL   (build will succeed but network will fail loudly)
 *
 * HOW TO ADD A NEW CLIENT:
 *   Add a new map entry below, e.g.:
 *       "rajResorts" to mapOf(
 *           "dev"  to "https://rajresorts-dev.example.com/api/v1/",
 *           "uat"  to "https://rajresorts-uat.example.com/api/v1/",
 *           "prod" to "https://rajresorts.example.com/api/v1/"
 *       ),
 * ─────────────────────────────────────────────────────────────────────────────
 */
val CLIENT_BASE_URLS: Map<String, Map<String, String>> = mapOf(

    // ── SmartPos — standard build (default for all clients with no customisation)
    // Also used as the fallback when a client entry is missing for a given environment.
    "smartPos" to mapOf(
        "dev"  to "https://billsmart-uwad.onrender.com/api/v1/",
        "uat"  to "https://billsmart-uwad.onrender.com/api/v1/",
        "prod" to "https://billsmart-api.onrender.com/api/v1/"
    ),

    // ── Jevnar Sweets ────────────────────────────────────────────────────────
    // Replace these with Jevnar Sweets' real server addresses when available.
    "jevnarSweets" to mapOf(
        "dev"  to "https://jevnarsweets-dev.example.com/api/v1/",
        "uat"  to "https://jevnarsweets-uat.example.com/api/v1/",
        "prod" to "https://jevnarsweets.example.com/api/v1/"
    )

    // ── Add new clients below ────────────────────────────────────────────────
    // "rajResorts" to mapOf(
    //     "dev"  to "https://rajresorts-dev.example.com/api/v1/",
    //     "uat"  to "https://rajresorts-uat.example.com/api/v1/",
    //     "prod" to "https://rajresorts.example.com/api/v1/"
    // ),
)

android {
    namespace = "com.autobill.smartpos.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        debug { }
        release { }
    }

    // productFlavors are declared by root build.gradle.kts via subprojects {}.
    // This module does NOT re-declare them.

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

/**
 * Inject BASE_URL and OFFLINE_QUEUE_ENABLED into BuildConfig for every
 * client × environment variant combination.
 *
 * androidComponents.onVariants fires once per fully-resolved variant, giving us
 * access to both flavor dimensions simultaneously — this is the only place in
 * Gradle where you can set BuildConfig fields for arbitrary flavor combinations
 * without duplicating code.
 */
androidComponents {
    onVariants { variant ->
        // Extract the two flavor dimension values for this variant.
        val client = variant.productFlavors
            .firstOrNull { (dimension, _) -> dimension == "client" }?.second ?: "common"
        val env = variant.productFlavors
            .firstOrNull { (dimension, _) -> dimension == "environment" }?.second ?: "dev"

        // Resolve BASE_URL: client-specific → smartPos fallback → sentinel.
        val baseUrl = CLIENT_BASE_URLS[client]?.get(env)
            ?: CLIENT_BASE_URLS["smartPos"]?.get(env)
            ?: "https://MISSING_BASE_URL_CONFIGURE_IN_DATA_BUILD_GRADLE/"

        variant.buildConfigFields.put(
            "BASE_URL",
            com.android.build.api.variant.BuildConfigField(
                "String", "\"$baseUrl\"",
                "API base URL for client=$client env=$env"
            )
        )

        // Offline write queue is disabled for all v1 variants (contract M-13).
        // Flip to true only when the backend conflict-recovery story is complete.
        variant.buildConfigFields.put(
            "OFFLINE_QUEUE_ENABLED",
            com.android.build.api.variant.BuildConfigField(
                "Boolean", "false",
                "Offline write queue — disabled in v1 (contract M-13)"
            )
        )
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)

    // Chucker HTTP inspector — real library in debug builds, no-op in release.
    // This covers all client×environment variants automatically.
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}

