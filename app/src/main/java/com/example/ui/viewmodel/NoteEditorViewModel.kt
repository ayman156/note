package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.NoteApplication
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * حالة واجهة المستخدم لشاشة تحرير الملاحظة.
 */
data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(""),
    val selectedFolderId: Long? = null,
    val selectedFolderName: String? = null,
    val tags: List<String> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val colorHex: String? = null,
    val isPinned: Boolean = false,
    val isRtl: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * ViewModel محرر الملاحظات مع دعم التنسيق النصي المرن، الوسائط المتعددة، المجلدات والوسوم.
 */
class NoteEditorViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    val availableFolders: StateFlow<List<FolderEntity>> = noteRepository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var initialLoaded = false

    fun loadNote(noteId: Long?) {
        if (noteId == null || noteId <= 0L || initialLoaded) return
        initialLoaded = true
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            if (note != null) {
                _uiState.value = NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    content = TextFieldValue(
                        text = note.content,
                        selection = TextRange(note.content.length)
                    ),
                    selectedFolderId = note.folderId,
                    selectedFolderName = note.folderName,
                    tags = note.tags,
                    imageUris = note.imageUris,
                    colorHex = note.colorHex,
                    isPinned = note.isPinned,
                    isRtl = note.isRtl,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt
                )
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onContentChanged(newContent: TextFieldValue) {
        _uiState.value = _uiState.value.copy(content = newContent)
    }

    fun setFolder(folderId: Long?, folderName: String?) {
        _uiState.value = _uiState.value.copy(
            selectedFolderId = folderId,
            selectedFolderName = folderName
        )
    }

    fun addTag(tag: String) {
        val cleanTag = tag.trim().removePrefix("#")
        if (cleanTag.isNotBlank() && !_uiState.value.tags.contains(cleanTag)) {
            _uiState.value = _uiState.value.copy(
                tags = _uiState.value.tags + cleanTag
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            tags = _uiState.value.tags.filter { it != tag }
        )
    }

    fun setColor(colorHex: String?) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun togglePin() {
        _uiState.value = _uiState.value.copy(isPinned = !_uiState.value.isPinned)
    }

    fun toggleRtl() {
        _uiState.value = _uiState.value.copy(isRtl = !_uiState.value.isRtl)
    }

    fun addImage(uriString: String) {
        if (uriString.isNotBlank() && !_uiState.value.imageUris.contains(uriString)) {
            _uiState.value = _uiState.value.copy(
                imageUris = _uiState.value.imageUris + uriString
            )
        }
    }

    fun removeImage(uriString: String) {
        _uiState.value = _uiState.value.copy(
            imageUris = _uiState.value.imageUris.filter { it != uriString }
        )
    }

    fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
            val destFile = File(imagesDir, "IMG_$timeStamp.jpg")
            FileOutputStream(destFile).use { output ->
                inputStream.copyTo(output)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            sourceUri.toString()
        }
    }

    // تنسيقات النصوص الغنية (Bold, Italic, Headings, Bullets, Checklists)
    fun applyBold() {
        wrapSelection("**", "**", "نص غامق")
    }

    fun applyItalic() {
        wrapSelection("*", "*", "نص مائل")
    }

    fun applyHeading(level: Int = 1) {
        val prefix = "#".repeat(level) + " "
        insertLinePrefix(prefix)
    }

    fun applyBulletList() {
        insertLinePrefix("• ")
    }

    fun applyNumberedList() {
        insertLinePrefix("1. ")
    }

    fun applyChecklist() {
        insertLinePrefix("☑ ")
    }

    fun applyQuote() {
        insertLinePrefix("> ")
    }

    private fun wrapSelection(startWrapper: String, endWrapper: String, defaultText: String) {
        val current = _uiState.value.content
        val text = current.text
        val selection = current.selection

        val (newText, newSelection) = if (selection.start != selection.end) {
            val selectedText = text.substring(selection.start, selection.end)
            val replaced = startWrapper + selectedText + endWrapper
            val fullText = text.replaceRange(selection.start, selection.end, replaced)
            fullText to TextRange(selection.start + replaced.length)
        } else {
            val inserted = startWrapper + defaultText + endWrapper
            val fullText = text.substring(0, selection.start) + inserted + text.substring(selection.start)
            fullText to TextRange(selection.start + startWrapper.length, selection.start + startWrapper.length + defaultText.length)
        }

        _uiState.value = _uiState.value.copy(content = TextFieldValue(newText, newSelection))
    }

    private fun insertLinePrefix(prefix: String) {
        val current = _uiState.value.content
        val text = current.text
        val cursor = current.selection.start

        val lastNewline = text.lastIndexOf('\n', cursor - 1)
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1

        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val newCursor = cursor + prefix.length
        _uiState.value = _uiState.value.copy(
            content = TextFieldValue(newText, TextRange(newCursor))
        )
    }

    fun saveNote(onComplete: (() -> Unit)? = null) {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.text.isBlank() && state.imageUris.isEmpty()) {
            onComplete?.invoke()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val noteEntity = NoteEntity(
                id = state.noteId ?: 0L,
                title = state.title.trim(),
                content = state.content.text,
                folderId = state.selectedFolderId,
                folderName = state.selectedFolderName,
                tags = state.tags,
                imageUris = state.imageUris,
                colorHex = state.colorHex,
                isPinned = state.isPinned,
                isRtl = state.isRtl,
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            val id = noteRepository.insertOrUpdateNote(noteEntity)
            _uiState.value = _uiState.value.copy(
                noteId = id,
                isSaving = false,
                isSaved = true
            )
            onComplete?.invoke()
        }
    }

    fun deleteCurrentNote(onDeleted: () -> Unit) {
        val id = _uiState.value.noteId ?: return onDeleted()
        viewModelScope.launch {
            noteRepository.deleteNotesByIds(listOf(id))
            _uiState.value = _uiState.value.copy(isDeleted = true)
            onDeleted()
        }
    }

    fun shareNote(context: Context) {
        val state = _uiState.value
        val shareText = buildString {
            if (state.title.isNotBlank()) {
                append(state.title)
                append("\n\n")
            }
            append(state.content.text)
            if (state.tags.isNotEmpty()) {
                append("\n\n")
                append(state.tags.joinToString(" ") { "#$it" })
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الملاحظة عبر")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApplication)
                NoteEditorViewModel(
                    noteRepository = app.container.noteRepository
                )
            }
        }
    }
}
