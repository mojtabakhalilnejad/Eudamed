package com.openregulatory.eudamedsearch.data.repository

import android.content.Context
import android.content.SharedPreferences

/** Tiny wrapper around SharedPreferences for the one piece of local state the app has: the
 *  optional subscription key for the official DG SANTE API. Excluded from backup, see
 *  res/xml/backup_rules.xml and data_extraction_rules.xml. */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("eudamed_prefs", Context.MODE_PRIVATE)

    var officialApiKey: String
        get() = prefs.getString(KEY_OFFICIAL_API_KEY, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_OFFICIAL_API_KEY, value).apply()

    companion object {
        private const val KEY_OFFICIAL_API_KEY = "official_api_key"
    }
}
