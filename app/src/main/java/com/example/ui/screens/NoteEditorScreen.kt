package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FolderEntity
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.FolderSelectorDialog
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.RichTextToolbar
import com.example.ui.components.TagSelectorDialog
import com.example.ui.viewmodel.NoteEditorUiState

/**
 * شاشة تحرير الملاحظة المتكاملة:
 * تدعم الكتابة بحرية بدون سقف لحجم النص، إرفاق صور متعددة وتكبيرها،
 * تنسيق غني (غامق، مائل، قوائم، عناوين)، دعم اتجاهات BiDi كامل (العربية والإنجليزية)،
 * والمجلدات والوسوم ومشاركة النص والوسائط.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    uiState: NoteEditorUiState,
    availableFolders: List<FolderEntity>,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (TextFieldValue) -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onHeadingClick: () -> Unit,
    onBulletListClick: () -> Unit,
    onNumberedListClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleRtl: () -> Unit,
    onAddImage: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    onSetFolder: (Long?, String?) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSetColor: (String?) -> Unit,
    onSaveNote: (() -> Unit) -> Unit,
    onDeleteNote: (() -> Unit) -> Unit,
    onShareNote: (android.content.Context) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // استدعاء منتقي الصور للوسائط
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            onAddImage(uri.toString())
        }
    }

    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var selectedImageForZoom by remember { mutableStateOf<String?>(null) }

    // الحفظ التلقائي عند الخروج
    BackHandler {
        onSaveNote {
            onNavigateBack()
        }
    }

    val defaultBgColor = MaterialTheme.colorScheme.background
    val parsedBgColor = remember(uiState.colorHex, defaultBgColor) {
        if (!uiState.colorHex.isNullOrBlank() && uiState.colorHex != "#00000000") {
            try {
                Color(android.graphics.Color.parseColor(uiState.colorHex))
            } catch (e: Exception) {
                defaultBgColor
            }
        } else {
            defaultBgColor
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onSaveNote {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع وحفظ"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (uiState.isPinned) "إلغاء التثبيت" else "تثبيت",
                            tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showColorDialog = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "تغيير اللون")
                    }
                    IconButton(onClick = { onShareNote(context) }) {
                        Icon(Icons.Filled.Share, contentDescription = "مشاركة الملاحظة")
                    }
                    if (uiState.noteId != null && uiState.noteId > 0) {
                        IconButton(onClick = { onDeleteNote { onNavigateBack() } }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف الملاحظة",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = parsedBgColor
                )
            )
        },
        bottomBar = {
            RichTextToolbar(
                onBoldClick = onBoldClick,
                onItalicClick = onItalicClick,
                onHeadingClick = onHeadingClick,
                onBulletListClick = onBulletListClick,
                onNumberedListClick = onNumberedListClick,
                onChecklistClick = onChecklistClick,
                onQuoteClick = onQuoteClick,
                onAddImageClick = { photoPickerLauncher.launch("image/*") },
                onTagClick = { showTagDialog = true },
                onFolderClick = { showFolderDialog = true },
                onColorClick = { showColorDialog = true },
                isRtl = uiState.isRtl,
                onToggleRtl = onToggleRtl,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        containerColor = parsedBgColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // شريط مؤشرات المجلد والوسوم أعلى الملاحظة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // شارة المجلد
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFolderDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.selectedFolderName ?: "بدون مجلد",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // شارة الوسوم
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showTagDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.tags.isEmpty()) "إضافة وسم" else "${uiState.tags.size} وسوم",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // عرض الوسوم الحالية كشرائح
                uiState.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "#$tag",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // حقل العنوان
            BasicTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textDirection = if (uiState.isRtl) TextDirection.ContentOrLtr else TextDirection.Ltr
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = "العنوان",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textDirection = if (uiState.isRtl) TextDirection.ContentOrLtr else TextDirection.Ltr
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // معرض الصور المرفقة إن وجدت مع إمكانية التكبير والحذف
            if (uiState.imageUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.imageUris.forEach { uriString ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedImageForZoom = uriString }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uriString)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "صورة مرفقة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onRemoveImage(uriString) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "حذف الصورة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // حقل محتوى الملاحظة الرئيسي (يدعم نصوص غير محدودة واتجاه BiDi طبيعي)
            BasicTextField(
                value = uiState.content,
                onValueChange = onContentChanged,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textDirection = if (uiState.isRtl) TextDirection.ContentOrLtr else TextDirection.Ltr
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    ) {
                        if (uiState.content.text.isEmpty()) {
                            Text(
                                text = "ابدأ بكتابة أفكارك هنا...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textDirection = if (uiState.isRtl) TextDirection.ContentOrLtr else TextDirection.Ltr
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_content_input")
            )
        }
    }

    if (showFolderDialog) {
        FolderSelectorDialog(
            folders = availableFolders,
            currentFolderId = uiState.selectedFolderId,
            onFolderSelected = { folderId, folderName ->
                onSetFolder(folderId, folderName)
                showFolderDialog = false
            },
            onCreateFolder = onCreateFolder,
            onDismiss = { showFolderDialog = false }
        )
    }

    if (showTagDialog) {
        TagSelectorDialog(
            currentTags = uiState.tags,
            onAddTag = onAddTag,
            onRemoveTag = onRemoveTag,
            onDismiss = { showTagDialog = false }
        )
    }

    if (showColorDialog) {
        ColorPickerDialog(
            selectedColorHex = uiState.colorHex,
            onColorSelected = onSetColor,
            onDismiss = { showColorDialog = false }
        )
    }

    selectedImageForZoom?.let { imageUri ->
        ImageViewerDialog(
            imageUri = imageUri,
            onDelete = {
                onRemoveImage(imageUri)
                selectedImageForZoom = null
            },
            onDismiss = { selectedImageForZoom = null }
        )
    }
}
