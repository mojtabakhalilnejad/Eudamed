package com.openregulatory.eudamedsearch.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openregulatory.eudamedsearch.R
import com.openregulatory.eudamedsearch.data.model.DataSource
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.ReferenceData
import com.openregulatory.eudamedsearch.ui.components.DateRangeField
import com.openregulatory.eudamedsearch.ui.components.DeviceListItem
import com.openregulatory.eudamedsearch.ui.components.FuzzyComboBox
import com.openregulatory.eudamedsearch.ui.components.RemoteFuzzyComboBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onDeviceClick: (Device) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                SourceSelector(
                    source = state.source,
                    onSourceChange = viewModel::setSource,
                    officialApiKeySet = state.officialApiKey.isNotBlank(),
                    onOpenSettings = onOpenSettings
                )
            }

            item {
                RemoteFuzzyComboBox(
                    label = stringResourceCompat(R.string.field_product_name),
                    text = state.filters.productName,
                    onTextChange = { text -> viewModel.updateFilters { it.copy(productName = text) } },
                    fetchSuggestions = { partial -> viewModel.suggestProductNames(partial) },
                    supportingText = stringResourceCompat(R.string.fuzzy_hint)
                )
            }

            item {
                RemoteFuzzyComboBox(
                    label = stringResourceCompat(R.string.field_manufacturer),
                    text = state.filters.manufacturerName,
                    onTextChange = { text -> viewModel.updateFilters { it.copy(manufacturerName = text) } },
                    fetchSuggestions = { partial -> viewModel.suggestManufacturerNames(partial) }
                )
            }

            item {
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
            }

            item {
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
            }

            item {
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
            }

            item {
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
            }

            item {
                androidx.compose.material3.OutlinedTextField(
                    value = state.filters.udi,
                    onValueChange = { text -> viewModel.updateFilters { it.copy(udi = text) } },
                    label = { Text(stringResourceCompat(R.string.field_udi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DateRangeField(
                    label = stringResourceCompat(R.string.field_reg_date),
                    from = state.filters.registrationDateFrom,
                    to = state.filters.registrationDateTo,
                    onFromChange = { d -> viewModel.updateFilters { it.copy(registrationDateFrom = d) } },
                    onToChange = { d -> viewModel.updateFilters { it.copy(registrationDateTo = d) } }
                )
            }

            item {
                DateRangeField(
                    label = stringResourceCompat(R.string.field_ce_date),
                    from = state.filters.ceDateFrom,
                    to = state.filters.ceDateTo,
                    onFromChange = { d -> viewModel.updateFilters { it.copy(ceDateFrom = d) } },
                    onToChange = { d -> viewModel.updateFilters { it.copy(ceDateTo = d) } }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = viewModel::search,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResourceCompat(R.string.btn_search))
                    }
                    OutlinedButton(onClick = viewModel::clearFilters) {
                        Text(stringResourceCompat(R.string.btn_clear))
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

            if (state.hasSearchedOnce && !state.isLoading) {
                item {
                    Text(
                        text = stringResourceCompatFormat(R.string.results_count, state.totalElements),
                        style = MaterialTheme.typography.labelSmall
                    )
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

            items(state.devices, key = { it.id.ifBlank { it.hashCode().toString() } }) { device ->
                DeviceListItem(device = device, onClick = { onDeviceClick(device) })
            }

            if (state.devices.isNotEmpty() && state.hasMore) {
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

@Composable
private fun SourceSelector(
    source: DataSource,
    onSourceChange: (DataSource) -> Unit,
    officialApiKeySet: Boolean,
    onOpenSettings: () -> Unit
) {
    Column {
        Text(stringResourceCompat(R.string.source_toggle), style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = source == DataSource.SITE,
                onClick = { onSourceChange(DataSource.SITE) },
                label = { Text(stringResourceCompat(R.string.source_site)) }
            )
            FilterChip(
                selected = source == DataSource.OFFICIAL_API,
                onClick = {
                    onSourceChange(DataSource.OFFICIAL_API)
                    if (!officialApiKeySet) onOpenSettings()
                },
                label = { Text(stringResourceCompat(R.string.source_official)) }
            )
        }
    }
}

// Small helpers so composables above read cleanly without importing stringResource everywhere.
@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringResourceCompatFormat(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id, *args)
