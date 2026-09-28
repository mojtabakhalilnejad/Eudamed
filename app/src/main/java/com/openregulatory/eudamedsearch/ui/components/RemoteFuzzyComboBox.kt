package com.openregulatory.eudamedsearch.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Same idea as [FuzzyComboBox], but for fields where the option list isn't static reference data —
 * product name and manufacturer name — so suggestions come from the live API as the user types
 * (debounced), then are fuzzy-ranked exactly like the static combo boxes. Falls back to plain free
 * text if the network call fails or returns nothing, so typing never gets blocked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteFuzzyComboBox(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    fetchSuggestions: suspend (String) -> List<String>,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    debounceMillis: Long = 350
) {
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        if (text.isBlank()) {
            suggestions = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(debounceMillis)
        suggestions = runCatching { fetchSuggestions(text) }.getOrDefault(emptyList())
        loading = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded && (suggestions.isNotEmpty() || loading),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                onTextChange(it)
                expanded = true
            },
            label = { Text(label) },
            supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
            singleLine = true,
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.heightIn(max = 20.dp))
                } else {
                    TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                items(suggestions) { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            onTextChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
