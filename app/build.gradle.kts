plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.calculator.vault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.calculator.vault"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "com.calculator.vault.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/2247696110\"")
        buildConfigField("String", "PREMIUM_MONTHLY_ID", "\"premium_monthly\"")
        buildConfigField("String", "PREMIUM_YEARLY_ID", "\"premium_yearly\"")
        buildConfigField("String", "PREMIUM_LIFETIME_ID", "\"premium_lifetime\"")
        buildConfigField("int", "FREE_VAULT_APP_LIMIT", "3")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val releaseStoreFile = project.findProperty("RELEASE_STORE_FILE") as String?
            val releaseStorePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            val releaseKeyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            val releaseKeyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
            if (
                releaseStoreFile != null &&
                releaseStorePassword != null &&
                releaseKeyAlias != null &&
                releaseKeyPassword != null
            ) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = file(releaseStoreFile)
                    storePassword = releaseStorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            // Default Android debug keystore
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":security"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.play.billing)
    implementation(libs.play.services.ads)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.testrules)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(libs.androidx.biometric)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.kotlinx.coroutines.android)
    androidTestUtil(libs.androidx.test.orchestrator)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
