package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Converters
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Note A", appName)
  }

  @Test
  fun `test note converters serialization`() {
    val converters = Converters()
    val tags = listOf("أفكار", "عمل", "مهم")
    val json = converters.fromStringList(tags)
    val restored = converters.toStringList(json)
    assertEquals(tags, restored)
  }

  @Test
  fun `test note entity creation`() {
    val note = NoteEntity(
      id = 1L,
      title = "ملاحظة أولى",
      content = "محتوى الملاحظة للتجربة والتحقق",
      tags = listOf("تطوير"),
      isPinned = true
    )
    assertEquals("ملاحظة أولى", note.title)
    assertTrue(note.isPinned)
    assertEquals(1, note.tags.size)
  }
}
