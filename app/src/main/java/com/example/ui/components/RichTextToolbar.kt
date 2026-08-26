package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * شريط أدوات التنسيق الغني أسفل شاشة تحرير الملاحظة.
 */
@Composable
fun RichTextToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onHeadingClick: () -> Unit,
    onBulletListClick: () -> Unit,
    onNumberedListClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onAddImageClick: () -> Unit,
    onTagClick: () -> Unit,
    onFolderClick: () -> Unit,
    onColorClick: () -> Unit,
    isRtl: Boolean,
    onToggleRtl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolbarButton(
                icon = Icons.Filled.FormatBold,
                description = "غامق",
                onClick = onBoldClick,
                testTag = "toolbar_bold"
            )
            ToolbarButton(
                icon = Icons.Filled.FormatItalic,
                description = "مائل",
                onClick = onItalicClick,
                testTag = "toolbar_italic"
            )
            ToolbarButton(
                icon = Icons.Filled.FormatSize,
                description = "عنوان",
                onClick = onHeadingClick,
                testTag = "toolbar_heading"
            )
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                description = "قائمة نقطية",
                onClick = onBulletListClick,
                testTag = "toolbar_bullets"
            )
            ToolbarButton(
                icon = Icons.Filled.FormatListNumbered,
                description = "قائمة رقمية",
                onClick = onNumberedListClick,
                testTag = "toolbar_numbered"
            )
            ToolbarButton(
                icon = Icons.Filled.Checklist,
                description = "قائمة مهام",
                onClick = onChecklistClick,
                testTag = "toolbar_checklist"
            )
            ToolbarButton(
                icon = Icons.Filled.FormatQuote,
                description = "اقتباس",
                onClick = onQuoteClick,
                testTag = "toolbar_quote"
            )
            ToolbarButton(
                icon = Icons.Filled.AddPhotoAlternate,
                description = "إضافة صورة",
                onClick = onAddImageClick,
                testTag = "toolbar_image"
            )
            ToolbarButton(
                icon = Icons.Filled.LocalOffer,
                description = "وسوم",
                onClick = onTagClick,
                testTag = "toolbar_tag"
            )
            ToolbarButton(
                icon = Icons.Filled.Folder,
                description = "مجلد",
                onClick = onFolderClick,
                testTag = "toolbar_folder"
            )
            ToolbarButton(
                icon = Icons.Filled.Palette,
                description = "لون الملاحظة",
                onClick = onColorClick,
                testTag = "toolbar_color"
            )
            ToolbarButton(
                icon = if (isRtl) Icons.AutoMirrored.Filled.FormatAlignRight else Icons.AutoMirrored.Filled.FormatAlignLeft,
                description = if (isRtl) "اتجاه عربي (RTL)" else "اتجاه إنجليزي (LTR)",
                onClick = onToggleRtl,
                testTag = "toolbar_rtl"
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}
