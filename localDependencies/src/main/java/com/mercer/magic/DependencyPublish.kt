package com.mercer.magic

import com.android.build.gradle.LibraryExtension
import com.google.gson.Gson
import com.mercer.magic.extension.DynamicLocalDependency
import com.mercer.magic.model.DependencyRecord
import com.mercer.magic.model.FileRecord
import com.mercer.magic.model.SourceSetRecord
import com.mercer.magic.model.convert
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.DefaultSourceSetContainer
import java.io.File

class DependencyPublish : Plugin<Project> {

    companion object {

        val pluginIds = arrayListOf(
            "java",
            "com.android.library"
        )
    }

    override fun apply(target: Project) {
        target.afterEvaluate {
            it.afterEvaluateHandle()
        }
    }

    private fun Project.afterEvaluateHandle() {

        // 只处理使用这两种插件的模块 [Java 和 Android 的 library]
        if (!pluginIds.any { pluginManager.hasPlugin(it) }) {
            return
        }

        val extension =
            (rootProject.extensions.findByName(DynamicLocalDependency.EXTENSION_KEY) as? DynamicLocalDependency
                ?: throw NullPointerException("can not receive extensions findByName(${DynamicLocalDependency.EXTENSION_KEY})."))

        addUploadArchivesTask(extension)
        tasks.findByName("uploadArchives")!!.doLast {
            if (pluginManager.hasPlugin(pluginIds[0])) {
                javaProcess(extension)
            } else {
                androidProcess(extension)
            }
        }

    }

    private fun Project.androidProcess(extension: DynamicLocalDependency) {
        val container = "android"
        val channel = "main"
        val sourceSetContainer = (extensions.findByName(container)
                as LibraryExtension).sourceSets.findByName(channel)
            ?: throw NullPointerException("can not found ${container}.${channel}")

        val path: String = project.projectDir.absolutePath

        val code = sourceSetContainer.java.srcDirs.unfolds()
        val resource = sourceSetContainer.res.srcDirs.unfolds().map { it.convert(path) }
        val assets = sourceSetContainer.assets.srcDirs.unfolds().map { it.convert(path) }
        val jni = sourceSetContainer.jni.srcDirs.unfolds().map { it.convert(path) }
        val aidl = sourceSetContainer.aidl.srcDirs.unfolds().map { it.convert(path) }

        val java = code.filter { it.path.endsWith(".java") }.map { it.convert(path) }
        val kotlin = code.filter { it.path.endsWith(".kt") }.map { it.convert(path) }

        val buildFileRecord = buildFile.convert(path)
        val manifestFileRecord = sourceSetContainer.manifest.srcFile.convert(path)

        val dependency = extension.group() + ":" + name + ":" + extension.version()

        val sourceSet = SourceSetRecord(
            java, kotlin, resource,
            assets, jni, aidl,
            buildFileRecord, manifestFileRecord
        )
        val json = Gson().toJson(
            DependencyRecord(
                dependency, sourceSet
            )
        )
        val jsonFile = File(extension.uri(), project.name + ".json")
        jsonFile.writeBytes(json.toByteArray())

    }

    private fun Project.javaProcess(extension: DynamicLocalDependency) {
        val container = "sourceSets"
        val channel = "main"
        val sourceSetContainer = (extensions.findByName(container)
                as DefaultSourceSetContainer).findByName(channel)
            ?: throw NullPointerException("can not found ${container}.${channel}")

        val path: String = project.projectDir.absolutePath

        val code = sourceSetContainer.java.srcDirs.unfolds()
        val resource = sourceSetContainer.resources.srcDirs.unfolds().map { it.convert(path) }
        val assets = arrayListOf<File>().map { it.convert(path) }
        val jni = arrayListOf<File>().map { it.convert(path) }
        val aidl = arrayListOf<File>().map { it.convert(path) }

        val java = code.filter { it.path.endsWith(".java") }.map { it.convert(path) }
        val kotlin = code.filter { it.path.endsWith(".kt") }.map { it.convert(path) }

        val buildFileRecord = buildFile.convert(path)
        val manifestFileRecord: FileRecord? = null

        val dependency = extension.group() + ":" + name + ":" + extension.version()

        val sourceSet = SourceSetRecord(
            java, kotlin, resource,
            assets, jni, aidl,
            buildFileRecord, manifestFileRecord
        )
        val json = Gson().toJson(
            DependencyRecord(
                dependency, sourceSet
            )
        )
        val jsonFile = File(extension.uri(), project.name + ".json")
        jsonFile.writeBytes(json.toByteArray())


    }

}

