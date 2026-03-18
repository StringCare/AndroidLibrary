package components

import StringCare
import com.google.common.io.Files
import models.ExecutionResult
import java.io.IOException

fun execute(command: String): ExecutionResult = execute(Runtime.getRuntime(), command)

private fun execute(runtime: Runtime, command: String): ExecutionResult {
    return try {
        when (getOs()) {
            Os.WINDOWS -> {
                val process = runtime.exec(
                    arrayOf(
                        "cmd",
                        "/c",
                        command.normalize()
                    )
                )
                ExecutionResult(
                    command.normalize(),
                    process.outputString()
                )
            }
            Os.OSX -> {
                ExecutionResult(
                    command,
                    runtime.exec(
                        arrayOf(
                            "/bin/bash",
                            "-c",
                            command
                        )
                    ).outputString()
                )
            }
        }
    } catch (e: IOException) {
        ExecutionResult(command.normalize(), "")
    }
}

fun tempPath(): String {
    StringCare.tempFolder = Files.createTempDir().absolutePath
    return StringCare.tempFolder

}

