package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.domain.models.ExecutionResult
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val COMMAND_TIMEOUT_SECONDS = 60L

fun execute(command: String): Result<ExecutionResult> = executeShell(shellCommand(command), command)

fun execute(command: List<String>): Result<ExecutionResult> = executeShell(command, command.joinToString(" "))

private fun shellCommand(command: String): List<String> =
    when (getOs()) {
        Os.WINDOWS -> listOf("cmd", "/c", command.normalize())
        Os.OSX, Os.LINUX -> listOf("/bin/bash", "-c", command)
    }

private fun executeShell(
    command: List<String>,
    displayCommand: String,
): Result<ExecutionResult> {
    return try {
        val process =
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
        val output =
            process.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }.replace("\r", "")
        val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            return Result.success(
                ExecutionResult.Timeout(
                    command = displayCommand,
                    result = output,
                ),
            )
        }
        val exit = process.exitValue()
        if (exit != 0) {
            Result.success(
                ExecutionResult.Failure(
                    command = displayCommand,
                    result = output,
                    exitCode = exit,
                ),
            )
        } else {
            Result.success(
                ExecutionResult.Success(
                    command = displayCommand,
                    result = output,
                    exitCode = exit,
                ),
            )
        }
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        Result.failure(e)
    }
}

fun tempPath(): String {
    val dir = kotlin.io.path.createTempDirectory("stringcare-").toFile().absolutePath
    StringCareSession.installTestTempRoot(dir)
    return dir
}
