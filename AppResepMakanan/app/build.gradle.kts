plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tugasmobile.appresepmakanan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tugasmobile.appresepmakanan"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding=true
    }
}



dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("io.realm:realm-android-library:10.10.0")
    implementation ("com.schoolteachers:views:1.2.2")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.amitshekhar.android:android-networking:1.0.2")
    implementation ("com.amitshekhar.android:rx-android-networking:1.0.2")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.androidnetworking:androidnetworking:1.0.2")
    implementation ("com.github.shar-ish:ShimmerRecyclerView:v1.3")
    implementation ("com.github.devendroid:ReadMoreOption:1.0.2")
    implementation ("com.github.ivbaranov:materialfavoritebutton:0.1.5")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.1")
}