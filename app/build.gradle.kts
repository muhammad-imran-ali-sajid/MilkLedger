plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android") //hilt
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.miassolutions.milkledger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.miassolutions.milkledger"
        minSdk = 27
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
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":Sort-Filter"))
    implementation(project(":datesort"))


    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))


    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.code.gson:gson:2.13.2")


    val work_version = "2.11.0"
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // finger print
    implementation("androidx.biometric:biometric:1.2.0-alpha05")


    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    // Room Database
    implementation("androidx.room:room-runtime:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")


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

}