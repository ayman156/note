package com.example.di

import android.content.Context
import com.example.data.db.NoteDatabase
import com.example.data.repository.BackupRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.security.BiometricAuthHelper
import com.example.data.security.SecurityManager

/**
 * حاوية حقن التبعيات (Dependency Injection Container)
 * توفر نسخاً موحدة واحادية (Singletons) من المستودعات وقواعد البيانات ومدير الأمان.
 */
interface AppContainer {
    val noteRepository: NoteRepository
    val backupRepository: BackupRepository
    val securityManager: SecurityManager
    val biometricAuthHelper: BiometricAuthHelper
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(context)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(
            noteDao = database.noteDao(),
            folderDao = database.folderDao()
        )
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepository(
            context = context,
            noteRepository = noteRepository
        )
    }

    override val securityManager: SecurityManager by lazy {
        SecurityManager(context)
    }

    override val biometricAuthHelper: BiometricAuthHelper by lazy {
        BiometricAuthHelper(context)
    }
}
