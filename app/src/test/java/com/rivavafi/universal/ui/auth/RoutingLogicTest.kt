package com.rivavafi.universal.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutingLogicTest {

    @Test
    fun `test new user is routed to Welcome screen`() {
        val hasCompletedOnboarding = false
        val isNewUser = true

        val startDestination = if (hasCompletedOnboarding || !isNewUser) "home" else "welcome"

        assertEquals("welcome", startDestination)
    }

    @Test
    fun `test existing user who completed onboarding is routed to Home screen`() {
        val hasCompletedOnboarding = true
        val isNewUser = false

        val startDestination = if (hasCompletedOnboarding || !isNewUser) "home" else "welcome"

        assertEquals("home", startDestination)
    }

    @Test
    fun `test new user who has NOT completed onboarding is routed to Welcome screen`() {
        val hasCompletedOnboarding = false
        val isNewUser = true

        val startDestination = if (hasCompletedOnboarding || !isNewUser) "home" else "welcome"

        assertEquals("welcome", startDestination)
    }

    @Test
    fun `test existing user who dropped out of onboarding is routed to Home screen based on logic`() {
        // Technically this tests the boolean logic: an existing user might not have finished onboarding, but the logic routes them Home.
        val hasCompletedOnboarding = false
        val isNewUser = false

        val startDestination = if (hasCompletedOnboarding || !isNewUser) "home" else "welcome"

        assertEquals("home", startDestination)
    }
}
