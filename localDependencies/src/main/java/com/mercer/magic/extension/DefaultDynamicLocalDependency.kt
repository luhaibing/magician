package com.mercer.magic.extension

import org.gradle.api.Project
import java.io.File

open class DefaultDynamicLocalDependency(
    private val target: Project,
) : DynamicLocalDependency {

    private var uri: File? = null

    private var group: String? = null

    private var version: String? = null

    override fun uri(): File = uri
        ?: throw NullPointerException("${DynamicLocalDependency.EXTENSION_KEY}.uri can not be null.")

    override fun group(): String = group
        ?: throw NullPointerException("${DynamicLocalDependency.EXTENSION_KEY}.group can not be null.")


    override fun version(): String = version
        ?: throw NullPointerException("${DynamicLocalDependency.EXTENSION_KEY}.version can not be null.")

    override fun uri(value: File) {
        uri = value
    }

    override fun uri(value: String) {
        uri(target.rootProject.file(value))
    }

    override fun group(value: String) {
        group = value
    }

    override fun version(value: String) {
        version = value
    }

}