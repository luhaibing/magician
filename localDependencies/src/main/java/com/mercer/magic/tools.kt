package com.mercer.magic

import java.io.File

fun <I, O> asCollection(
    collection: Collection<I>, values: MutableCollection<O>, action: (I) -> O
): MutableCollection<O> {
    collection.onEach {
        values.add(action(it))
    }
    return values
}

fun transformFiles(parent: String, src: List<File>): List<String> {
    return asCollection(src, arrayListOf()) {
        // 裁掉文件绝对路径的父路径
        it.absolutePath.substring(parent.length)
    }.toList()
}

// 展开
tailrec fun Collection<File>.unfolds(): List<File> {
    val toList = if (this is List) {
        this
    } else {
        toList()
    }
    val values = arrayListOf<File>()
    for (file in toList) {
        if (file.isFile) {
            values.add(file)
        } else {
            val unfolds = file.listFiles()?.toList()?.unfolds() ?: emptyList()
            values.addAll(unfolds)
        }
    }
    return values
}