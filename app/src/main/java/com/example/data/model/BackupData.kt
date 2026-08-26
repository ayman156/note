package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * بنية بيانات موحدة للنسخ الاحتياطي وتصدير/استيراد كافة الملاحظات والمجلدات.
 */
@JsonClass(generateAdapter = true)
data class BackupData(
    val appVersion: String = "1.0",
    val exportTimestamp: Long = System.currentTimeMillis(),
    val notes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList()
)
