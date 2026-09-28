package com.openregulatory.eudamedsearch.data.model

import kotlinx.serialization.Serializable

/**
 * EUDAMED represents almost every "enum-like" value (risk class, device status, legislation,
 * actor status, ...) as a small object carrying a reference-data code such as
 * "refdata.risk-class.class-i" instead of a plain string. We model that shape once and reuse it.
 */
@Serializable
data class CodeLabel(
    val code: String? = null,
    val srnCode: String? = null,
    val category: String? = null
) {
    /** Turns "refdata.risk-class.class-iii" into "class iii", used as a fallback display label. */
    fun humanize(): String =
        code?.substringAfterLast('.')?.replace('-', ' ')?.replaceFirstChar { it.uppercase() } ?: ""
}

@Serializable
data class LocalizedText(
    val text: String? = null,
    val allLanguagesApplicable: Boolean? = null
)

@Serializable
data class LocalizedTextContainer(
    val texts: List<LocalizedText>? = null
) {
    fun firstOrNull(): String? = texts?.firstOrNull { !it.text.isNullOrBlank() }?.text
}

@Serializable
data class Country(
    val name: String? = null,
    val type: String? = null,
    val iso2Code: String? = null,
    val nonEUMemberState: Boolean? = null
)
