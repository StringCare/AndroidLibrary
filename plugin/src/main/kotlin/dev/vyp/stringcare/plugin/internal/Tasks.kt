package dev.vyp.stringcare.plugin.internal

import java.io.File

internal fun signingReportTask(): String =
    "${when (getOs()) {
        Os.WINDOWS -> wrapperWindows
        Os.OSX, Os.LINUX -> wrapperOsX
    }} signingReport"

/** `gradlew` in the current working directory (e.g. cloned KotlinSample). */
internal fun projectGradleWrapper(): String =
    when (getOs()) {
        Os.WINDOWS -> wrapperWindows
        Os.OSX, Os.LINUX -> wrapperOsX
    }

/** Monorepo wrapper when `user.dir` is `:plugin` — root project is parent directory. */
internal fun repoRootGradle(): String =
    when (getOs()) {
        Os.WINDOWS -> "..\\gradlew.bat -p .."
        Os.OSX, Os.LINUX -> "sh ../gradlew -p .."
    }

internal fun pluginBuildTask(): String =
    "${repoRootGradle()} :plugin:compileKotlin --no-daemon"

/**
 * Ensures JNI prebuilts exist on the test runtime classpath under `macos/`, `linux/`, `windows/`
 * (same layout as [processResources]). Quotes paths for spaces/special chars in absolute working dirs.
 */
internal val librarySetupTask =
    """
    mkdir -p build/resources/main/macos build/resources/main/linux build/resources/main/windows &&
    ${copyCommand()} "src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni/macos/$osxLib" "build/resources/main/macos/" &&
    ${copyCommand()} "src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni/linux/$linuxLib" "build/resources/main/linux/" &&
    ${copyCommand()} "src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni/linux/$linuxLibArm64" "build/resources/main/linux/" &&
    ${copyCommand()} "src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni/windows/$winLib" "build/resources/main/windows/" &&
    ${copyCommand()} "src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni/windows/$winLibArm64" "build/resources/main/windows/"
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
    ${projectGradleWrapper()} build
    """.trimIndent()

internal fun basicGradleTask(directory: String): String =
    """
    cd $directory &&
    ${projectGradleWrapper()} $gradleTaskNameDoctor
    """.trimIndent()

internal fun obfuscationTestGradleTask(directory: String): String =
    """
    cd $directory &&
    ${projectGradleWrapper()} $gradleTaskNameObfuscate
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
