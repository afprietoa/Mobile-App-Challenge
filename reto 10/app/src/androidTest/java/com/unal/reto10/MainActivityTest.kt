package com.unal.reto10

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.reto10.data.FakeCollectionPointRepository
import com.unal.reto10.data.ServiceLocator
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.ui.MainActivity
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    /** La red corre en un hilo propio (sin IdlingResource): se reintenta hasta 5 s. */
    private fun eventually(block: () -> Unit) {
        val end = System.currentTimeMillis() + 5_000
        while (true) {
            try { block(); return } catch (e: Throwable) {
                if (System.currentTimeMillis() > end) throw e
                Thread.sleep(100)
            }
        }
    }

    @After fun tearDown() { ServiceLocator.repository = null }

    @Test fun showsResultsAndCounter() {
        ServiceLocator.repository = FakeCollectionPointRepository(FakeCollectionPointRepository.sample(3))
        ActivityScenario.launch(MainActivity::class.java).use {
            eventually { onView(withText("Punto 01")).check(matches(isDisplayed())) }
            eventually { onView(withId(R.id.counter)).check(matches(withText("Mostrando 3 de 3 resultados"))) }
        }
    }

    @Test fun emptyStateIsShown() {
        ServiceLocator.repository = FakeCollectionPointRepository(emptyList())
        ActivityScenario.launch(MainActivity::class.java).use {
            eventually { onView(withText("Sin resultados para estos filtros")).check(matches(isDisplayed())) }
        }
    }

    @Test fun errorShowsMessageAndRetryRecovers() {
        val repo = FakeCollectionPointRepository(FakeCollectionPointRepository.sample(2), error = ErrorKind.NO_NETWORK)
        ServiceLocator.repository = repo
        ActivityScenario.launch(MainActivity::class.java).use {
            eventually { onView(withText(R.string.error_no_network)).check(matches(isDisplayed())) }
            repo.error = null
            onView(withId(R.id.retryButton)).perform(click())
            eventually { onView(withText("Punto 01")).check(matches(isDisplayed())) }
        }
    }

    @Test fun clickingAnItemOpensDetail() {
        ServiceLocator.repository = FakeCollectionPointRepository(FakeCollectionPointRepository.sample(2))
        ActivityScenario.launch(MainActivity::class.java).use {
            eventually { onView(withText("Punto 01")).check(matches(isDisplayed())) }
            onView(withText("Punto 01")).perform(click())
            onView(withId(R.id.directionsButton)).check(matches(isDisplayed()))
        }
    }

    @Test fun rotationDoesNotRepeatTheRequest() {
        val repo = FakeCollectionPointRepository(FakeCollectionPointRepository.sample(3))
        ServiceLocator.repository = repo
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            eventually { onView(withText("Punto 01")).check(matches(isDisplayed())) }
            val before = repo.searchCalls
            scenario.recreate()
            eventually { onView(withText("Punto 01")).check(matches(isDisplayed())) }
            assert(repo.searchCalls == before)
        }
    }
}
