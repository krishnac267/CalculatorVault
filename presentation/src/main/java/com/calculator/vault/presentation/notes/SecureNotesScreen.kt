package com.calculator.vault.presentation.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.domain.model.SecureNote
import com.calculator.vault.presentation.components.GlassCard
import com.calculator.vault.presentation.components.SecureScreenEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureNotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecureNotesViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    SecureScreenEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Notes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showEditor) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.saveNote(SecureNote(title = title, body = body))
                        title = ""
                        body = ""
                        showEditor = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save securely")
                }
            }
            if (notes.isEmpty() && !showEditor) {
                Text(
                    text = "No private notes yet. Tap + to create your first secure note.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes, key = { it.id }) { note ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Text(note.body, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
