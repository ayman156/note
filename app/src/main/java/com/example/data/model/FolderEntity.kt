package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * يمثل هذا الكيان (Entity) جدول المجلدات لتنظيم الملاحظات اختيارياً.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String? = null,
    val iconName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
