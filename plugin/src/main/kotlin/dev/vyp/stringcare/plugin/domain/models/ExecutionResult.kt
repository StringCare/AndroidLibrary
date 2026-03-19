package dev.vyp.stringcare.plugin.domain.models

/** Result of running an external command (e.g. Gradle wrapper / shell script). */
sealed class ExecutionResult {
    data class Success(
        val command: String,
        val result: String,
        val exitCode: Int = 0,
    ) : ExecutionResult()

    data class Failure(
        val command: String,
        val result: String,
        val exitCode: Int?,
        val error: Throwable? = null,
    ) : ExecutionResult()

    data class Timeout(
        val command: String,
        val result: String,
    ) : ExecutionResult()
}
