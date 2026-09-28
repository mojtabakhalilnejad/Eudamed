package com.openregulatory.eudamedsearch.data.repository

import com.openregulatory.eudamedsearch.data.model.DataSource
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.DeviceDetailResponse
import com.openregulatory.eudamedsearch.data.model.DeviceSearchFilters
import com.openregulatory.eudamedsearch.data.model.DeviceSearchResult
import com.openregulatory.eudamedsearch.data.model.DeviceSummary
import com.openregulatory.eudamedsearch.data.model.OfficialDeviceRecord
import com.openregulatory.eudamedsearch.data.remote.EudamedOfficialApi
import com.openregulatory.eudamedsearch.data.remote.EudamedSiteApi
import com.openregulatory.eudamedsearch.data.remote.NetworkModule
import com.openregulatory.eudamedsearch.util.FuzzyDate
import com.openregulatory.eudamedsearch.util.FuzzyMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single entry point for everything device-search related. Hides the two very different EUDAMED
 * backends (see [EudamedSiteApi] / [EudamedOfficialApi]) behind one [Device] model, and layers the
 * fuzzy "find similar" behaviour on top of whichever raw results come back, since neither backend is
 * guaranteed to do partial/typo-tolerant matching server-side.
 *
 * Strategy per request:
 *  1. Send the strict/known filters to the server (page, language, and whichever code-based filters
 *     the chosen backend supports) to keep payloads small.
 *  2. Re-filter + re-rank the page that comes back against the *free-typed* text the user entered,
 *     using [FuzzyMatcher], so close-but-not-exact spellings still surface useful results.
 *  3. Apply date-range filters client-side with [FuzzyDate], since neither backend documents a
 *     reliable date filter for arbitrary "registration date" / "CE date" ranges.
 */
