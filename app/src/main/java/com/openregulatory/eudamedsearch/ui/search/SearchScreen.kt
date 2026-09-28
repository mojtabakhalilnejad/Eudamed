package com.openregulatory.eudamedsearch.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openregulatory.eudamedsearch.R
import com.openregulatory.eudamedsearch.data.model.DataSource
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.ReferenceData
import com.openregulatory.eudamedsearch.data.model.SortOption
import com.openregulatory.eudamedsearch.ui.components.DateRangeField
import com.openregulatory.eudamedsearch.ui.components.DeviceListItem
import com.openregulatory.eudamedsearch.ui.components.FuzzyComboBox
import com.openregulatory.eudamedsearch.ui.components.RemoteFuzzyComboBox
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onDeviceClick: (Device) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Client-side filtering of the already-returned result page, on top of the server-side
    // search filters — lets the person narrow what's on screen (e.g. "only Class III") without
    // re-querying. "همه" (null) means no filter on that field.
    var riskClassFilter by remember { mutableStateOf<String?>(null) }
    var deviceStatusFilter by remember { mutableStateOf<String?>(null) }
    var countryFilter by remember { mutableStateOf<String?>(null) }
    var legislationFilter by remember { mutableStateOf<String?>(null) }

    val filteredDevices = state.devices.filter { device ->
        (riskClassFilter == null || device.riskClassLabel == riskClassFilter) &&
            (deviceStatusFilter == null || device.deviceStatusLabel == deviceStatusFilter) &&
            (countryFilter == null || device.manufacturerCountry == countryFilter) &&
            (legislationFilter == null || device.legislationLabel == legislationFilter)
    }
    val visibleDevices = sortDevices(filteredDevices, state.sortOption)
    val hasActiveResultFilter = riskClassFilter != null || deviceStatusFilter != null ||
        countryFilter != null || legislationFilter != null

    fun clearResultFilters() {
        riskClassFilter = null
        deviceStatusFilter = null
        countryFilter = null
        legislationFilter = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResourceCompat(R.string.search_title)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SourceSelector(
                            source = state.source,
                            onSourceChange = viewModel::setSource,
                            officialApiKeySet = state.officialApiKey.isNotBlank(),
                            onOpenSettings = onOpenSettings
                        )

                        RemoteFuzzyComboBox(
                            label = stringResourceCompat(R.string.field_product_name),
                            text = state.filters.productName,
                            onTextChange = { text -> viewModel.updateFilters { it.copy(productName = text) } },
                            fetchSuggestions = { partial -> viewModel.suggestProductNames(partial) },
                            supportingText = stringResourceCompat(R.string.fuzzy_hint)
                        )

                        RemoteFuzzyComboBox(
                            label = stringResourceCompat(R.string.field_manufacturer),
                            text = state.filters.manufacturerName,
                            onTextChange = { text -> viewModel.updateFilters { it.copy(manufacturerName = text) } },
                            fetchSuggestions = { partial -> viewModel.suggestManufacturerNames(partial) }
                        )

                        FuzzyComboBox(
                            label = stringResourceCompat(R.string.field_country),
                            text = state.filters.countryText,
                            onTextChange = { text ->
                                viewModel.updateFilters { it.copy(countryText = text, countryCode = null) }
                            },
                            allOptions = ReferenceData.countries,
                            onOptionSelected = { option ->
                                viewModel.updateFilters { it.copy(countryText = option.label, countryCode = option.code) }
                            }
                        )

                        FuzzyComboBox(
                            label = stringResourceCompat(R.string.field_risk_class),
                            text = state.filters.riskClassText,
                            onTextChange = { text ->
                                viewModel.updateFilters { it.copy(riskClassText = text, riskClassCode = null) }
                            },
                            allOptions = ReferenceData.riskClasses,
                            onOptionSelected = { option ->
                                viewModel.updateFilters { it.copy(riskClassText = option.label, riskClassCode = option.code) }
                            }
                        )

                        FuzzyComboBox(
                            label = stringResourceCompat(R.string.field_device_status),
                            text = state.filters.deviceStatusText,
                            onTextChange = { text ->
                                viewModel.updateFilters { it.copy(deviceStatusText = text, deviceStatusCode = null) }
                            },
                            allOptions = ReferenceData.deviceStatuses,
                            onOptionSelected = { option ->
                                viewModel.updateFilters { it.copy(deviceStatusText = option.label, deviceStatusCode = option.code) }
                            }
                        )

                        FuzzyComboBox(
                            label = stringResourceCompat(R.string.field_legislation),
                            text = state.filters.legislationText,
                            onTextChange = { text ->
                                viewModel.updateFilters { it.copy(legislationText = text, legislationCode = null) }
                            },
                            allOptions = ReferenceData.legislations,
                            onOptionSelected = { option ->
                                viewModel.updateFilters { it.copy(legislationText = option.label, legislationCode = option.code) }
                            }
                        )

                        androidx.compose.material3.OutlinedTextField(
                            value = state.filters.udi,
                            onValueChange = { text -> viewModel.updateFilters { it.copy(udi = text) } },
                            label = { Text(stringResourceCompat(R.string.field_udi)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DateRangeField(
                            label = stringResourceCompat(R.string.field_reg_date),
                            from = state.filters.registrationDateFrom,
                            to = state.filters.registrationDateTo,
                            onFromChange = { d -> viewModel.updateFilters { it.copy(registrationDateFrom = d) } },
                            onToChange = { d -> viewModel.updateFilters { it.copy(registrationDateTo = d) } }
                        )

                        DateRangeField(
                            label = stringResourceCompat(R.string.field_ce_date),
                            from = state.filters.ceDateFrom,
                            to = state.filters.ceDateTo,
                            onFromChange = { d -> viewModel.updateFilters { it.copy(ceDateFrom = d) } },
                            onToChange = { d -> viewModel.updateFilters { it.copy(ceDateTo = d) } }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    clearResultFilters()
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResourceCompat(R.string.btn_search))
                            }
                            OutlinedButton(
                                onClick = {
                                    clearResultFilters()
                                    viewModel.clearFilters()
                                }
                            ) {
                                Text(stringResourceCompat(R.string.btn_clear))
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            state.errorMessage?.let { message ->
                item {
                    Text(
                        text = stringResourceCompatFormat(R.string.error_generic, message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (state.hasSearchedOnce && !state.isLoading && state.devices.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (hasActiveResultFilter) {
                                    stringResourceCompatFormat(
                                        R.string.results_count_filtered,
                                        visibleDevices.size,
                                        state.totalElements
                                    )
                                } else {
                                    stringResourceCompatFormat(R.string.results_count, state.totalElements)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            SortMenu(
                                selected = state.sortOption,
                                onSelected = viewModel::setSortOption
                            )
                        }

                        if (state.isLoadingCeDates) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.height(16.dp).width(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResourceCompat(R.string.loading_ce_dates),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        ResultFilterBar(
                            label = stringResourceCompat(R.string.filter_risk_class),
                            selected = riskClassFilter,
                            options = state.devices.mapNotNull { it.riskClassLabel?.takeIf(String::isNotBlank) }.distinct(),
                            onSelected = { riskClassFilter = it }
                        )
                        ResultFilterBar(
                            label = stringResourceCompat(R.string.filter_device_status),
                            selected = deviceStatusFilter,
                            options = state.devices.mapNotNull { it.deviceStatusLabel?.takeIf(String::isNotBlank) }.distinct(),
                            onSelected = { deviceStatusFilter = it }
                        )
                        ResultFilterBar(
                            label = stringResourceCompat(R.string.filter_country),
                            selected = countryFilter,
                            options = state.devices.mapNotNull { it.manufacturerCountry?.takeIf(String::isNotBlank) }.distinct(),
                            onSelected = { countryFilter = it }
                        )
                        ResultFilterBar(
                            label = stringResourceCompat(R.string.filter_legislation),
                            selected = legislationFilter,
                            options = state.devices.mapNotNull { it.legislationLabel?.takeIf(String::isNotBlank) }.distinct(),
                            onSelected = { legislationFilter = it }
                        )

                        if (hasActiveResultFilter) {
                            TextButton(onClick = { clearResultFilters() }) {
                                Text(stringResourceCompat(R.string.filter_clear))
                            }
                        }
                    }
                }
            }

            if (state.hasSearchedOnce && !state.isLoading && state.devices.isEmpty() && state.errorMessage == null) {
                item {
                    Text(
                        stringResourceCompat(R.string.no_results),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (state.hasSearchedOnce && !state.isLoading && state.devices.isNotEmpty() && visibleDevices.isEmpty()) {
                item {
                    Text(
                        stringResourceCompat(R.string.no_results),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(visibleDevices, key = { it.id.ifBlank { it.hashCode().toString() } }) { device ->
                DeviceListItem(device = device, onClick = { onDeviceClick(device) })
            }

            if (state.devices.isNotEmpty() && state.hasMore && !hasActiveResultFilter) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        if (state.isLoadingMore) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        } else {
                            TextButton(onClick = viewModel::loadMore) {
                                Text(stringResourceCompat(R.string.load_more))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Applies the chosen [SortOption] client-side; missing dates always sort last regardless of
 *  direction, rather than being pulled to the front by a null-as-minimum comparison. */
private fun sortDevices(devices: List<Device>, sort: SortOption): List<Device> = when (sort) {
    SortOption.DEFAULT -> devices
    SortOption.PRODUCT_NAME_ASC -> devices.sortedBy { it.tradeName.lowercase() }
    SortOption.MANUFACTURER_ASC -> devices.sortedBy { it.manufacturerName.lowercase() }
    SortOption.RISK_CLASS -> devices.sortedBy { it.riskClassLabel ?: "￿" }
    SortOption.UPDATED_NEWEST -> devices.sortedByDescending { it.lastUpdateDate ?: LocalDate.MIN }
    SortOption.UPDATED_OLDEST -> devices.sortedWith(compareBy { it.lastUpdateDate ?: LocalDate.MAX })
    SortOption.CE_NEWEST -> devices.sortedByDescending { it.ceDate ?: LocalDate.MIN }
    SortOption.CE_OLDEST -> devices.sortedWith(compareBy { it.ceDate ?: LocalDate.MAX })
}

@Composable
private fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.DEFAULT -> stringResourceCompat(R.string.sort_default)
    SortOption.PRODUCT_NAME_ASC -> stringResourceCompat(R.string.sort_product_name_asc)
    SortOption.MANUFACTURER_ASC -> stringResourceCompat(R.string.sort_manufacturer_asc)
    SortOption.RISK_CLASS -> stringResourceCompat(R.string.sort_risk_class)
    SortOption.UPDATED_NEWEST -> stringResourceCompat(R.string.sort_updated_newest)
    SortOption.UPDATED_OLDEST -> stringResourceCompat(R.string.sort_updated_oldest)
    SortOption.CE_NEWEST -> stringResourceCompat(R.string.sort_ce_newest)
    SortOption.CE_OLDEST -> stringResourceCompat(R.string.sort_ce_oldest)
}

@Composable
private fun SortMenu(selected: SortOption, onSelected: (SortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("${stringResourceCompat(R.string.sort_by)}: ${sortOptionLabel(selected)}") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(sortOptionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

/** A single "field: value ▾" chip that opens a dropdown of the distinct values currently present
 *  in the result page, plus "همه" to clear it. Hidden when there's nothing to filter by (fewer
 *  than two distinct values, since one value can't narrow anything). */
@Composable
private fun ResultFilterBar(
    label: String,
    selected: String?,
    options: List<String>,
    onSelected: (String?) -> Unit
) {
    if (options.size < 2) return
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResourceCompat(R.string.filter_all)
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("$label: ${selected ?: allLabel}") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    expanded = false
                    onSelected(null)
                }
            )
            options.sorted().forEach { value ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    }
                )
            }
        }
    }
}

@Composable
private fun SourceSelector(
    source: DataSource,
    onSourceChange: (DataSource) -> Unit,
    officialApiKeySet: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResourceCompat(R.string.source_toggle), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SourceOptionRow(
                selected = source == DataSource.SITE,
                title = stringResourceCompat(R.string.source_site_title),
                description = stringResourceCompat(R.string.source_site_desc),
                onClick = { onSourceChange(DataSource.SITE) }
            )
            SourceOptionRow(
                selected = source == DataSource.OFFICIAL_API,
                title = stringResourceCompat(R.string.source_official_title),
                description = stringResourceCompat(R.string.source_official_desc),
                onClick = {
                    onSourceChange(DataSource.OFFICIAL_API)
                    if (!officialApiKeySet) onOpenSettings()
                }
            )
        }
    }
}

@Composable
private fun SourceOptionRow(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Small helpers so composables above read cleanly without importing stringResource everywhere.
@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringResourceCompatFormat(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id, *args)
