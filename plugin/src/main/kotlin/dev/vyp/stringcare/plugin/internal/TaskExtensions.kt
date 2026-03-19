package dev.vyp.stringcare.plugin.internal

import org.gradle.api.Task

fun Task.getModuleName(): String? {
    val path = this.project.path
    return if (path.isEmpty()) null else path.split(":")[path.split(":").size - 1]
}

fun Task.dataFound(): Boolean =
    this.name.contains(pre) &&
        this.name.contains(build) &&
        this.name != "$pre$build" &&
        !this.name.contains(test)

fun Task.onMergeResourcesStarts(): Boolean =
    this.name.contains(merge) &&
        this.name.contains(resources) &&
        !this.name.contains(test)

fun Task.onMergeResourcesFinish(): Boolean =
    this.name.contains(merge) &&
        this.name.contains(resources) &&
        !this.name.contains(test)

fun Task.onMergeAssetsStarts(): Boolean =
    this.name.contains(generate) &&
        this.name.contains(assets) &&
        !this.name.contains(test)

fun Task.onMergeAssetsFinish(): Boolean =
    this.name.contains(merge) &&
        this.name.contains(assets) &&
        !this.name.contains(test)

fun Task.dataFoundVariant(): String =
    this.name
        .substring(pre.length)
        .substring(0, this.name.substring(pre.length).length - build.length)

fun Task.onMergeResourcesStartsVariant(): String =
    this.name
        .substring(merge.length)
        .substring(0, this.name.substring(merge.length).length - resources.length)

fun Task.onMergeResourcesFinishVariant(): String =
    this.name
        .substring(merge.length)
        .substring(0, this.name.substring(merge.length).length - resources.length)

fun Task.onMergeAssetsStartsVariant(): String =
    this.name
        .substring(generate.length)
        .substring(0, this.name.substring(generate.length).length - assets.length)

fun Task.onMergeAssetsFinishVariant(): String =
    this.name
        .substring(merge.length)
        .substring(0, this.name.substring(merge.length).length - assets.length)
