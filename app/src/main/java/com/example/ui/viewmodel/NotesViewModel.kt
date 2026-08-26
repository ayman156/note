package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.NoteApplication
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.repository.BackupRepository
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * إحصائيات شاملة وخفيفة لحساب ملخص الملاحظات والكلمات والمجلدات.
 */
data class NoteStats(
    val totalNotes: Int = 0,
    val totalFolders: Int = 0,
    val totalWords: Int = 0,
    val totalCharacters: Int = 0,
    val totalPinned: Int = 0,
    val totalImages: Int = 0
)

/**
 * حالة واجهة المستخدم لشاشة الملاحظات الرئيسية.
 */
data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val allTags: List<String> = emptyList(),
    val selectedFolderId: Long? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val isMultiSelectMode: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet(),
    val stats: NoteStats = NoteStats(),
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

private data class FilterConfig(
    val selectedFolderId: Long? = null,
    val selectedTag: String? = null,
    val isGridView: Boolean = true,
    val selectedNoteIds: Set<Long> = emptySet(),
    val userMessage: String? = null
)

/**
 * ViewModel الرئيسي لإدارة قائمة الملاحظات، البحث، التحديد المتعدد، المجلدات والوسوم.
 */
class NotesViewModel(
    private val noteRepository: NoteRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Long>> = _selectedNoteIds.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val folders: StateFlow<List<FolderEntity>> = noteRepository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawNotes: Flow<List<NoteEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteRepository.getAllNotes()
        } else {
            noteRepository.searchNotes(query.trim())
        }
    }

    private val filterConfig: Flow<FilterConfig> = combine(
        _selectedFolderId,
        _selectedTag,
        _isGridView,
        _selectedNoteIds,
        _userMessage
    ) { folderId, tag, isGrid, selectedIds, message ->
        FilterConfig(
            selectedFolderId = folderId,
            selectedTag = tag,
            isGridView = isGrid,
            selectedNoteIds = selectedIds,
            userMessage = message
        )
    }

    val uiState: StateFlow<NotesUiState> = combine(
        rawNotes,
        folders,
        _searchQuery,
        filterConfig
    ) { notes: List<NoteEntity>, folderList: List<FolderEntity>, query: String, config: FilterConfig ->
        // تصفية حسب المجلد والوسم
        val filteredNotes = notes.filter { note ->
            val matchFolder = when {
                config.selectedFolderId == null -> true // الكل
                config.selectedFolderId == -1L -> note.folderId == null // غير مصنفة
                else -> note.folderId == config.selectedFolderId
            }
            val matchTag = when {
                config.selectedTag == null -> true
                else -> note.tags.contains(config.selectedTag)
            }
            matchFolder && matchTag
        }

        // استخراج جميع الوسوم الفريدة
        val uniqueTags = notes.flatMap { it.tags }.distinct().sorted()

        // حساب الإحصائيات
        val totalWords = notes.sumOf { note ->
            val titleWords = note.title.split("\\s+".toRegex()).count { it.isNotBlank() }
            val contentWords = note.content.split("\\s+".toRegex()).count { it.isNotBlank() }
            titleWords + contentWords
        }
        val totalChars = notes.sumOf { it.title.length + it.content.length }
        val totalImages = notes.sumOf { it.imageUris.size }
        val totalPinned = notes.count { it.isPinned }

        val stats = NoteStats(
            totalNotes = notes.size,
            totalFolders = folderList.size,
            totalWords = totalWords,
            totalCharacters = totalChars,
            totalPinned = totalPinned,
            totalImages = totalImages
        )

        NotesUiState(
            notes = filteredNotes,
            folders = folderList,
            allTags = uniqueTags,
            selectedFolderId = config.selectedFolderId,
            selectedTag = config.selectedTag,
            searchQuery = query,
            isGridView = config.isGridView,
            isMultiSelectMode = config.selectedNoteIds.isNotEmpty(),
            selectedNoteIds = config.selectedNoteIds,
            stats = stats,
            isLoading = false,
            userMessage = config.userMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleNoteSelection(noteId: Long) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
        } else {
            current.add(noteId)
        }
        _selectedNoteIds.value = current
    }

    fun selectAllNotes() {
        _selectedNoteIds.value = uiState.value.notes.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
    }

    fun deleteSelectedNotes() {
        val ids = _selectedNoteIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            noteRepository.deleteNotesByIds(ids)
            clearSelection()
            _userMessage.value = "تم حذف ${ids.size} ملاحظة"
        }
    }

    fun moveSelectedNotesToFolder(folderId: Long?, folderName: String?) {
        val ids = _selectedNoteIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            noteRepository.moveNotesToFolder(ids, folderId, folderName)
            clearSelection()
            _userMessage.value = "تم نقل الملاحظات بنجاح"
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.togglePin(note.id, note.isPinned)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            _userMessage.value = "تم حذف الملاحظة"
        }
    }

    fun createFolder(name: String, colorHex: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            noteRepository.insertOrUpdateFolder(
                FolderEntity(name = name.trim(), colorHex = colorHex)
            )
            _userMessage.value = "تم إنشاء المجلد بنجاح"
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            noteRepository.insertOrUpdateFolder(folder)
            _userMessage.value = "تم تحديث المجلد"
        }
    }

    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            noteRepository.deleteFolderById(folder.id)
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            _userMessage.value = "تم حذف المجلد"
        }
    }

    fun exportBackup(onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = backupRepository.exportBackupToFile()
                onFileReady(file)
                _userMessage.value = "تم تجهيز ملف النسخ الاحتياطي"
            } catch (e: Exception) {
                _userMessage.value = "حدث خطأ أثناء النسخ الاحتياطي: ${e.message}"
            }
        }
    }

    fun restoreBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            val result = backupRepository.restoreFromUri(uri)
            if (result.isSuccess) {
                _userMessage.value = "تمت استعادة ${result.getOrNull() ?: 0} ملاحظة بنجاح"
            } else {
                _userMessage.value = "فشلت الاستعادة: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApplication)
                NotesViewModel(
                    noteRepository = app.container.noteRepository,
                    backupRepository = app.container.backupRepository
                )
            }
        }
    }
}
