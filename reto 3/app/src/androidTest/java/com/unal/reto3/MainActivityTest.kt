package com.unal.reto3

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun tappingBoardButton_showsXAndDisablesButton() {
        onView(withId(R.id.button_0)).perform(click())

        onView(withId(R.id.button_0))
            .check(matches(withText("X")))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun newGameMenuItem_clearsBoard() {
        onView(withId(R.id.button_0)).perform(click())

        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(withText(R.string.action_new_game)).perform(click())

        onView(withId(R.id.button_0)).check(matches(withText("")))
    }
}
