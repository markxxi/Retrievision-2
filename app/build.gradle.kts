plugins {
    alias(libs.plugins.android.application)
    id("com.chaquo.python")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.retrievision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.retrievision"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86", "x86_64")
        }
        signingConfig = signingConfigs.getByName("debug")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        mlModelBinding = true
    }
    packagingOptions{
        exclude("META-INF/DEPENDENCIES")
    }
    flavorDimensions += "pyVersion"
    productFlavors {
        create("py311") { dimension = "pyVersion" }
    }
}

chaquopy {
    defaultConfig {
        buildPython("C:/Users/Spencer/AppData/Local/Programs/Python/Python311/python.exe")
        //buildPython("C:/Users/MSI/AppData/Local/Microsoft/WindowsApps/python3.11.exe")
        version = "3.11"

        pip{
            install("pillow")
            install("colorthief")
//            install("matplotlib")
//            install("argparse")
            install("webcolors")


        }
    }
    productFlavors {getByName("py311") { version = "3.11" } }
    sourceSets {
        getByName("main") {
            srcDir("src/main/python")
        }
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.google.android.material:material:1.5.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.fornewid:neumorphism:0.3.2")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation(kotlin("script-runtime"))
}