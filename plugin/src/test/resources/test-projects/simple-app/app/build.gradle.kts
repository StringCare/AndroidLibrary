plugins {
    id("com.android.application")
    kotlin("android")
    id("dev.vyp.stringcare.plugin")
}

android {
    namespace = "dev.vyp.stringcare.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vyp.stringcare.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

stringcare {
    debug = false
    skip = false
    stringFiles = arrayListOf("strings.xml")
    srcFolders = arrayListOf("src/main")
}
