package task

import StringCare
import StringCare.Companion.absoluteProjectPath
import com.google.gson.Gson
import components.applicationVariants
import components.getContent
import components.locateResourceFiles
import components.parseXML
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SCPreview : DefaultTask() {

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
        val gson = Gson()
        println("== REPORT ======================================")
        val task = this;
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
            val files = locateResourceFiles(absoluteProjectPath, configuration)
            println("\tLocated files(${files.size}) for obfuscating")
            println("\tConfig (${gson.toJson(configuration)})")
            files.forEach { file ->
                println("\t- ${file.file.name}")
                println("\t\t${file.module}${File.separator}${file.sourceFolder}${File.separator}")
                val entities = parseXML(file.file)
                println("\tpath: ${file.file.absolutePath}")
                println("")
                println("\t============================")
                entities.forEach { entity ->
                    entity.attributes.forEach { attribute ->
                        println("\t\"${attribute.name}\": \"${attribute.value}\"")
                    }
                    println("\t\"value\": \"${entity.value}\"")
                    println("\t============================")
                }
                println("")
                println("\t=== content ================")
                println("" + file.file.getContent())
                println("\t============================")

            }
        }
        println("== END REPORT ==================================")
    }
}
