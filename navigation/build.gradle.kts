import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            
            implementation(libs.kotlinx.serialization)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(project(":shared"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:details"))
            implementation(project(":feature:home"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:admin_panel"))
            implementation(project(":feature:admin_panel:manage_product"))
            implementation(project(":feature:home:categories:category_search"))
            implementation(project(":feature:home:cart:checkout"))
            implementation(project(":feature:payment_completed"))
            implementation(project(":feature:locations"))
        }
    }
}

android {
    namespace = "com.appbosna.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}