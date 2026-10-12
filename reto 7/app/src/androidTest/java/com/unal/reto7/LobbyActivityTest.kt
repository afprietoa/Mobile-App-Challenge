package com.unal.reto7

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.unal.reto7.online.LobbyActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Pruebas de humo del lobby. Solo comprueban la estructura de la pantalla: no dependen de
 * que haya juegos ni de que el inicio de sesión anónimo tenga éxito.
 */
@RunWith(AndroidJUnit4::class)
class LobbyActivityTest {

    // Con el nombre vacío el lobby abre un diálogo que taparía las vistas: se fija uno antes de lanzar.
    private val presetNameRule = object : ExternalResource() {
        override fun before() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE)
                .edit().putString("player_name", "Tester").commit()
        }
    }
    private val activityRule = ActivityScenarioRule(LobbyActivity::class.java)

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(presetNameRule).around(activityRule)

    @Test
    fun lobby_showsCreateButton() {
        onView(withId(R.id.button_create)).check(matches(isDisplayed()))
    }

    @Test
    fun lobby_showsVsAndroidButton() {
        onView(withText(R.string.lobby_vs_android)).check(matches(isDisplayed()))
    }

    @Test
    fun lobby_showsGamesListContainer() {
        onView(withId(R.id.games_list)).check(matches(isDisplayed()))
    }
}
