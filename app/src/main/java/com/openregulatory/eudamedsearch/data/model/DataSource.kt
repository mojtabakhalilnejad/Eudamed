package com.openregulatory.eudamedsearch.data.model

enum class DataSource {
    /** The public, no-key JSON API behind the eudamed.ec.europa.eu website (live data). */
    SITE,

    /** The official, documented DG SANTE "EUDAMED Public API v1.0" (needs a subscription key,
     *  nightly snapshot, exact-match only). */
    OFFICIAL_API
}
