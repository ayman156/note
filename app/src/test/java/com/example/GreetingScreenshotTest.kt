package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.NoteEntity
import com.example.ui.components.NoteCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleNote = NoteEntity(
      id = 1L,
      title = "أفكار لتطوير Note A",
      content = "تطبيق فائق السرعة يدعم Jetpack Compose و Room Database وأمان عالي",
      tags = listOf("تطوير", "مهم"),
      folderName = "مشاريع",
      isPinned = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        NoteCard(
          note = sampleNote,
          isSelected = false,
          isSelectionMode = false,
          onClick = {},
          onLongClick = {},
          onPinClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/note_card.png")
  }
}
