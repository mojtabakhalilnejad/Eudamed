package com.openregulatory.eudamedsearch.data.model

/** A single option inside one of the search combo boxes: what the user sees, and what code (if any)
 *  gets sent to the API. [code] is null for free-text-derived options (product name, manufacturer, UDI). */
data class ComboOption(
    val label: String,
    val code: String? = null
)

/**
 * Static EUDAMED reference-data lists used to populate the combo boxes that don't need a network
 * round-trip (risk class, device status, legislation, country). Codes follow the `refdata.*` scheme
 * EUDAMED itself uses (see [CodeLabel]), taken from the MDR/IVDR reference-data tables published by
 * the Commission (Annex to Implementing Regulation 2021/2078) and the OpenRegulatory API notes.
 */
object ReferenceData {

    val riskClasses = listOf(
        ComboOption("کلاس I", "refdata.risk-class.class-i"),
        ComboOption("کلاس I - استریل", "refdata.risk-class.class-i-sterile"),
        ComboOption("کلاس I - عملکرد اندازه‌گیری", "refdata.risk-class.class-i-measuring-function"),
        ComboOption("کلاس IIa", "refdata.risk-class.class-iia"),
        ComboOption("کلاس IIb", "refdata.risk-class.class-iib"),
        ComboOption("کلاس III", "refdata.risk-class.class-iii"),
        ComboOption("کلاس A (IVD)", "refdata.risk-class.class-a"),
        ComboOption("کلاس B (IVD)", "refdata.risk-class.class-b"),
        ComboOption("کلاس C (IVD)", "refdata.risk-class.class-c"),
        ComboOption("کلاس D (IVD)", "refdata.risk-class.class-d")
    )

    val deviceStatuses = listOf(
        ComboOption("در بازار (On the market)", "refdata.device-model-status.on-the-market"),
        ComboOption("در بازار قرار نگرفته", "refdata.device-model-status.not-placed-on-the-market"),
        ComboOption("از رده خارج شده", "refdata.device-model-status.no-longer-placed-on-the-market")
    )

    val legislations = listOf(
        ComboOption("MDR - تجهیزات پزشکی (2017/745)", "refdata.applicable-legislation.mdr"),
        ComboOption("IVDR - تجهیزات تشخیص آزمایشگاهی (2017/746)", "refdata.applicable-legislation.ivdr"),
        ComboOption("MDD (قدیمی)", "refdata.applicable-legislation.mdd"),
        ComboOption("AIMDD (قدیمی)", "refdata.applicable-legislation.aimdd"),
        ComboOption("IVDD (قدیمی)", "refdata.applicable-legislation.ivdd")
    )

    /** EU/EEA + a few common non-EU manufacturer countries seen in EUDAMED actor records. */
    val countries = listOf(
        "اتریش" to "AT", "بلژیک" to "BE", "بلغارستان" to "BG", "کرواسی" to "HR",
        "قبرس" to "CY", "چک" to "CZ", "دانمارک" to "DK", "استونی" to "EE",
        "فنلاند" to "FI", "فرانسه" to "FR", "آلمان" to "DE", "یونان" to "GR",
        "مجارستان" to "HU", "ایرلند" to "IE", "ایتالیا" to "IT", "لتونی" to "LV",
        "لیتوانی" to "LT", "لوکزامبورگ" to "LU", "مالت" to "MT", "هلند" to "NL",
        "لهستان" to "PL", "پرتغال" to "PT", "رومانی" to "RO", "اسلواکی" to "SK",
        "اسلوونی" to "SI", "اسپانیا" to "ES", "سوئد" to "SE",
        "ایسلند" to "IS", "لیختن‌اشتاین" to "LI", "نروژ" to "NO",
        "سوئیس" to "CH", "بریتانیا" to "GB", "ترکیه" to "TR",
        "ایالات متحده آمریکا" to "US", "چین" to "CN", "ژاپن" to "JP",
        "کره جنوبی" to "KR", "کانادا" to "CA", "استرالیا" to "AU",
        "هند" to "IN", "برزیل" to "BR", "اسرائیل" to "IL"
    ).map { (name, iso) -> ComboOption("$name ($iso)", iso) }
}
