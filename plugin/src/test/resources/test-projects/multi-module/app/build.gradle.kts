plugins {
    id("com.android.application")
    kotlin("android")
    id("dev.vyp.stringcare.plugin")
}

android {
    namespace = "dev.vyp.stringcare.multimodule.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vyp.stringcare.multimodule.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":feature"))
}

stringcare {
    debug = false
    skip = false
    stringFiles = arrayListOf("strings.xml")
    srcFolders = arrayListOf("src/main")
}
