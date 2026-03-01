package com.diary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.data.DiaryEntry
import com.diary.data.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EntriesState {
    object Loading : EntriesState()
    data class Success(val data: Map<String, Map<String, List<DiaryEntry>>>) : EntriesState()
    data class Error(val message: String) : EntriesState()
}

sealed class ContentState {
    object Idle : ContentState()
    object Loading : ContentState()
    data class Success(val text: String) : ContentState()
    data class Error(val message: String) : ContentState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _entriesState = MutableStateFlow<EntriesState>(EntriesState.Loading)
    val entriesState: StateFlow<EntriesState> = _entriesState

    private val _contentState = MutableStateFlow<ContentState>(ContentState.Idle)
    val contentState: StateFlow<ContentState> = _contentState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun loadEntries() {
        _entriesState.value = EntriesState.Loading
        viewModelScope.launch {
            _entriesState.value = try {
                EntriesState.Success(repository.listEntries())
            } catch (e: Exception) {
                EntriesState.Error(e.message ?: "Failed to load entries")
            }
        }
    }

    fun loadEntry(entry: DiaryEntry) {
        _contentState.value = ContentState.Loading
        viewModelScope.launch {
            _contentState.value = try {
                ContentState.Success(repository.getEntryContent(entry))
            } catch (e: Exception) {
                ContentState.Error(e.message ?: "Failed to load entry")
            }
        }
    }

    fun saveEntry(content: String, onSuccess: () -> Unit) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            try {
                repository.createEntry(content)
                _saveState.value = SaveState.Success
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save entry")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun resetContentState() {
        _contentState.value = ContentState.Idle
    }
}
