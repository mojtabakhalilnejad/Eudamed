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
    val source: DataSource
)

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
