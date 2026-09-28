package com.openregulatory.eudamedsearch.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

/**
 * Client for the **official, documented** "EUDAMED Public API v1.0", published by the European
 * Commission (DG SANTE) on the EU data-lake developer portal:
 * https://developer.datalake.sante.service.ec.europa.eu/api-details#api=94b9e658-d721-4b58-8d96-022c490f7a17
 *
 * Unlike [EudamedSiteApi], this one:
 *  - requires a free subscription key from the developer portal, sent as the
 *    `Ocp-Apim-Subscription-Key` header;
 *  - only supports EXACT-match filters (no partial/fuzzy search server-side — the "similar" search
 *    the app offers is done locally, see [com.openregulatory.eudamedsearch.util.FuzzyMatcher]);
 *  - serves a nightly snapshot of the UDI/device and actor registers rather than the live database.
 *
 * The exact resource path and query-parameter names are set by the Commission's own OpenAPI
 * definition on that portal (it requires a login to view), so [BASE_PATH] and the parameter map
 * built in the repository are our best-effort mapping and may need a one-line adjustment once you
 * have portal access — everything else in the app (UI, fuzzy matching, combo boxes) stays the same.
 */
interface EudamedOfficialApi {

    @GET("api/v1/udi-devices")
    suspend fun searchDevices(
        @Header("Ocp-Apim-Subscription-Key") subscriptionKey: String,
        @QueryMap params: Map<String, String>
    ): OfficialDeviceSearchResponse

    companion object {
        const val BASE_PATH = "api/v1/"
    }
}

@Serializable
data class OfficialDeviceSearchResponse(
    val results: List<OfficialDeviceRecord> = emptyList(),
    val totalCount: Int = 0,
    val page: Int = 0,
    val pageSize: Int = 0
)

@Serializable
data class OfficialDeviceRecord(
    val basicUdiDi: String? = null,
    val primaryDi: String? = null,
    val deviceName: String? = null,
    val tradeName: String? = null,
    val manufacturerName: String? = null,
    val manufacturerSrn: String? = null,
    val manufacturerCountry: String? = null,
    val riskClass: String? = null,
    val applicableLegislation: String? = null,
    val deviceStatus: String? = null,
    val lastUpdateDate: String? = null
)
