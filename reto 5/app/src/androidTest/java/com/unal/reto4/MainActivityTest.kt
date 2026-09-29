package com.unal.reto4

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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

    private fun openOverflowMenu() {
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

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

        openOverflowMenu()
        onView(withText(R.string.action_new_game)).perform(click())

        onView(withId(R.id.button_0)).check(matches(withText("")))
    }

    @Test
    fun difficultyMenuItem_showsLevelOptions() {
        openOverflowMenu()
        onView(withText(R.string.action_difficulty)).perform(click())

        onView(withText(R.string.difficulty_easy)).check(matches(isDisplayed()))
        onView(withText(R.string.difficulty_harder)).check(matches(isDisplayed()))
        onView(withText(R.string.difficulty_expert)).check(matches(isDisplayed()))

        onView(withText(R.string.difficulty_easy)).perform(click())
    }

    @Test
    fun aboutMenuItem_showsAboutDialog() {
        openOverflowMenu()
        onView(withText(R.string.action_about)).perform(click())

        onView(withText(R.string.about_title)).check(matches(isDisplayed()))

        onView(withText(R.string.ok)).perform(click())
    }

    @Test
    fun quitMenuItem_confirmingYes_finishesActivity() {
        openOverflowMenu()
        onView(withText(R.string.action_quit)).perform(click())

        onView(withText(R.string.yes)).perform(click())

        activityRule.scenario.moveToState(Lifecycle.State.RESUMED) // no-op si ya terminó
        assert(activityRule.scenario.state == Lifecycle.State.DESTROYED)
    }
}
