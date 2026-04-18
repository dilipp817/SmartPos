// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

/**
 * Centralized environment flavor config — contract §12.1 (M-03).
 *
 * Applied automatically to every Android module (library + application) via subprojects {}.
 * No individual module needs to declare flavorDimensions or productFlavors.
 *
 * Resulting build variants:
 *   localDebug, devDebug, uatDebug, prodDebug
 *   localRelease, devRelease, uatRelease, prodRelease
 */
fun com.android.build.gradle.BaseExtension.applyEnvironmentFlavors() {
    flavorDimensions("environment")
    productFlavors.create("local") { dimension = "environment" }
    productFlavors.create("dev")   { dimension = "environment" }
    productFlavors.create("uat")   { dimension = "environment" }
    productFlavors.create("prod")  { dimension = "environment" }
}

subprojects {
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            applyEnvironmentFlavors()
        }
    }
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.gradle.AppExtension> {
            applyEnvironmentFlavors()
        }
    }
}
