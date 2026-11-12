plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.hvktmm.bach.securitylab"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.hvktmm.bach.securitylab.rest"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-lab"
    }

    flavorDimensions += "security"
    productFlavors {
        create("vulnerable") {
            dimension = "security"
            buildConfigField("boolean", "VULNERABLE_MODE", "true")
            applicationIdSuffix = ".vulnerable"
            versionNameSuffix = "-vulnerable"
        }
        create("hardened") {
            dimension = "security"
            buildConfigField("boolean", "VULNERABLE_MODE", "false")
            applicationIdSuffix = ".hardened"
            versionNameSuffix = "-hardened"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Quét và Tạo mã QR
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    testImplementation("junit:junit:4.13.2")
}
