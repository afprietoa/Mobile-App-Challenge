package com.example.helloworld

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun appContext_hasExpectedPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.helloworld", appContext.packageName)
    }

    @Test
    fun tappingScreen_changesGreetingToNextLanguage() {
        ActivityScenario.launch(MainActivity::class.java).use {
            Thread.sleep(600)
            onView(withId(R.id.languageLabel)).check(matches(withText(Greetings.all[0].languageLabel)))

            onView(withId(R.id.main)).perform(click())
            Thread.sleep(600)

            onView(withId(R.id.languageLabel)).check(matches(withText(Greetings.all[1].languageLabel)))
        }
    }
}
