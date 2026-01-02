rootProject.name = "naruciklpi"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
include(":data")
include(":di")
include(":feature:admin_panel")
include(":feature:admin_panel:manage_product")
include(":feature:details")
include(":feature:home:cart")
include(":feature:home:cart:checkout")
include(":feature:home:categories")
include(":feature:home:categories:category_search")
include(":feature:home:products_overview")
include(":feature:payment_completed")
include(":feature:home")
include(":feature:profile")
// include(":feature:product_details")  // ❌ Disabled - build errors, not used
include(":feature:auth")
include(":navigation")
include(":composeApp")
include(":shared")
include(":feature:locations")
