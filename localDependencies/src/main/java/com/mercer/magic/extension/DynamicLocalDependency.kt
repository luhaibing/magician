package com.mercer.magic.extension

import java.io.File

interface DynamicLocalDependency {

    companion object {
        const val EXTENSION_KEY: String = "dependency"
    }

    fun uri(): File
    fun group(): String
    fun version(): String


    /**
     * 依赖输出路径
     */
    fun uri(value: File)
    fun uri(value: String)

    fun group(value: String)
    fun version(value: String)

}