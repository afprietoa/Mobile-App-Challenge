package com.unal.reto6

import android.content.Context
import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // Marcadores y dificultad persisten en el dispositivo: se borran antes de lanzar la Activity
    // para que ningún test dependa del anterior.
    private val clearPrefsRule = object : ExternalResource() {
        override fun before() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit()
        }
    }
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(clearPrefsRule).around(activityRule)

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
    fun recreate_keepsHumanTurnMessageAfterComputerMoved() {
        onView(withId(R.id.board)).perform(tapCell(1, 1))
        waitForComputerMove()

        activityRule.scenario.recreate()

        onView(withText(R.string.turn_human)).check(matches(isDisplayed()))
    }

    // Extra 2: rotar antes de que mueva el computador no debe dejarlo "colgado".
    @Test
    fun recreateDuringComputerTurn_computerStillMoves() {
        onView(withId(R.id.board)).perform(tapCell(0, 0))

        activityRule.scenario.recreate()

        onView(withText(R.string.turn_computer)).check(matches(isDisplayed()))
        waitForComputerMove()
        onView(withText(R.string.turn_human)).check(matches(isDisplayed()))
    }

    @Test
    fun recreate_doesNotMakeComputerMoveTwiceOnHumanTurn() {
        onView(withId(R.id.board)).perform(tapCell(0, 0))
        waitForComputerMove()

        activityRule.scenario.recreate()
        waitForComputerMove() // si hubiera una jugada extra, ya habría ocurrido

        onView(withText(R.string.turn_human)).check(matches(isDisplayed()))
    }

    @Test
    fun resetScores_showsZerosAndSurvivesRecreate() {
        onView(withText("Humano: 0")).check(matches(isDisplayed()))

        openOverflowMenu()
        onView(withText(R.string.action_reset_scores)).perform(click())

        activityRule.scenario.recreate()
        onView(withText("Humano: 0")).check(matches(isDisplayed()))
        onView(withText("Empates: 0")).check(matches(isDisplayed()))
        onView(withText("Android: 0")).check(matches(isDisplayed()))
    }

    @Test
    fun difficulty_survivesRecreate() {
        openOverflowMenu()
        onView(withText(R.string.action_difficulty)).perform(click())
        onView(withText(R.string.difficulty_easy)).perform(click())

        activityRule.scenario.recreate()

        openOverflowMenu()
        onView(withText(R.string.action_difficulty)).perform(click())
        onView(withText(R.string.difficulty_easy)).check(matches(isChecked()))
        onView(withText(R.string.difficulty_easy)).perform(click())
    }

    @Test
    fun menu_hasResetScoresAndNoQuit() {
        openOverflowMenu()

        onView(withText(R.string.action_reset_scores)).check(matches(isDisplayed()))
        onView(withText("Salir")).check(doesNotExist())
    }

    private companion object {
        const val COMPUTER_MOVE_WAIT_MS = 1500L
    }
}
