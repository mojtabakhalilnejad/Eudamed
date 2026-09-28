package com.openregulatory.eudamedsearch.data.model

import java.time.LocalDate

/** UI-facing, flattened device record — built from either [DeviceSummary] (site API) or
 *  [OfficialDeviceRecord] (official API) so the rest of the app doesn't care which source it came from. */
data class Device(
    val id: String,
    val basicUdiDiId: String?,
    val primaryDi: String?,
    val tradeName: String,
    val manufacturerName: String,
    val manufacturerSrn: String?,
    val manufacturerCountry: String?,
    val riskClassLabel: String?,
    val riskClassCode: String?,
    val deviceStatusLabel: String?,
    val legislationLabel: String?,
    val lastUpdateDate: LocalDate?,
    val source: DataSource,
    /** The device's earliest CE-certificate date. The search endpoints never return this (only a
     *  device's own detail page does), so it starts out null and is filled in lazily — see
     *  [com.openregulatory.eudamedsearch.data.repository.DeviceRepository.fetchCeDate] — only when
     *  the person actually asks to sort by it, rather than on every search result. */
    val ceDate: LocalDate? = null
)

enum class SortOption {
    DEFAULT,
    PRODUCT_NAME_ASC,
    MANUFACTURER_ASC,
    RISK_CLASS,
    UPDATED_NEWEST,
    UPDATED_OLDEST,
    CE_NEWEST,
    CE_OLDEST;

    /** Whether this sort needs each device's CE date fetched first (see [Device.ceDate]). */
    val needsCeDate: Boolean get() = this == CE_NEWEST || this == CE_OLDEST
}

/** Search parameters gathered from the combo boxes / text fields on the search screen. All are
 *  optional; the ones that are combo boxes carry both a free-typed [text] (used for fuzzy matching)
 *  and, when the user picked a suggestion, the resolved [code] to send to the server. */
data class DeviceSearchFilters(
    val productName: String = "",
    val manufacturerName: String = "",
    val countryText: String = "",
    val countryCode: String? = null,
    val riskClassText: String = "",
    val riskClassCode: String? = null,
    val deviceStatusText: String = "",
    val deviceStatusCode: String? = null,
    val legislationText: String = "",
    val legislationCode: String? = null,
    val udi: String = "",
    val registrationDateFrom: LocalDate? = null,
    val registrationDateTo: LocalDate? = null,
    val ceDateFrom: LocalDate? = null,
    val ceDateTo: LocalDate? = null
) {
    fun isEmpty(): Boolean = this == DeviceSearchFilters()
}

data class DeviceSearchResult(
    val devices: List<Device>,
    val page: Int,
    val totalPages: Int,
    val totalElements: Int
) {
    val hasMore: Boolean get() = page + 1 < totalPages
}
