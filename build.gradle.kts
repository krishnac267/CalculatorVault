plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
}

subprojects {
    plugins.withId("com.android.library") {
        afterEvaluate {
            tasks.matching { it.name.startsWith("connected") && it.name.endsWith("AndroidTest") }.configureEach {
                val androidTestDir = project.file("src/androidTest")
                val hasInstrumentedTests = androidTestDir.exists() &&
                    androidTestDir.walk().any { it.extension == "kt" || it.extension == "java" }
                enabled = hasInstrumentedTests
            }
        }
    }
}
