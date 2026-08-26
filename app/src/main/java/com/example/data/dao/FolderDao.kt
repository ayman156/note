package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FolderEntity
import kotlinx.coroutines.flow.Flow

/**
 * واجهة DAO لإدارة المجلدات (إنشاء، تعديل، حذف، جلب).
 */
@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)

    @Query("SELECT COUNT(*) FROM folders")
    fun getFoldersCount(): Flow<Int>

    @Query("SELECT * FROM folders")
    suspend fun getAllFoldersSnapshot(): List<FolderEntity>

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}
