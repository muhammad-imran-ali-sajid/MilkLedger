plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android") //hilt
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.miassolutions.milkledger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.miassolutions.milkledger"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES"
            )
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")


    // finger print
    implementation("androidx.biometric:biometric:1.2.0-alpha05")


    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    // Room Database
    implementation("androidx.room:room-runtime:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:5.0.5") // Assumed latest stable version
    ksp("com.github.bumptech.glide:compiler:5.0.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //data store
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")

    // fragment for view model code generation
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    // DocumentFile is a wrapper class in AndroidX that helps you interact with files and folders
    implementation("androidx.documentfile:documentfile:1.1.0")

    // Required for Google Sign-In (modern)
    implementation("com.google.android.gms:play-services-auth:21.4.0")

// Official Drive REST API client (not deprecated)
    implementation("com.google.api-client:google-api-client-android:2.8.1")
    // Google Drive REST API client library
    implementation("com.google.apis:google-api-services-drive:v3-rev20250220-2.0.0")

    implementation("com.google.api-client:google-api-client-android:2.8.1")
}