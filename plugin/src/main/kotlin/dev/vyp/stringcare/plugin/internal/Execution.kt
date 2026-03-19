package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.models.ExecutionResult
import org.gradle.api.GradleException
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val COMMAND_TIMEOUT_SECONDS = 60L

fun execute(command: String): ExecutionResult = executeShell(command)

private fun executeShell(command: String): ExecutionResult {
    val processArgs =
        when (getOs()) {
            Os.WINDOWS -> listOf("cmd", "/c", command.normalize())
            Os.OSX, Os.LINUX -> listOf("/bin/bash", "-c", command)
        }
    val displayCommand = command
    return try {
        val process =
            ProcessBuilder(processArgs)
                .redirectErrorStream(true)
                .start()
        val output =
            process.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }.replace("\r", "")
        val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            return ExecutionResult.Failure(
                command = displayCommand,
                result = output,
                exitCode = null,
                error = GradleException("Command timed out after ${COMMAND_TIMEOUT_SECONDS}s"),
            )
        }
        val exit = process.exitValue()
        if (exit != 0) {
            ExecutionResult.Failure(
                command = displayCommand,
                result = output,
                exitCode = exit,
            )
        } else {
            ExecutionResult.Success(
                command = displayCommand,
                result = output,
                exitCode = exit,
            )
        }
    } catch (e: IOException) {
        ExecutionResult.Failure(
            command = displayCommand,
            result = "",
            exitCode = null,
            error = e,
        )
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        ExecutionResult.Failure(
            command = displayCommand,
            result = "",
            exitCode = null,
            error = e,
        )
    }
}

fun tempPath(): String {
    val dir = kotlin.io.path.createTempDirectory("stringcare-").toFile().absolutePath
    StringCareSession.installTestTempRoot(dir)
    return dir
}
