package com.mercer.magic

import com.mercer.magic.extension.DynamicLocalDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload

/**
 * 使用插件
 */
inline fun <reified P : Plugin<Project>> Project.applyPlugin() {
    apply { action ->
        action.plugin(P::class.java)
    }
}

/**
 * 添加上传任务
 */
fun Project.addUploadArchivesTask(extension: DynamicLocalDependency) {
    pluginManager.apply("maven")
    val groupId = extension.group()
    val artifactId = name
    val version = extension.version()
    val url = uri(extension.uri())
    val name = "uploadArchives"
    (tasks.findByName(name) as? Upload
        ?: throw NullPointerException("can not get task by findByName(${name})")).run {
        withGroovyBuilder {
            "repositories"{
                "mavenDeployer"{
                    "repository"("url" to url)
                    "pom"{
                        "project"{
                            "groupId"(groupId)
                            "artifactId"(artifactId)
                            "version"(version)
                        }
                    }
                }
            }
        }
    }
}