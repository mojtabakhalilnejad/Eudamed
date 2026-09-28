package com.openregulatory.eudamedsearch.data.model

import kotlinx.serialization.Serializable

/**
 * Response of GET /devices/udiDiData (the device *search/list* endpoint).
 * Field names and shape come from the reverse-engineered EUDAMED API docs published by
 * OpenRegulatory: https://openregulatory.github.io/eudamed-api/
 */
@Serializable
data class DeviceSearchResponse(
    val content: List<DeviceSummary> = emptyList(),
    val first: Boolean = true,
    val last: Boolean = true,
    val number: Int = 0,
    val numberOfElements: Int = 0,
    val size: Int = 0,
    val totalElements: Int = 0,
    val totalPages: Int = 0
)

@Serializable
data class DeviceSummary(
    val uuid: String? = null,
    val ulid: String? = null,
    val basicUdi: String? = null,
    val primaryDi: String? = null,
    val basicUdiDiDataUlid: String? = null,
    val riskClass: CodeLabel? = null,
    val tradeName: String? = null,
    val manufacturerName: String? = null,
    val manufacturerSrn: String? = null,
    val deviceStatusType: CodeLabel? = null,
    val manufacturerStatus: CodeLabel? = null,
    val latestVersion: Boolean? = null,
    val versionNumber: Int? = null,
    val reference: String? = null,
    val authorisedRepresentativeSrn: String? = null,
    val authorisedRepresentativeName: String? = null,

    // Extra fields exposed by the live search endpoint beyond the documented sample;
    // kept optional so parsing never breaks if EUDAMED adds/removes a field.
    val deviceModel: String? = null,
    val issuingAgency: CodeLabel? = null,
    val applicableLegislation: CodeLabel? = null,
    val manufacturerCountry: Country? = null,
    val lastUpdateDate: String? = null
)

/** Response of GET /devices/udiDiData/{deviceId} — a single device *version*, much more detailed. */
@Serializable
data class DeviceDetailResponse(
    val uuid: String? = null,
    val ulid: String? = null,
    val reference: String? = null,
    val deviceName: String? = null,
    val deviceModel: String? = null,
    val tradeName: LocalizedTextContainer? = null,
    val legislation: CodeLabel? = null,
    val riskClass: CodeLabel? = null,
    val specialDeviceType: CodeLabel? = null,
    val placedOnTheMarket: Country? = null,
    val sterile: Boolean? = null,
    val singleUse: Boolean? = null,
    val implantable: Boolean? = null,
    val reusable: Boolean? = null,
    val versionDate: String? = null,
    val versionNumber: Int? = null,
    val latestVersion: Boolean? = null,
    val deviceStatus: DeviceStatus? = null,
    val basicUdi: BasicUdiRef? = null,
    val deviceCertificateInfoList: List<DeviceCertificateInfo>? = null
)

@Serializable
data class DeviceStatus(
    val uuid: String? = null,
    val type: CodeLabel? = null,
    val statusDate: String? = null
)

@Serializable
data class BasicUdiRef(
    val uuid: String? = null,
    val code: String? = null,
    val issuingAgency: CodeLabel? = null,
    val type: String? = null
)

@Serializable
data class DeviceCertificateInfo(
    val uuid: String? = null,
    val certificateNumber: String? = null,
    val certificateRevision: String? = null,
    val certificateExpiry: String? = null,
    val issueDate: String? = null,
    val startingValidityDate: String? = null,
    val certificateType: CodeLabel? = null,
    val status: CodeLabel? = null,
    val notifiedBody: NotifiedBody? = null
)

@Serializable
data class NotifiedBody(
    val uuid: String? = null,
    val name: String? = null,
    val srn: String? = null,
    val countryName: String? = null
)

/** Response of GET /devices/basicUdiData/{basicUdiDiId} — data spanning all versions of a device,
 *  including the full manufacturer (actor) record and the certificate list. */
@Serializable
data class BasicUdiDetailResponse(
    val uuid: String? = null,
    val deviceName: String? = null,
    val deviceModel: String? = null,
    val legislation: CodeLabel? = null,
    val riskClass: CodeLabel? = null,
    val manufacturer: ManufacturerWrapper? = null,
    val deviceCertificateInfoList: List<DeviceCertificateInfo>? = null,
    val versionDate: String? = null
)

@Serializable
data class ManufacturerWrapper(
    val actorDataPublicView: ActorPublicView? = null
)

/** Response of GET /actors/{actorId}/publicInformation, and the nested shape reused above. */
@Serializable
data class ActorPublicView(
    val ulid: String? = null,
    val uuid: String? = null,
    val type: CodeLabel? = null,
    val actorStatus: CodeLabel? = null,
    val country: Country? = null,
    val name: LocalizedTextContainer? = null,
    val eudamedIdentifier: String? = null,
    val telephone: String? = null,
    val electronicMail: String? = null,
    val website: String? = null,
    val actorAddress: ActorAddress? = null
)

@Serializable
data class ActorAddress(
    val streetName: String? = null,
    val buildingNumber: String? = null,
    val cityName: String? = null,
    val postalZone: String? = null,
    val country: Country? = null
)
