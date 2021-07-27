package com.mercer.magic

import com.mercer.magic.extension.DefaultDynamicLocalDependency
import com.mercer.magic.extension.DynamicLocalDependency
import org.gradle.api.Plugin
import org.gradle.api.Project

class DynamicLocalDependencyPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        target.extensions.create(
            DynamicLocalDependency::class.java, DynamicLocalDependency.EXTENSION_KEY,
            DefaultDynamicLocalDependency::class.java, target
        )

        // 排除 根项目
        if (target != target.rootProject) {
            target.applyPlugin<DependencyPublish>()
            target.applyPlugin<DependencyReplace>()
            return
        }
        if (target.subprojects.isEmpty()) {
            return
        }

        target.subprojects.onEach { project ->
            project.applyPlugin<DependencyPublish>()
            project.applyPlugin<DependencyReplace>()
        }

    }

}