package com.mercer.magic.model

class SourceSetRecord(
    val java: List<FileRecord>,
    val kotlin: List<FileRecord>,
    val resource: List<FileRecord>,
    val assets: List<FileRecord>,
    val jni: List<FileRecord>,
    val aidl: List<FileRecord>,
    val build: FileRecord,
    val manifest: FileRecord?,
)