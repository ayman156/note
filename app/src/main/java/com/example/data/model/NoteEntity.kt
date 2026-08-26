package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * يمثل هذا الكيان (Entity) جدول الملاحظات في قاعدة بيانات Room.
 * تم تصميمه ليتسع لنصوص غير محدودة الحجم مع دعم كامل للمرفقات والوسوم والألوان.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val folderName: String? = null,
    val tags: List<String> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val isRtl: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
