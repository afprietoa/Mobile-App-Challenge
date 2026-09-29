package com.unal.reto5

import android.view.InputDevice
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

    // Toca el centro de la celda (row, col) del BoardView.
    private fun tapCell(row: Int, col: Int): ViewAction = GeneralClickAction(
        Tap.SINGLE,
        CoordinatesProvider { view ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            floatArrayOf(
                location[0] + view.width / 3f * (col + 0.5f),
                location[1] + view.height / 3f * (row + 0.5f)
            )
        },
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )

    // El computador espera 1 s (Handler.postDelayed); un margen holgado evita tests inestables.
    private fun waitForComputerMove() = Thread.sleep(COMPUTER_MOVE_WAIT_MS)

    @Test
    fun tappingBoardCell_announcesComputerTurn() {
        onView(withId(R.id.board)).perform(tapCell(0, 0))

        onView(withText(R.string.turn_computer)).check(matches(isDisplayed()))
    }

    @Test
    fun computerAnswersAfterDelay_andHumanTurnReturns() {
        onView(withId(R.id.board)).perform(tapCell(1, 1))
        waitForComputerMove()

        onView(withText(R.string.turn_human)).check(matches(isDisplayed()))
    }

    @Test
    fun tapDuringComputerTurn_isIgnored() {
        onView(withId(R.id.board)).perform(tapCell(0, 0))
        // Segundo toque inmediato: el computador aún "piensa", no debe cambiar nada.
        onView(withId(R.id.board)).perform(tapCell(2, 2))

        onView(withText(R.string.turn_computer)).check(matches(isDisplayed()))
        waitForComputerMove()
        onView(withText(R.string.turn_human)).check(matches(isDisplayed()))
    }

    @Test
    fun newGameMenuItem_resetsTurnMessage() {
        onView(withId(R.id.board)).perform(tapCell(0, 0))

        openOverflowMenu()
        onView(withText(R.string.action_new_game)).perform(click())

        // La primera partida la empezó el humano, la nueva la empieza el computador (turnos alternos).
        onView(withText(R.string.turn_computer)).check(matches(isDisplayed()))
    }

    @Test
    fun soundMenuItem_canBeToggled() {
        openOverflowMenu()
        onView(withText(R.string.action_sound)).perform(click())

        openOverflowMenu()
        onView(withText(R.string.action_sound)).check(matches(isDisplayed()))
        onView(withText(R.string.action_sound)).perform(click())
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

    private companion object {
        const val COMPUTER_MOVE_WAIT_MS = 1500L
    }
}
