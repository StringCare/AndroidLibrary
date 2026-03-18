package components

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class ExecutionListener(
    private val debug: Boolean = false,
    val dataFound: (module: String, variant: String) -> Unit,
    val mergeResourcesStart: (module: String, variant: String) -> Unit,
    val mergeResourcesFinish: (module: String, variant: String) -> Unit,
    val mergeAssetsStart: (module: String, variant: String) -> Unit,
    val mergeAssetsFinish: (module: String, variant: String) -> Unit
) : BuildListener,
    TaskExecutionListener {

    override fun beforeExecute(task: Task) {
        when {
            task.dataFound() -> task.getModuleName()?.let {
                dataFound(it, PrintUtils.uncapitalize(task.dataFoundVariant()))
            }
            task.onMergeResourcesStarts() -> task.getModuleName()?.let {
                if (debug) {
                    PrintUtils.print(it, "Module: $it", true)
                }
                mergeResourcesStart(it, PrintUtils.uncapitalize(task.onMergeResourcesStartsVariant()))
            }
            task.onMergeAssetsStarts() -> task.getModuleName()?.let {
                if (debug) {
                    PrintUtils.print(it, "Module: $it", true)
                }
                mergeAssetsStart(it, PrintUtils.uncapitalize(task.onMergeAssetsStartsVariant()))
            }
        }
    }

    override fun afterExecute(task: Task, state: TaskState) {
        when {
            task.onMergeResourcesFinish() -> task.getModuleName()?.let {
                mergeResourcesFinish(it, PrintUtils.uncapitalize(task.onMergeResourcesFinishVariant()))
            }
            task.onMergeAssetsFinish() -> task.getModuleName()?.let {
                mergeAssetsFinish(it, PrintUtils.uncapitalize(task.onMergeAssetsFinishVariant()))
            }
        }
    }

    override fun settingsEvaluated(settings: Settings) {
        // nothing to do here
    }

    override fun buildFinished(result: BuildResult) {
        // nothing to do here
    }

    override fun projectsLoaded(gradle: Gradle) {
        // nothing to do here
    }

    override fun buildStarted(gradle: Gradle) {
        // nothing to do here
    }

    override fun projectsEvaluated(gradle: Gradle) {
        // nothing to do here
    }
}