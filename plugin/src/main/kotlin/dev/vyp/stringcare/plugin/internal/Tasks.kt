package dev.vyp.stringcare.plugin.internal

import java.io.File

internal fun signingReportTask(): String =
    "${when (getOs()) {
        Os.WINDOWS -> wrapperWindows
        Os.OSX, Os.LINUX -> wrapperOsX
    }} signingReport"

internal fun gradleWrapper(): String =
    when (getOs()) {
        Os.WINDOWS -> wrapperWindows
        Os.OSX, Os.LINUX -> wrapperOsX
    }

internal fun pluginBuildTask(): String =
    "${when (getOs()) {
        Os.WINDOWS -> wrapperWindows
        Os.OSX, Os.LINUX -> wrapperOsX
    }} build --exclude-task test"

internal val librarySetupTask =
    """
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$osxLib out${File.separator}production${File.separator}classes${File.separator}$osxLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$linuxLib out${File.separator}production${File.separator}classes${File.separator}$linuxLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$linuxLibArm64 out${File.separator}production${File.separator}classes${File.separator}$linuxLibArm64 &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$winLib out${File.separator}production${File.separator}classes${File.separator}$winLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$winLibArm64 out${File.separator}production${File.separator}classes${File.separator}$winLibArm64 &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$osxLib build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$osxLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$linuxLib build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$linuxLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$linuxLibArm64 build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$linuxLibArm64 &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$winLib build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$winLib &
    ${copyCommand()} src${File.separator}main${File.separator}kotlin${File.separator}dev${File.separator}vyp${File.separator}stringcare${File.separator}plugin${File.separator}internal${File.separator}jni${File.separator}$winLibArm64 build${File.separator}classes${File.separator}kotlin${File.separator}main${File.separator}$winLibArm64
    """.trimIndent()

internal fun prepareTask(directory: String): String =
    """
    cd $directory &&
    git clone https://github.com/StringCare/$testProjectName.git &&
    cd $testProjectName
    """.trimIndent()

internal fun buildTask(directory: String): String =
    """
    cd $directory &&
    ${gradleWrapper()} build
    """.trimIndent()

internal fun basicGradleTask(directory: String): String =
    """
    cd $directory &&
    ${gradleWrapper()} $gradleTaskNameDoctor
    """.trimIndent()

internal fun obfuscationTestGradleTask(directory: String): String =
    """
    cd $directory &&
    ${gradleWrapper()} $gradleTaskNameObfuscate
    """.trimIndent()

internal fun signingReportTask(directory: String): String =
    """
    ${prepareTask(directory)} &&
    ${signingReportTask()}
    """.trimIndent()

internal fun copyCommand(): String =
    when (getOs()) {
        Os.WINDOWS -> copyCommandWindows
        Os.OSX, Os.LINUX -> copyCommandOsX
    }
