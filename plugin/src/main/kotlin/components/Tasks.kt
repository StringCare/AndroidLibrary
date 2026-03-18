package components

import java.io.File

internal fun signingReportTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}} signingReport"

internal fun gradleWrapper(): String = when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}

internal fun pluginBuildTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}} build --exclude-task test"

internal val librarySetupTask = """
            ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}components${File.separator}jni${File.separator}$osxLib out${File.separator}production${File.separator}classes${File.separator}$osxLib &
            ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}components${File.separator}jni${File.separator}$winLib out${File.separator}production${File.separator}classes${File.separator}$winLib &
            ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}components${File.separator}jni${File.separator}$osxLib build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$osxLib &
            ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}components${File.separator}jni${File.separator}$winLib build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$winLib
        """.trimIndent()

internal fun prepareTask(directory: String): String {
    return """
            cd $directory &&
            git clone https://github.com/StringCare/$testProjectName.git &&
            cd $testProjectName
        """.trimIndent()
}

internal fun buildTask(directory: String): String {
    return """
        cd $directory &&
        ${gradleWrapper()} build
        """.trimIndent()
}

internal fun basicGradleTask(directory: String): String {
    return """
        cd $directory &&
        ${gradleWrapper()} $gradleTaskNameDoctor
        """.trimIndent()
}

internal fun obfuscationTestGradleTask(directory: String): String {
    return """
        cd $directory &&
        ${gradleWrapper()} $gradleTaskNameObfuscate
        """.trimIndent()
}

// gradlew task needs export ANDROID_SDK_ROOT=/Users/efrainespada/Library/Android/sdk
// echo "sdk.dir=${System.getenv("ANDROID_SDK_ROOT")}" > local.properties &&
internal fun signingReportTask(directory: String): String {
    return """
            ${prepareTask(directory)} &&
            ${signingReportTask()}
        """.trimIndent()
}

internal fun copyCommand(): String = when (getOs()) {
    Os.WINDOWS -> copyCommandWindows
    Os.OSX -> copyCommandOsX
}


