package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.FolderSelectorDialog
import com.example.ui.components.NoteCard
import com.example.ui.viewmodel.NotesUiState

/**
 * الشاشة الرئيسية لتطبيق Note A.
 * تعرض الملاحظات بشكل فوري وسلس، مع دعم المجلدات والوسوم الاختيارية، والبحث السريع، والتحديد المتعدد.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: NotesUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onSelectTag: (String?) -> Unit,
    onToggleViewMode: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onToggleNoteSelection: (Long) -> Unit,
    onSelectAllNotes: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelectedNotes: () -> Unit,
    onMoveSelectedNotes: (folderId: Long?, folderName: String?) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onOpenFoldersManager: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onClearUserMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoveFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.isMultiSelectMode) {
                // شريط إجراءات التحديد المتعدد
                TopAppBar(
                    title = {
                        Text(
                            text = "${uiState.selectedNoteIds.size} محددة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClearSelection) {
                            Icon(Icons.Filled.Close, contentDescription = "إلغاء التحديد")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSelectAllNotes) {
                            Icon(Icons.Filled.SelectAll, contentDescription = "تحديد الكل")
                        }
                        IconButton(onClick = { showMoveFolderDialog = true }) {
                            Icon(Icons.Filled.DriveFileMove, contentDescription = "نقل لمجلد")
                        }
                        IconButton(onClick = onDeleteSelectedNotes) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف المحدد",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                // شريط التطبيق الرئيسي
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "A",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Note A",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleViewMode,
                            modifier = Modifier.testTag("toggle_view_mode")
                        ) {
                            Icon(
                                imageVector = if (uiState.isGridView) Icons.Filled.ViewAgenda else Icons.Filled.GridView,
                                contentDescription = if (uiState.isGridView) "عرض قائمة" else "عرض شبكي"
                            )
                        }
                        IconButton(onClick = onOpenStats) {
                            Icon(Icons.Filled.BarChart, contentDescription = "الإحصائيات")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "الإعدادات")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isMultiSelectMode) {
                FloatingActionButton(
                    onClick = onNewNoteClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_new_note")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "إنشاء ملاحظة جديدة",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // شريط البحث المدمج
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                "بحث في الملاحظات والمحتوى والوسوم...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textDirection = TextDirection.ContentOrLtr
                                )
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_field")
                    )
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "مسح البحث",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // شريط تصفية المجلدات (اختياري بالكامل - يظهر المجلدات بطريقة انسيابية)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // خيار كل الملاحظات
                FilterChip(
                    selected = uiState.selectedFolderId == null,
                    onClick = { onSelectFolder(null) },
                    label = { Text("الكل") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // خيار غير مصنفة
                FilterChip(
                    selected = uiState.selectedFolderId == -1L,
                    onClick = { onSelectFolder(-1L) },
                    label = { Text("عام") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // المجلدات المخصصة
                uiState.folders.forEach { folder ->
                    FilterChip(
                        selected = uiState.selectedFolderId == folder.id,
                        onClick = { onSelectFolder(folder.id) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(folder.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                // زر إدارة المجلدات
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenFoldersManager)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "إدارة المجلدات",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "مجلد",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // شريط تصفية الوسوم إن وجدت
            if (uiState.allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.allTags.forEach { tag ->
                        val isSelected = uiState.selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectTag(if (isSelected) null else tag)
                                }
                        ) {
                            Text(
                                text = "#$tag",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // قائمة الملاحظات أو الحالة الفارغة
            if (uiState.notes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.NoteAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "لا توجد نتائج مطابقة للبحث" else "لا توجد ملاحظات بعد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "جرّب البحث بكلمات أخرى" else "اضغط على زر (+) بالأسفل لتدوين أول فكرة!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                if (uiState.isGridView) {
                    // عرض شبكي متجاوب ومريح
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_staggered_grid")
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            val isSelected = uiState.selectedNoteIds.contains(note.id)
                            NoteCard(
                                note = note,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isMultiSelectMode,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        onToggleNoteSelection(note.id)
                                    } else {
                                        onNoteClick(note.id)
                                    }
                                },
                                onLongClick = {
                                    onToggleNoteSelection(note.id)
                                },
                                onPinClick = {
                                    onTogglePin(note)
                                }
                            )
                        }
                    }
                } else {
                    // عرض قائمة عمودية
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_linear_list")
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            val isSelected = uiState.selectedNoteIds.contains(note.id)
                            NoteCard(
                                note = note,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isMultiSelectMode,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        onToggleNoteSelection(note.id)
                                    } else {
                                        onNoteClick(note.id)
                                    }
                                },
                                onLongClick = {
                                    onToggleNoteSelection(note.id)
                                },
                                onPinClick = {
                                    onTogglePin(note)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMoveFolderDialog) {
        FolderSelectorDialog(
            folders = uiState.folders,
            currentFolderId = null,
            onFolderSelected = { folderId, folderName ->
                onMoveSelectedNotes(folderId, folderName)
                showMoveFolderDialog = false
            },
            onCreateFolder = onCreateFolder,
            onDismiss = { showMoveFolderDialog = false }
        )
    }
}
