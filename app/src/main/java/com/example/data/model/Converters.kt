package com.example.data.model

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * محولات Room لتحويل القوائم المعقدة (مثل الوسوم والروابط) إلى سلاسل نصية JSON وتخزينها في قاعدة البيانات.
 */
class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
