package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * واجهة DAO للتعامل مع جدول الملاحظات مع استعلامات تفاعلية باستخدام Flow ودعم البحث والترتيب والتصفية.
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId IS NULL ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesWithoutFolder(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
           OR folderName LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id IN (:noteIds)")
    suspend fun deleteNotesByIds(noteIds: List<Long>)

    @Query("UPDATE notes SET folderId = :folderId, folderName = :folderName WHERE id IN (:noteIds)")
    suspend fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?, folderName: String?)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM notes")
    fun getNotesCount(): Flow<Int>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSnapshot(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
