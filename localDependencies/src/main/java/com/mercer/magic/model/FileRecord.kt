package com.mercer.magic.model

import com.mercer.magic.string2Md5
import java.io.File

class FileRecord(
    val source: String,
    val md5: String,
    val lastModified: Long,
)

fun File.convert(p: String) = FileRecord(
    path.substring(p.length + 0),
    string2Md5(readText()),
    lastModified()
)