plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.autobill.smartpos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.autobill.smartpos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    /**
     * ─────────────────────────────────────────────────────────────────────────
     * Client flavors — app-level customisation per client.
     *
     * Each client flavor here MUST match a name declared in the root
     * build.gradle.kts CLIENT_FLAVORS list.
     *
     * Per-client customisation available here:
     *   • applicationId suffix  — each client gets its own Play Store listing
     *   • resValue "app_name"   — the label shown on the launcher
     *   • versionName suffix    — e.g. "1.0-acme"
     *   • signingConfig         — each client can have its own keystore
     *
     * Per-client custom resources (icons, splash, colours, strings):
     *   Place files under app/src/<clientName>/res/
     *   e.g. app/src/acmeCafe/res/mipmap-xxxhdpi/ic_launcher.png
     *        app/src/acmeCafe/res/values/colors.xml
     *   These automatically override the defaults in app/src/main/res/.
     *
     * Per-client custom Kotlin/Java source code:
     *   Place files under app/src/<clientName>/kotlin/
     *   e.g. app/src/acmeCafe/kotlin/com/autobill/smartpos/ClientConfig.kt
     *
     * Per-client custom features in feature modules:
     *   Place files under feature/<module>/src/<clientName>/
     * ─────────────────────────────────────────────────────────────────────────
     */
    productFlavors {
        // ── client dimension ────────────────────────────────────────────────

        /**
         * smartPos — the standard SmartPos build.
         * Ships to clients who want the default experience with no customisation.
         * No applicationId suffix so the base package stays clean.
         */
        getByName("smartPos") {
            dimension = "client"
            // applicationId stays as defaultConfig.applicationId
            resValue("string", "app_name", "SmartPos")
        }

        /**
         * jevnarSweets — Jevnar Sweets client.
         *
         * Steps to fully set up a new client (use this as a template):
         *   1. Duplicate this block with the new client name.
         *   2. Add the name to CLIENT_FLAVORS in root build.gradle.kts.
         *   3. Add BASE_URL entries in data/build.gradle.kts CLIENT_BASE_URLS.
         *   4. Create app/src/<clientName>/res/ with the client's icons & branding.
         */
        getByName("jevnarSweets") {
            dimension = "client"
            applicationIdSuffix = ".jevnarsweets"      // com.autobill.smartpos.jevnarsweets
            resValue("string", "app_name", "Jevnar Sweets POS")
            versionNameSuffix = "-js"
        }

        // ── environment dimension ────────────────────────────────────────────
        getByName("dev")  { dimension = "environment" }
        getByName("uat")  { dimension = "environment" }
        getByName("prod") { dimension = "environment" }
    }

    /**
     * Source sets — per-client resource / code folders.
     *
     * app/src/smartPos/     — resources for the standard SmartPos client
     * app/src/jevnarSweets/ — branding overrides for Jevnar Sweets
     *
     * Files here are merged with app/src/main/ at build time.
     * Client files win over main/ files when names clash.
     */
    sourceSets {
        getByName("smartPos")     { res.srcDirs("src/smartPos/res");     java.srcDirs("src/smartPos/kotlin") }
        getByName("jevnarSweets") { res.srcDirs("src/jevnarSweets/res"); java.srcDirs("src/jevnarSweets/kotlin") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":ui-components"))
    implementation(project(":feature:food"))
    implementation(project(":feature:table"))
    implementation(project(":feature:order"))
    implementation(project(":feature:billing"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:admin"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt dependency injection - now compatible with AGP 8.3
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // WorkManager + Hilt-Work for offline sync scheduling (Phase 9.2)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}