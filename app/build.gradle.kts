plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.bacancy.ccs2androidhmi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bacancy.ccs2androidhmi"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-common:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val roomVersion = "2.6.1"
    val daggerHiltVersion = "2.48.1"
    val hiltCompilerVersion = "1.1.0"
    val sdpVersion = "1.0.6"
    val sspVersion = "1.0.6"

    //Room Database
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    //hilt
    implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerHiltVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltCompilerVersion")

    //For using "by viewModels()"
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //SDP and SSP for responsive design
    implementation("com.intuit.sdp:sdp-android:$sdpVersion")
    implementation("com.intuit.ssp:ssp-android:$sspVersion")

    /*implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")*/
    implementation("com.github.hannesa2:paho.mqtt.android:3.6.4")
    implementation("androidx.hilt:hilt-work:$hiltCompilerVersion")

    implementation("androidx.fragment:fragment-ktx:1.6.2")
}