package com.yourfiles.manager.presentation.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourfiles.manager.app.App
import com.yourfiles.manager.data.repository.LocalFilesRepoImpl
import com.yourfiles.manager.domain.interactors.FileUseCases
import com.yourfiles.manager.utils.SavedMemoryTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

open class SelectableDeletableVM : ViewModel() {

    val fileUseCases = FileUseCases(LocalFilesRepoImpl(App.instance.db.localFilesDao()))

    val selectedModeOn = mutableStateOf(false)
    val selectedFiles = mutableStateOf(setOf<String>())
    val showDeleteDialog = mutableStateOf(false)
    val isDeleting = mutableStateOf(false)

    fun deleteFiles(ids: Set<String>) {
        showDeleteDialog.value = true
        pendingDeleteFiles = ids
    }

    open fun confirmDeleteFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = pendingDeleteFiles.sumOf { File(it).length() }
            withContext(Dispatchers.Main) { isDeleting.value = true }

            // Permanent delete
            pendingDeleteFiles.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    if (file.isDirectory) file.deleteRecursively()
                    else file.delete()
                }
            }

            launch {
                fileUseCases.deleteFiles(pendingDeleteFiles.toList())
            }.join()
            SavedMemoryTracker.addSavedBytes(totalBytes)
            withContext(Dispatchers.Main) {
                selectedFiles.value -= pendingDeleteFiles
                pendingDeleteFiles = emptySet()
                showDeleteDialog.value = false
                selectedModeOn.value = false
                isDeleting.value = false
            }
        }
    }

    fun cancelDelete() {
        showDeleteDialog.value = false
        pendingDeleteFiles = emptySet()
    }

    protected var pendingDeleteFiles = emptySet<String>()
}
