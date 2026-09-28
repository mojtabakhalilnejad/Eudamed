package com.openregulatory.eudamedsearch.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openregulatory.eudamedsearch.data.model.DataSource
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.DeviceSearchFilters
import com.openregulatory.eudamedsearch.data.repository.DeviceRepository
import com.openregulatory.eudamedsearch.data.repository.SettingsStore
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
    val officialApiKey: String = ""
) {
    val hasMore: Boolean get() = page + 1 < totalPages
}

class SearchViewModel(
    application: Application,
    private val repository: DeviceRepository = DeviceRepository()
) : AndroidViewModel(application) {

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
        _uiState.update { it.copy(filters = DeviceSearchFilters(), devices = emptyList(), hasSearchedOnce = false) }
    }

    suspend fun suggestProductNames(partial: String): List<String> = repository.suggestProductNames(partial)
    suspend fun suggestManufacturerNames(partial: String): List<String> = repository.suggestManufacturerNames(partial)

    fun search() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearchedOnce = true) }
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
