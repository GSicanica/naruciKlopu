plugins {
    // centralizovano — sprječava višestruko učitavanje u submodulima
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

detekt {
    config.setFrom(files("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    ignoreFailures = false   // ❗ build će pasti ako ima Detekt grešaka
    autoCorrect = true
    parallel = true          // koristi više threadova
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}

// 🔥 Ovo je ključni dio: vežemo Detekt na svaki build
gradle.projectsEvaluated {
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("detekt")
    }
}

// (opcionalno) ako koristiš lint
gradle.projectsEvaluated {
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("lint")
    }
}

// (opcionalno) za CI integraciju (GitHub Actions, GitLab, itd.)
tasks.register("checkQuality") {
    group = "verification"
    description = "Pokreće Detekt i Lint zajedno"
    dependsOn("detekt", "lint")
}

buildscript {
    dependencies {
        classpath(libs.kotzilla.plugin)
    }
}
