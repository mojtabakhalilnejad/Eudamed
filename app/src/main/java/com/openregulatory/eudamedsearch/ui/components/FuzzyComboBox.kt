package com.openregulatory.eudamedsearch.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openregulatory.eudamedsearch.data.model.ComboOption
import com.openregulatory.eudamedsearch.util.FuzzyMatcher

/**
 * A Material 3 combo box: an editable text field that, as the user types, shows a dropdown of
 * [allOptions] ranked by [FuzzyMatcher] similarity to what was typed — so a partial or slightly
 * misspelled entry ("filips" or just "hue") still surfaces "Philips Hue Sensor". The user can
 * either pick a suggestion (which resolves to that option's stable [ComboOption.code] for exact
 * server-side filtering) or keep free-typed text (used for local fuzzy filtering only).
 *
 * This single composable backs every "similar match" field requested: product name, manufacturer,
 * country, risk class, device status and legislation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuzzyComboBox(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    allOptions: List<ComboOption>,
    onOptionSelected: (ComboOption) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    maxSuggestions: Int = 8
) {
    var expanded by remember { mutableStateOf(false) }

    val suggestions = remember(text, allOptions) {
        if (text.isBlank()) {
            allOptions.take(maxSuggestions)
        } else {
            FuzzyMatcher.filterSorted(text, allOptions, threshold = 20) { it.label }.take(maxSuggestions)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
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
            trailingIcon = { TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        // ExposedDropdownMenu is a member of ExposedDropdownMenuBoxScope (the implicit receiver of
        // this trailing lambda), not a top-level function, so it's called bare here — same as
        // menuAnchor() above — rather than imported or package-qualified.
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                items(suggestions) { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
