package com.openregulatory.eudamedsearch.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openregulatory.eudamedsearch.data.model.DataSource
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.DeviceSearchFilters
import com.openregulatory.eudamedsearch.data.model.SortOption
import com.openregulatory.eudamedsearch.data.repository.DeviceRepository
import com.openregulatory.eudamedsearch.data.repository.SettingsStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val filters: DeviceSearchFilters = DeviceSearchFilters(),
    val source: DataSource = DataSource.SITE,
    val devices: List<Device> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 1,
    val totalElements: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasSearchedOnce: Boolean = false,
    val officialApiKey: String = "",
    val sortOption: SortOption = SortOption.DEFAULT,
    val isLoadingCeDates: Boolean = false
) {
    val hasMore: Boolean get() = page + 1 < totalPages
}

// Compose's default viewModel() factory can only instantiate an AndroidViewModel through its
// single-argument (Application) constructor (found via reflection); a second constructor
// parameter — even with a default value — compiles fine but makes that constructor
// unreachable to the factory and crashes the app on launch with NoSuchMethodException. So the
// repository is a plain field, not a constructor parameter.
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeviceRepository = DeviceRepository()
    private val settings = SettingsStore(application)

    private val _uiState = MutableStateFlow(SearchUiState(officialApiKey = settings.officialApiKey))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedDevice = MutableStateFlow<Device?>(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice.asStateFlow()

    fun selectDevice(device: Device) {
        _selectedDevice.value = device
    }

    fun updateFilters(transform: (DeviceSearchFilters) -> DeviceSearchFilters) {
        _uiState.update { it.copy(filters = transform(it.filters)) }
    }

    fun setSource(source: DataSource) {
        _uiState.update { it.copy(source = source) }
    }

    fun setOfficialApiKey(key: String) {
        settings.officialApiKey = key
        _uiState.update { it.copy(officialApiKey = key) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filters = DeviceSearchFilters(),
                devices = emptyList(),
                hasSearchedOnce = false,
                sortOption = SortOption.DEFAULT
            )
        }
    }

    /** Changes the active sort. CE-date sorts need each visible device's certificate date, which
     *  the search endpoints never return — so the first time such a sort is picked, this fetches
     *  the missing dates (in parallel, one detail call per device) before applying it; devices
     *  that already have a [Device.ceDate] (from a previous CE sort) aren't re-fetched. */
    fun setSortOption(option: SortOption) {
        val current = _uiState.value
        if (!option.needsCeDate || current.devices.all { it.ceDate != null }) {
            _uiState.update { it.copy(sortOption = option) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCeDates = true) }
            val withDates = coroutineScope {
                current.devices.map { device ->
                    async {
                        if (device.ceDate != null) device
                        else device.copy(ceDate = runCatching { repository.fetchCeDate(device) }.getOrNull())
                    }
                }.awaitAll()
            }
            _uiState.update {
                it.copy(devices = withDates, sortOption = option, isLoadingCeDates = false)
            }
        }
    }

    suspend fun suggestProductNames(partial: String): List<String> = repository.suggestProductNames(partial)
    suspend fun suggestManufacturerNames(partial: String): List<String> = repository.suggestManufacturerNames(partial)

    fun search() {
        val state = _uiState.value
        viewModelScope.launch {
            // A fresh search means fresh devices with no ceDate fetched yet, so a previously
            // selected CE sort can no longer be honoured without re-fetching — reset to default
            // rather than silently sorting the new list as if those dates were still known.
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    hasSearchedOnce = true,
                    sortOption = SortOption.DEFAULT
                )
            }
            runCatching {
                repository.search(
                    filters = state.filters,
                    source = state.source,
                    page = 1,
                    officialApiKey = state.officialApiKey
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        devices = result.devices,
                        page = result.page,
                        totalPages = result.totalPages,
                        totalElements = result.totalElements,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = throwable.message ?: throwable.toString())
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            runCatching {
                repository.search(
                    filters = state.filters,
                    source = state.source,
                    page = state.page + 2,
                    officialApiKey = state.officialApiKey
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        devices = it.devices + result.devices,
                        page = result.page,
                        totalPages = result.totalPages,
                        totalElements = result.totalElements,
                        isLoadingMore = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoadingMore = false, errorMessage = throwable.message ?: throwable.toString())
                }
            }
        }
    }
}
