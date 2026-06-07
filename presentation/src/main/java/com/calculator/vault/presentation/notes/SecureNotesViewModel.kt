package com.calculator.vault.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.SecureNote
import com.calculator.vault.domain.usecase.ObserveSecureNotesUseCase
import com.calculator.vault.domain.usecase.UpsertSecureNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecureNotesViewModel @Inject constructor(
    observeSecureNotesUseCase: ObserveSecureNotesUseCase,
    private val upsertSecureNoteUseCase: UpsertSecureNoteUseCase,
) : ViewModel() {
    val notes = observeSecureNotesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveNote(note: SecureNote) {
        viewModelScope.launch {
            upsertSecureNoteUseCase(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }
}
