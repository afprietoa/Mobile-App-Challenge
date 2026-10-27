package com.unal.reto9

import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.unal.reto9.data.SettingsStore
import com.unal.reto9.ui.SettingsActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearPrefs() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
    }

    @Test
    fun showsRadiusAndCategoriesPreferences() {
        ActivityScenario.launch(SettingsActivity::class.java).use {
            onView(withText(R.string.pref_radius_title)).check(matches(isDisplayed()))
            onView(withText(R.string.pref_categories_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun defaultRadiusIsThreeKm() = assertEquals(3, SettingsStore(context).radiusKm)

    @Test
    fun corruptRadiusIsClamped() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SettingsStore.KEY_RADIUS, 500).commit()
        assertEquals(20, SettingsStore(context).radiusKm)
    }
}
