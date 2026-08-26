package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.BackupData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مستودع إدارة النسخ الاحتياطي والاستعادة المحلية بتنسيق JSON.
 */
class BackupRepository(
    private val context: Context,
    private val noteRepository: NoteRepository
) {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(BackupData::class.java).indent("  ")

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val notes = noteRepository.getAllNotesSnapshot()
        val folders = noteRepository.getAllFoldersSnapshot()
        val backupData = BackupData(
            notes = notes,
            folders = folders
        )
        adapter.toJson(backupData)
    }

    suspend fun exportBackupToFile(): File = withContext(Dispatchers.IO) {
        val json = createBackupJson()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
        val backupFile = File(backupDir, "NoteA_Backup_$timeStamp.json")
        FileOutputStream(backupFile).use { output ->
            output.write(json.toByteArray(Charsets.UTF_8))
        }
        backupFile
    }

    suspend fun restoreFromJsonString(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backupData = adapter.fromJson(jsonString)
                ?: return@withContext Result.failure(Exception("ملف النسخ الاحتياطي غير صالح"))

            noteRepository.restoreData(backupData.notes, backupData.folders)
            Result.success(backupData.notes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("تعذر قراءة الملف"))
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            restoreFromJsonString(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
