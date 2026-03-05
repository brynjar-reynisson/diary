package com.diary.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.diary.data.DiaryEntry
import com.diary.viewmodel.DiaryViewModel
import com.diary.viewmodel.EntriesState

private val MONTH_NAMES = arrayOf(
    "Jan","Feb","Mar","Apr","May","Jun",
    "Jul","Aug","Sep","Oct","Nov","Dec"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    vm: DiaryViewModel,
    onEntryClick: (DiaryEntry) -> Unit,
    onNewEntry: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by vm.entriesState.collectAsState()
    var collapsedYears by remember { mutableStateOf(emptySet<String>()) }
    var collapsedMonths by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(Unit) { vm.loadEntries() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Diary",
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewEntry,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New entry", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is EntriesState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EntriesState.Error -> {
                    Text(
                        "Error: ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is EntriesState.Success -> {
                    val data = s.data
                    if (data.isEmpty()) {
                        Text(
                            "No entries yet. Tap + to write your first.",
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val years = data.keys.sortedDescending()
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            for (year in years) {
                                item(key = "y-$year") {
                                    YearHeader(
                                        year = year,
                                        collapsed = year in collapsedYears,
                                        onToggle = {
                                            collapsedYears = if (year in collapsedYears)
                                                collapsedYears - year else collapsedYears + year
                                        }
                                    )
                                }
                                if (year !in collapsedYears) {
                                    val months = data[year]!!.keys.sortedDescending()
                                    for (month in months) {
                                        val monthKey = "$year-$month"
                                        item(key = "m-$monthKey") {
                                            MonthHeader(
                                                month = month,
                                                collapsed = monthKey in collapsedMonths,
                                                onToggle = {
                                                    collapsedMonths = if (monthKey in collapsedMonths)
                                                        collapsedMonths - monthKey else collapsedMonths + monthKey
                                                }
                                            )
                                        }
                                        if (monthKey !in collapsedMonths) {
                                            items(
                                                items = data[year]!![month]!!,
                                                key = { it.filename }
                                            ) { entry ->
                                                EntryRow(entry = entry, onClick = { onEntryClick(entry) })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearHeader(year: String, collapsed: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (collapsed) "▶  $year" else "▼  $year",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
    HorizontalDivider()
}

@Composable
private fun MonthHeader(month: String, collapsed: Boolean, onToggle: () -> Unit) {
    val idx = month.toIntOrNull()?.minus(1) ?: -1
    val name = if (idx in 0..11) MONTH_NAMES[idx] else month
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (collapsed) "▶  $name" else "▼  $name",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
    }
}

@Composable
private fun EntryRow(entry: DiaryEntry, onClick: () -> Unit) {
    val label = entry.filename.removePrefix("entry-").removeSuffix(".txt")
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 56.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
    )
}
