package task

import StringCare
import StringCare.Companion.absoluteProjectPath
import com.google.gson.Gson
import components.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SCTestObfuscation : DefaultTask() {

    @Input
    var module = String()

    @Input
    var assetsFiles = String()

    @Input
    var stringFiles = String()

    @Input
    var srcFolders = String()

    @Input
    var debug = false

    @Input
    var skip = false

    @Input
    var applicationId = ""

    @Input
    var mockedFingerprint = ""

    @TaskAction
    fun greet() {
        println("== TEST OBFUSCATION ======================================")
        var key = ""
        val gson = Gson()

        val task = this
        project.applicationVariants()?.forEach { variant ->
            println("\t== ${variant.name} ======================================")
            val configuration = StringCare.Configuration(module).apply {
                val lSrcFolders = gson.fromJson(task.srcFolders, MutableList::class.java)
                if (task.srcFolders.isNotEmpty()) {
                    srcFolders.addAll(lSrcFolders as MutableList<String>)
                }
                val lStringFiles = gson.fromJson(task.stringFiles, MutableList::class.java)
                if (task.stringFiles.isNotEmpty()) {
                    stringFiles.addAll(lStringFiles as MutableList<String>)
                }
                val lAssetsFiles = gson.fromJson(task.assetsFiles, MutableList::class.java)
                if (task.assetsFiles.isNotEmpty()) {
                    assetsFiles.addAll(lAssetsFiles as MutableList<String>)
                }
                if (task.mockedFingerprint.isNotEmpty()) {
                    mockedFingerprint = task.mockedFingerprint
                }
                applicationId = variant.applicationId
                skip = task.skip
                debug = task.debug
            }
            signingReportTask().runCommand { _, result ->
                key = result.extractFingerprint(module, variant.name, configuration)
            }
            val filesToObfuscate = backupResourceFiles(absoluteProjectPath, configuration)
            filesToObfuscate.forEach { file ->
                val originalEntities = parseXML(file.file)
                println("\t============================")
                println("\tpath: ${file.file.absolutePath}")
                originalEntities.forEach { entity ->
                    entity.attributes.forEach { attribute ->
                        println("\"${attribute.name}\": \"${attribute.value}\"")
                    }
                    println("\"\tvalue\": \"${entity.value}\"")
                    println("\t============================")
                }
                println("")
                println("\t=== content ================")
                println(file.file.getContent())
                println("\t============================")
                modifyXML(file.file, key, configuration)
                println("\t=== content obfuscated ================")
                println(file.file.getContent())
                println("\t============================")
            }
            restoreResourceFiles(absoluteProjectPath, module)
        }
        println("== END OBFUSCATION ==================================")


    }
}
