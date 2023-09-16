plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libs.plugins.spotless)
}

apply(from = "../buildCompose.gradle")

ext {
    set("PUBLISH_GROUP_ID", "io.github.myounis97")
    set("PUBLISH_ARTIFACT_ID", "revealable")
    set("PUBLISH_VERSION", "1.0.2")
}

apply(from = "../publish.gradle")

android {
    namespace = "mo.younis.compose.revealable"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
}
