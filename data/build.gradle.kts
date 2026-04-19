// Data module - data sources, repositories, and DI
plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

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

    // NOTE: Flavors are CREATED once in root build.gradle.kts (contract §12.1).
    // This block does NOT re-declare them — it only adds BASE_URL BuildConfig field
    // to each already-created flavor. Only the data module needs BASE_URL.
    // OFFLINE_QUEUE_ENABLED=false for all v1 flavors (contract M-13).
    productFlavors {
        getByName("local") {
            buildConfigField("String",  "BASE_URL",              "\"http://10.0.2.2:8080/api/v1/\"")
            buildConfigField("Boolean", "OFFLINE_QUEUE_ENABLED", "false")
        }
        getByName("dev") {
            buildConfigField("String",  "BASE_URL",              "\"https://billsmart-uwad.onrender.com/api/v1/\"")
            buildConfigField("Boolean", "OFFLINE_QUEUE_ENABLED", "false")
        }
        getByName("uat") {
            buildConfigField("String",  "BASE_URL",              "\"https://billsmart-uwad.onrender.com/api/v1/\"")
            buildConfigField("Boolean", "OFFLINE_QUEUE_ENABLED", "false")
        }
        getByName("prod") {
            buildConfigField("String",  "BASE_URL",              "\"https://billsmart-uwad.onrender.com/api/v1/\"")
            buildConfigField("Boolean", "OFFLINE_QUEUE_ENABLED", "false")
        }
    }

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

    // WorkManager + Hilt-Work for SyncWorker (Phase 9.2 — offline sync)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Hilt dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)

    // Chucker — HTTP inspector UI for dev flavor only.
    // no-op variant for local/uat/prod ensures zero overhead in non-dev builds.
    "devImplementation"(libs.chucker)
    "localImplementation"(libs.chucker.no.op)
    "uatImplementation"(libs.chucker.no.op)
    "prodImplementation"(libs.chucker.no.op)
}

