
import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Load keystore.properties (DO NOT commit this file)
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("io.kotzilla.kotzilla-plugin")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Lifecycle
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)

                // Dependency Injection
                implementation(libs.koin.compose)

                api(libs.kmp.notifier)

                // Kotzilla SDK
                implementation(libs.kotzilla.sdk.ktor3)

                // Local modules
                implementation(project(path = ":navigation"))
                implementation(project(path = ":shared"))
                implementation(project(path = ":di"))
                implementation(project(path = ":data"))
            }
        }

        val androidMain by getting {
            dependencies {
                // Compose Android
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Dependency Injection
                implementation(libs.koin.android)
            }
        }
    }
}

android {
    namespace = "com.appbosna.naruciklpi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.appbosna.naruciklpi"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // ✅ Release signing (uses upload-keystore.jks via keystore.properties)
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        // debug ostaje default (debug keystore automatski)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

configurations.matching { it.name.contains("release", ignoreCase = true) }
    .configureEach {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
