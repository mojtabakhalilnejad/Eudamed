package com.openregulatory.eudamedsearch.util

import kotlin.math.max
import kotlin.math.min

/**
 * Small, dependency-free fuzzy/"approximate" text matching used everywhere the app needs to find
 * "things similar to what the user typed": combo-box suggestions (product name, manufacturer,
 * country, ...) and post-filtering of API results so a slightly misspelled or partial query still
 * surfaces the right devices.
 *
 * The scoring combines three signals, each cheap to compute, which together behave well for short
 * product/company names typed on a phone:
 *  1. Exact / prefix / substring match — always ranked first (score 100/95/85).
 *  2. Token-based partial match — e.g. "phil hue" matches "Philips Hue Sensor" (score up to 80).
 *  3. Levenshtein-distance similarity — catches typos like "phillips" vs "philips" (score up to 70).
 */
object FuzzyMatcher {

    /** Normalizes text for comparison: lower-cased, diacritics-insensitive-ish, extra spaces collapsed. */
    fun normalize(text: String): String =
        text.trim().lowercase().replace(Regex("\\s+"), " ")

    /**
     * Returns a similarity score in `0..100` between [query] and [candidate]; 0 means "no match at
     * all" and callers typically drop anything below ~35 unless the candidate list is very short.
     */
    fun score(query: String, candidate: String): Int {
        val q = normalize(query)
        val c = normalize(candidate)
        if (q.isEmpty()) return 100
        if (c.isEmpty()) return 0

        if (c == q) return 100
        if (c.startsWith(q)) return 95
        if (c.contains(q)) return 85

        val qTokens = q.split(' ').filter { it.isNotBlank() }
        val cTokens = c.split(' ').filter { it.isNotBlank() }
        val tokenHits = qTokens.count { qt -> cTokens.any { it.startsWith(qt) || it.contains(qt) } }
        if (tokenHits > 0) {
            val tokenScore = (60 + 20 * tokenHits / max(qTokens.size, 1)).coerceAtMost(80)
            return tokenScore
        }

        val distance = levenshtein(q, c.take(q.length + 6))
        val longest = max(q.length, min(c.length, q.length + 6))
        val similarity = 1.0 - distance.toDouble() / longest.toDouble()
        return if (similarity >= 0.55) (similarity * 70).toInt() else 0
    }

    /** True when [candidate] should be considered "similar enough" to [query] to show/keep. */
    fun matches(query: String, candidate: String, threshold: Int = 35): Boolean =
        query.isBlank() || score(query, candidate) >= threshold

    /** Filters and ranks [items] by similarity of [selector] to [query], best matches first. */
    fun <T> filterSorted(query: String, items: List<T>, threshold: Int = 35, selector: (T) -> String): List<T> {
        if (query.isBlank()) return items
        return items
            .map { it to score(query, selector(it)) }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}