class DeviceRepository(
    private val siteApi: EudamedSiteApi = NetworkModule.siteApi,
    private val officialApi: EudamedOfficialApi = NetworkModule.officialApi
) {
    suspend fun search(
        filters: DeviceSearchFilters,
        source: DataSource,
        page: Int,
        pageSize: Int = 25,
        officialApiKey: String? = null
    ): DeviceSearchResult = withContext(Dispatchers.IO) {
        when (source) {
            DataSource.SITE -> searchSite(filters, page, pageSize)
            DataSource.OFFICIAL_API -> searchOfficial(filters, page, pageSize, officialApiKey.orEmpty())
        }
    }

    suspend fun getDeviceDetail(device: Device): DeviceDetailResponse? = withContext(Dispatchers.IO) {
        if (device.source != DataSource.SITE) return@withContext null
        runCatching {
            siteApi.getDeviceDetail(device.id, mapOf("languageIso2Code" to "en"))
        }.getOrNull()
    }

    /** Quick free-text lookup used to populate the product-name / manufacturer-name combo boxes
     *  with real, currently-registered values as the user types (debounced by the caller). */
    suspend fun suggestProductNames(partial: String): List<String> =
        suggestField(partial) { it.tradeName }

    suspend fun suggestManufacturerNames(partial: String): List<String> =
        suggestField(partial) { it.manufacturerName }

    private suspend fun suggestField(
        partial: String,
        extract: (DeviceSummary) -> String?
    ): List<String> = withContext(Dispatchers.IO) {
        if (partial.isBlank()) return@withContext emptyList()
        val response = runCatching {
            siteApi.searchDevices(
                mapOf(
                    "page" to "1",
                    "pageSize" to "50",
                    "size" to "50",
                    "iso2Code" to "en",
                    "languageIso2Code" to "en",
                    "search" to partial
                )
            )
        }.getOrNull() ?: return@withContext emptyList()

        val values = response.content.mapNotNull { extract(it)?.takeIf(String::isNotBlank) }.distinct()
        FuzzyMatcher.filterSorted(partial, values, threshold = 20) { it }.take(12)
    }

    // ---- SITE (unofficial, no key, live) --------------------------------------------------

    private suspend fun searchSite(
        filters: DeviceSearchFilters,
        page: Int,
        pageSize: Int
    ): DeviceSearchResult {
        val params = buildSiteParams(filters, page, pageSize)
        val response = siteApi.searchDevices(params)

        var devices = response.content.map { it.toDevice() }
        devices = applyFuzzyAndDateFilters(devices, filters)

        return DeviceSearchResult(
            devices = devices,
            page = response.number,
            totalPages = response.totalPages.coerceAtLeast(1),
            totalElements = response.totalElements
        )
    }

    private fun buildSiteParams(filters: DeviceSearchFilters, page: Int, pageSize: Int): Map<String, String> {
        val params = mutableMapOf(
            "page" to page.toString(),
            "pageSize" to pageSize.toString(),
            "size" to pageSize.toString(),
            "iso2Code" to "en",
            "languageIso2Code" to "en"
        )
        // Free-text search: EUDAMED's own search box sends the whole phrase under a single
        // "search" parameter that matches across trade name / device name / reference.
        val freeText = listOfNotNull(
            filters.productName.takeIf { it.isNotBlank() },
            filters.udi.takeIf { it.isNotBlank() }
        ).joinToString(" ")
        if (freeText.isNotBlank()) params["search"] = freeText

        if (filters.manufacturerName.isNotBlank()) params["manufacturerName"] = filters.manufacturerName
        filters.riskClassCode?.let { params["riskClass"] = it }
        filters.deviceStatusCode?.let { params["deviceStatusType"] = it }
        filters.legislationCode?.let { params["applicableLegislation"] = it }
        filters.countryCode?.let { params["manufacturerCountry"] = it }
        return params
    }

    // ---- OFFICIAL (documented, keyed, nightly snapshot, exact match) ---------------------

    private suspend fun searchOfficial(
        filters: DeviceSearchFilters,
        page: Int,
        pageSize: Int,
        apiKey: String
    ): DeviceSearchResult {
        if (apiKey.isBlank()) {
            return DeviceSearchResult(devices = emptyList(), page = 0, totalPages = 1, totalElements = 0)
        }
        val params = mutableMapOf(
            "page" to page.toString(),
            "pageSize" to pageSize.toString()
        )
        if (filters.productName.isNotBlank()) params["tradeName"] = filters.productName
        if (filters.manufacturerName.isNotBlank()) params["manufacturerName"] = filters.manufacturerName
        if (filters.udi.isNotBlank()) params["primaryDi"] = filters.udi
        filters.riskClassCode?.let { params["riskClass"] = it }
        filters.deviceStatusCode?.let { params["deviceStatus"] = it }
        filters.legislationCode?.let { params["applicableLegislation"] = it }
        filters.countryCode?.let { params["manufacturerCountry"] = it }

        val response = officialApi.searchDevices(apiKey, params)
        var devices = response.results.map { it.toDevice() }
        // The official API only does exact matching server-side, so the "find similar" behaviour
        // the user asked for is applied here client-side on top of whatever exact/broad results
        // came back, exactly like the SITE path.
        devices = applyFuzzyAndDateFilters(devices, filters)

        val totalPages = if (response.pageSize > 0) {
            ((response.totalCount + response.pageSize - 1) / response.pageSize).coerceAtLeast(1)
        } else 1

        return DeviceSearchResult(
            devices = devices,
            page = response.page,
            totalPages = totalPages,
            totalElements = response.totalCount
        )
    }

    // ---- shared client-side fuzzy + date filtering ----------------------------------------

    private fun applyFuzzyAndDateFilters(devices: List<Device>, filters: DeviceSearchFilters): List<Device> {
        var result = devices

        if (filters.productName.isNotBlank()) {
            result = FuzzyMatcher.filterSorted(filters.productName, result, threshold = 30) { it.tradeName }
        }
        if (filters.manufacturerName.isNotBlank()) {
            result = FuzzyMatcher.filterSorted(filters.manufacturerName, result, threshold = 30) { it.manufacturerName }
        }
        if (filters.countryText.isNotBlank() && filters.countryCode == null) {
            result = FuzzyMatcher.filterSorted(filters.countryText, result, threshold = 30) { it.manufacturerCountry.orEmpty() }
        }
        if (filters.udi.isNotBlank()) {
            result = result.filter {
                FuzzyMatcher.matches(filters.udi, it.primaryDi.orEmpty(), threshold = 40) ||
                    FuzzyMatcher.matches(filters.udi, it.basicUdiDiId.orEmpty(), threshold = 40)
            }
        }
        if (filters.registrationDateFrom != null || filters.registrationDateTo != null) {
            result = result.filter {
                FuzzyDate.inRange(it.lastUpdateDate, filters.registrationDateFrom, filters.registrationDateTo)
            }
        }
        // Note: a device's CE / certificate date lives on its certificate list, which the search
        // endpoint doesn't return — it's only available from the per-device detail call. When a CE
        // date range is set, callers should prefer opening device detail to verify; here we keep
        // devices whose last-update date is compatible as a first-pass filter rather than dropping
        // everything (a device with an undated summary is never silently excluded).
        if (filters.ceDateFrom != null || filters.ceDateTo != null) {
            result = result.filter {
                FuzzyDate.inRange(it.lastUpdateDate, filters.ceDateFrom, filters.ceDateTo)
            }
        }

        return result
    }
}

private fun DeviceSummary.toDevice(): Device = Device(
    id = uuid.orEmpty(),
    basicUdiDiId = basicUdiDiDataUlid,
    primaryDi = primaryDi,
    tradeName = tradeName?.takeIf { it.isNotBlank() } ?: deviceModel.orEmpty(),
    manufacturerName = manufacturerName.orEmpty(),
    manufacturerSrn = manufacturerSrn,
    manufacturerCountry = manufacturerCountry?.name,
    riskClassLabel = riskClass?.humanize(),
    riskClassCode = riskClass?.code,
    deviceStatusLabel = deviceStatusType?.humanize(),
    legislationLabel = applicableLegislation?.humanize(),
    lastUpdateDate = FuzzyDate.parseApiDate(lastUpdateDate),
    source = DataSource.SITE
)

private fun OfficialDeviceRecord.toDevice(): Device = Device(
    id = primaryDi ?: basicUdiDi.orEmpty(),
    basicUdiDiId = basicUdiDi,
    primaryDi = primaryDi,
    tradeName = tradeName?.takeIf { it.isNotBlank() } ?: deviceName.orEmpty(),
    manufacturerName = manufacturerName.orEmpty(),
    manufacturerSrn = manufacturerSrn,
    manufacturerCountry = manufacturerCountry,
    riskClassLabel = riskClass,
    riskClassCode = riskClass,
    deviceStatusLabel = deviceStatus,
    legislationLabel = applicableLegislation,
    lastUpdateDate = FuzzyDate.parseApiDate(lastUpdateDate),
    source = DataSource.OFFICIAL_API
)
