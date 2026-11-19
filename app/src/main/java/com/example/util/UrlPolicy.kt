package com.example.util

import java.net.URI
import java.util.Locale

object UrlPolicy {
    const val BASE_URL = "http://phoenix.runasp.net"
    const val PRIMARY_HOST = "phoenix.runasp.net"

    private val externalActionSchemes = setOf(
        "tel",
        "mailto",
        "sms",
        "smsto",
        "geo",
        "intent",
        "market",
        "whatsapp",
        "tg"
    )

    fun isWebScheme(url: String): Boolean {
        val scheme = extractScheme(url) ?: return false
        return scheme == "http" || scheme == "https"
    }

    fun isExternalActionScheme(url: String): Boolean {
        val scheme = extractScheme(url) ?: return false
        return scheme in externalActionSchemes || !isWebScheme(url)
    }

    fun isInternalPhoenixUrl(url: String): Boolean {
        if (!isWebScheme(url)) return false
        val host = extractHost(url) ?: return false
        return host == PRIMARY_HOST || host.endsWith(".$PRIMARY_HOST")
    }

    fun formatDisplayLocation(url: String): String {
        val host = extractHost(url) ?: return PRIMARY_HOST
        val path = try {
            URI(url).rawPath?.takeIf { it.isNotBlank() && it != "/" }.orEmpty()
        } catch (_: Exception) {
            ""
        }
        return if (path.isEmpty()) host else "$host$path"
    }

    private fun extractScheme(url: String): String? {
        return try {
            URI(url).scheme?.lowercase(Locale.ROOT)
        } catch (_: Exception) {
            url.substringBefore(':', missingDelimiterValue = "")
                .takeIf { it.isNotEmpty() }
                ?.lowercase(Locale.ROOT)
        }
    }

    private fun extractHost(url: String): String? {
        return try {
            URI(url).host?.lowercase(Locale.ROOT)
        } catch (_: Exception) {
            null
        }
    }
}
