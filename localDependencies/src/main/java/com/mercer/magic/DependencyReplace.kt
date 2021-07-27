package com.mercer.magic

import com.mercer.magic.extension.DynamicLocalDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseProjectDependency
import java.io.File

class DependencyReplace : Plugin<Project> {

    override fun apply(target: Project) {

        target.afterEvaluate { project ->
            project.afterEvaluateHandle()
        }

    }

    private fun Project.afterEvaluateHandle() {
        val extension =
            (rootProject.extensions.findByName(DynamicLocalDependency.EXTENSION_KEY) as? DynamicLocalDependency
                ?: throw NullPointerException("can not receive extensions findByName(${DynamicLocalDependency.EXTENSION_KEY})."))

        // 导入本地依赖发布的文件夹地址作为仓库
        repositories.maven { mavenArtifactRepository ->
            mavenArtifactRepository.url = uri(extension.uri())
        }

        // 关系
        val connections = project.configurations.asMap
            .map { entry: Map.Entry<String, Configuration> ->
                entry.key to entry.value.dependencies.toList()
            }
            .filter {
                it.second.isNotEmpty()
            }
            .flatMap { (configuration, dependencies) ->
                dependencies.map { dependency ->
                    configuration to dependency
                }
            }
            .filter { (_, dependency) ->
                dependency is ProjectDependency
            }
            .filter { (configuration, _) ->
                // 现阶段只处理 implementation 方式
                configuration == "implementation"
            }
            .groupBy({ (_, dependency) ->
                dependency.name
            }) { (configuration, _) ->
                configuration
            }

        val configurationContainer = configurations
        connections.flatMap { (dependency, configurations) ->
            configurations.map { configuration ->
                configuration to dependency
            }
        }.onEach { (configuration, dependency) ->
            val dependencySet = configurationContainer.getByName(configuration).dependencies

            val exists = File(extension.uri(), "$dependency.json").exists()
            val isApp = project.pluginManager.hasPlugin("com.android.application")
            if (exists && isApp) {
                dependencySet.removeIf { it.name == dependency }
                configurationContainer.getByName(configuration)
                    .dependencies
                    .add(
                        DefaultExternalModuleDependency(
                            extension.group(), dependency, extension.version(),
                        )
                    )
            } else if (exists) {
                dependencySet.removeIf { it.name == dependency }
                configurationContainer.getByName("compileOnly")
                    .dependencies
                    .add(
                        DefaultExternalModuleDependency(
                            extension.group(), dependency, extension.version(),
                        )
                    )
            } else if (!exists && !isApp) {
                val find = dependencySet.find { it.name == dependency }
                dependencySet.remove(find)
                configurationContainer.getByName("compileOnly")
                    .dependencies
                    .add(find)
            }
        }
    }

}