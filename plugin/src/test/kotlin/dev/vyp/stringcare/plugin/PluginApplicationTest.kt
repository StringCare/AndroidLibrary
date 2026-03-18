package dev.vyp.stringcare.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PluginApplicationTest {

    @Test
    fun `plugin applies and creates stringcare extension`() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.vyp.stringcare.plugin")
        val extension = project.extensions.findByName("stringcare")
        assertNotNull(extension)
    }
}
