package com.example

import com.example.util.UrlPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlPolicyTest {

    @Test
    fun `internal phoenix urls are recognized across http and https`() {
        assertTrue(UrlPolicy.isInternalPhoenixUrl("http://phoenix.runasp.net"))
        assertTrue(UrlPolicy.isInternalPhoenixUrl("https://phoenix.runasp.net/dashboard"))
        assertTrue(UrlPolicy.isInternalPhoenixUrl("http://sub.phoenix.runasp.net/login"))
        assertFalse(UrlPolicy.isInternalPhoenixUrl("https://google.com"))
        assertFalse(UrlPolicy.isInternalPhoenixUrl("https://evilphoenix.runasp.net"))
    }

    @Test
    fun `external action schemes are detected`() {
        assertTrue(UrlPolicy.isExternalActionScheme("tel:+15551234567"))
        assertTrue(UrlPolicy.isExternalActionScheme("mailto:support@phoenix.runasp.net"))
        assertTrue(UrlPolicy.isExternalActionScheme("intent://scan/#Intent;end"))
        assertFalse(UrlPolicy.isExternalActionScheme("http://phoenix.runasp.net"))
    }

    @Test
    fun `formatDisplayLocation formats host and optional path`() {
        assertEquals("phoenix.runasp.net", UrlPolicy.formatDisplayLocation("http://phoenix.runasp.net/"))
        assertEquals(
            "phoenix.runasp.net/account/profile",
            UrlPolicy.formatDisplayLocation("http://phoenix.runasp.net/account/profile?id=4")
        )
    }
}
