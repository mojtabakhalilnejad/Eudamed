package com.openregulatory.eudamedsearch.data.remote

import com.openregulatory.eudamedsearch.data.model.BasicUdiDetailResponse
import com.openregulatory.eudamedsearch.data.model.DeviceDetailResponse
import com.openregulatory.eudamedsearch.data.model.DeviceSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * Client for the *unofficial* JSON API behind the public EUDAMED website
 * (https://ec.europa.eu/tools/eudamed). This is the same endpoint the browser calls when a
 * person searches devices on eudamed.ec.europa.eu — it needs no API key and is publicly reachable,
 * but it is **not officially documented or guaranteed stable** by the European Commission.
 *
 * Endpoint shapes are taken from the community reverse-engineering effort at
 * https://openregulatory.github.io/eudamed-api/ . Only `page`, `pageSize`, `size`, `iso2Code`
 * and `languageIso2Code` are confirmed/documented there. The additional filter parameters below
 * (search text, risk class, device status, legislation, manufacturer, country, dates) mirror the
 * fields the EUDAMED web UI itself sends when a person fills in the on-screen search form; if the
 * Commission changes those parameter names, only [buildDeviceSearchParams] in
 * [com.openregulatory.eudamedsearch.data.repository.DeviceRepository] needs to be updated.
 *
 * Because server-side filtering of this endpoint is not guaranteed to be exact, the app *also*
 * performs its own fuzzy/"contains similar" re-filtering of whatever page(s) come back
 * (see [com.openregulatory.eudamedsearch.util.FuzzyMatcher]), so the search stays useful even if a
 * given filter parameter is silently ignored by the server.
 */
interface EudamedSiteApi {

    @GET("devices/udiDiData")
    suspend fun searchDevices(@QueryMap params: Map<String, String>): DeviceSearchResponse

    @GET("devices/udiDiData/{deviceId}")
    suspend fun getDeviceDetail(
        @Path("deviceId") deviceId: String,
        @QueryMap params: Map<String, String>
    ): DeviceDetailResponse

    @GET("devices/basicUdiData/{basicUdiDiId}")
    suspend fun getBasicUdiDetail(
        @Path("basicUdiDiId") basicUdiDiId: String,
        @QueryMap params: Map<String, String>
    ): BasicUdiDetailResponse
}
