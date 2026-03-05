package com.diary.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diary.data.DiaryEntry
import com.diary.viewmodel.ContentState
import com.diary.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryViewScreen(
    vm: DiaryViewModel,
    entry: DiaryEntry,
    onBack: () -> Unit,
) {
    val state by vm.contentState.collectAsState()

    LaunchedEffect(entry) {
        vm.resetContentState()
        vm.loadEntry(entry)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${entry.year} / ${entry.month} / ${entry.filename.removePrefix("entry-").removeSuffix(".txt")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                is ContentState.Idle, is ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ContentState.Error -> {
                    Text(
                        "Error: ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ContentState.Success -> {
                    Text(
                        text = s.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                    )
                }
            }
        }
    }
}
