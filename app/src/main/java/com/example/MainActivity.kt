package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LockOverlay
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LockViewModel
import com.example.ui.viewmodel.NoteEditorViewModel
import com.example.ui.viewmodel.NotesViewModel

/**
 * الوجهات والشاشات المختلفة داخل تطبيق Note A.
 */
sealed interface Screen {
    data object Home : Screen
    data class Editor(val noteId: Long?) : Screen
    data object Folders : Screen
    data object Stats : Screen
    data object Settings : Screen
}

/**
 * النشاط الرئيسي MainActivity لتطبيق Note A.
 * يرث من FragmentActivity لدعم البصمة BiometricPrompt و Jetpack Compose معاً.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                NoteApp(activity = this)
            }
        }
    }
}

@Composable
fun NoteApp(
    activity: FragmentActivity,
    notesViewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory),
    lockViewModel: LockViewModel = viewModel(factory = LockViewModel.Factory)
) {
    val notesUiState by notesViewModel.uiState.collectAsStateWithLifecycle()
    val lockUiState by lockViewModel.uiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // تشغيل المصادقة بالبصمة تلقائياً عند فتح التطبيق إذا كان مقفلاً ومفعلاً
    LaunchedEffect(lockUiState.isUnlocked, lockUiState.isSecurityEnabled, lockUiState.isBiometricEnabled) {
        if (!lockUiState.isUnlocked && lockUiState.isSecurityEnabled && lockUiState.isBiometricEnabled) {
            lockViewModel.triggerBiometricPrompt(activity)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // إدارة التنقل بين الشاشات مع انتقالات حركية سلسة
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState is Screen.Editor || targetState is Screen.Folders || targetState is Screen.Stats || targetState is Screen.Settings) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut()
                    )
                }
            },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(
                        uiState = notesUiState,
                        onSearchQueryChanged = notesViewModel::onSearchQueryChanged,
                        onSelectFolder = notesViewModel::selectFolder,
                        onSelectTag = notesViewModel::selectTag,
                        onToggleViewMode = notesViewModel::toggleViewMode,
                        onNoteClick = { noteId ->
                            currentScreen = Screen.Editor(noteId)
                        },
                        onNewNoteClick = {
                            currentScreen = Screen.Editor(null)
                        },
                        onToggleNoteSelection = notesViewModel::toggleNoteSelection,
                        onSelectAllNotes = notesViewModel::selectAllNotes,
                        onClearSelection = notesViewModel::clearSelection,
                        onDeleteSelectedNotes = notesViewModel::deleteSelectedNotes,
                        onMoveSelectedNotes = notesViewModel::moveSelectedNotesToFolder,
                        onTogglePin = notesViewModel::togglePin,
                        onOpenFoldersManager = { currentScreen = Screen.Folders },
                        onOpenStats = { currentScreen = Screen.Stats },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onCreateFolder = notesViewModel::createFolder,
                        onClearUserMessage = notesViewModel::clearUserMessage
                    )
                }

                is Screen.Editor -> {
                    // إنشاء ViewModel خاص بالمحرر لكل جلسة تحرير
                    val editorViewModel: NoteEditorViewModel = viewModel(
                        key = "editor_${screen.noteId ?: "new"}",
                        factory = NoteEditorViewModel.Factory
                    )

                    LaunchedEffect(screen.noteId) {
                        editorViewModel.loadNote(screen.noteId)
                    }

                    val editorUiState by editorViewModel.uiState.collectAsStateWithLifecycle()
                    val availableFolders by editorViewModel.availableFolders.collectAsStateWithLifecycle()

                    NoteEditorScreen(
                        uiState = editorUiState,
                        availableFolders = availableFolders,
                        onTitleChanged = editorViewModel::onTitleChanged,
                        onContentChanged = editorViewModel::onContentChanged,
                        onBoldClick = editorViewModel::applyBold,
                        onItalicClick = editorViewModel::applyItalic,
                        onHeadingClick = { editorViewModel.applyHeading(1) },
                        onBulletListClick = editorViewModel::applyBulletList,
                        onNumberedListClick = editorViewModel::applyNumberedList,
                        onChecklistClick = editorViewModel::applyChecklist,
                        onQuoteClick = editorViewModel::applyQuote,
                        onTogglePin = editorViewModel::togglePin,
                        onToggleRtl = editorViewModel::toggleRtl,
                        onAddImage = editorViewModel::addImage,
                        onRemoveImage = editorViewModel::removeImage,
                        onSetFolder = editorViewModel::setFolder,
                        onAddTag = editorViewModel::addTag,
                        onRemoveTag = editorViewModel::removeTag,
                        onSetColor = editorViewModel::setColor,
                        onSaveNote = { onComplete -> editorViewModel.saveNote(onComplete) },
                        onDeleteNote = { onDeleted -> editorViewModel.deleteCurrentNote(onDeleted) },
                        onShareNote = editorViewModel::shareNote,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onCreateFolder = notesViewModel::createFolder
                    )
                }

                is Screen.Folders -> {
                    FoldersScreen(
                        folders = notesUiState.folders,
                        onSelectFolder = { folderId ->
                            notesViewModel.selectFolder(folderId)
                            currentScreen = Screen.Home
                        },
                        onCreateFolder = notesViewModel::createFolder,
                        onUpdateFolder = notesViewModel::updateFolder,
                        onDeleteFolder = notesViewModel::deleteFolder,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }

                is Screen.Stats -> {
                    StatsScreen(
                        stats = notesUiState.stats,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        lockState = lockUiState,
                        onToggleSecurity = lockViewModel::toggleSecurity,
                        onSetPin = lockViewModel::setPin,
                        onRemovePin = lockViewModel::removePin,
                        onToggleBiometric = lockViewModel::toggleBiometric,
                        onLockNow = {
                            lockViewModel.lockNow()
                            currentScreen = Screen.Home
                        },
                        onExportBackup = notesViewModel::exportBackup,
                        onRestoreBackupFromUri = notesViewModel::restoreBackupFromUri,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }

        // واجهة القفل الآمنة في حال كان التطبيق مقفلاً
        if (!lockUiState.isUnlocked && lockUiState.isSecurityEnabled) {
            LockOverlay(
                enteredPin = lockUiState.enteredPin,
                errorMessage = lockUiState.pinError,
                isBiometricAvailable = lockUiState.isBiometricAvailable && lockUiState.isBiometricEnabled,
                onDigitClick = lockViewModel::onPinDigitEntered,
                onBackspaceClick = lockViewModel::onPinBackspace,
                onBiometricClick = { lockViewModel.triggerBiometricPrompt(activity) }
            )
        }
    }
}
