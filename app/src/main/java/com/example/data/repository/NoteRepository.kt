package com.example.data.repository

import com.example.data.dao.FolderDao
import com.example.data.dao.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * مستودع البيانات (Repository Pattern) لفصل منطق الوصول لقاعدة البيانات عن طبقة العرض (UI / ViewModel).
 */
interface NoteRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>>
    fun getNotesWithoutFolder(): Flow<List<NoteEntity>>
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    suspend fun getNoteById(id: Long): NoteEntity?
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>
    suspend fun insertOrUpdateNote(note: NoteEntity): Long
    suspend fun deleteNote(note: NoteEntity)
    suspend fun deleteNotesByIds(noteIds: List<Long>)
    suspend fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?, folderName: String?)
    suspend fun togglePin(noteId: Long, currentPinState: Boolean)

    // Folders
    fun getAllFolders(): Flow<List<FolderEntity>>
    suspend fun insertOrUpdateFolder(folder: FolderEntity): Long
    suspend fun deleteFolder(folder: FolderEntity)
    suspend fun deleteFolderById(folderId: Long)

    // Snapshots for backup & statistics
    suspend fun getAllNotesSnapshot(): List<NoteEntity>
    suspend fun getAllFoldersSnapshot(): List<FolderEntity>
    suspend fun restoreData(notes: List<NoteEntity>, folders: List<FolderEntity>)
}

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesByFolder(folderId)

    override fun getNotesWithoutFolder(): Flow<List<NoteEntity>> =
        noteDao.getNotesWithoutFolder()

    override fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    override suspend fun getNoteById(id: Long): NoteEntity? =
        noteDao.getNoteById(id)

    override fun getNoteByIdFlow(id: Long): Flow<NoteEntity?> =
        noteDao.getNoteByIdFlow(id)

    override suspend fun insertOrUpdateNote(note: NoteEntity): Long {
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        return noteDao.insertNote(updatedNote)
    }

    override suspend fun deleteNote(note: NoteEntity) =
        noteDao.deleteNote(note)

    override suspend fun deleteNotesByIds(noteIds: List<Long>) =
        noteDao.deleteNotesByIds(noteIds)

    override suspend fun moveNotesToFolder(
        noteIds: List<Long>,
        folderId: Long?,
        folderName: String?
    ) = noteDao.moveNotesToFolder(noteIds, folderId, folderName)

    override suspend fun togglePin(noteId: Long, currentPinState: Boolean) =
        noteDao.updatePinStatus(noteId, !currentPinState)

    override fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override suspend fun insertOrUpdateFolder(folder: FolderEntity): Long =
        folderDao.insertFolder(folder)

    override suspend fun deleteFolder(folder: FolderEntity) =
        folderDao.deleteFolder(folder)

    override suspend fun deleteFolderById(folderId: Long) {
        // فك ارتباط الملاحظات التابعة للمجلد أولاً
        noteDao.moveNotesToFolder(emptyList(), null, null)
        folderDao.deleteFolderById(folderId)
    }

    override suspend fun getAllNotesSnapshot(): List<NoteEntity> =
        noteDao.getAllNotesSnapshot()

    override suspend fun getAllFoldersSnapshot(): List<FolderEntity> =
        folderDao.getAllFoldersSnapshot()

    override suspend fun restoreData(notes: List<NoteEntity>, folders: List<FolderEntity>) {
        if (folders.isNotEmpty()) {
            folderDao.insertAll(folders)
        }
        if (notes.isNotEmpty()) {
            noteDao.insertAll(notes)
        }
    }
}
