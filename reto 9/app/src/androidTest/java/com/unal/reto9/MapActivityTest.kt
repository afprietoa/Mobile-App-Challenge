package com.unal.reto9

import android.Manifest
import android.location.Location
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.unal.reto9.data.PoiException
import com.unal.reto9.data.PoiRepository
import com.unal.reto9.data.ServiceLocator
import com.unal.reto9.location.LocationSource
import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest
import com.unal.reto9.ui.MapActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapActivityTest {

    private class FixedLocation : LocationSource {
        override fun current(onResult: (Location?) -> Unit) {
            onResult(Location("fake").apply { latitude = 4.6097; longitude = -74.0817 })
        }

        override fun stop() = Unit
    }

    private class FakeRepository(private val result: () -> List<PointOfInterest>) : PoiRepository {
        override fun search(lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>) = result()
    }

    private fun poi(id: Long) =
        PointOfInterest(id, "Lugar $id", PoiCategory.HOSPITAL, 4.61 + id / 1000.0, -74.08, id * 100.0)

    @Before
    fun setUp() {
        val ui = InstrumentationRegistry.getInstrumentation().uiAutomation
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        ui.grantRuntimePermission(pkg, Manifest.permission.ACCESS_FINE_LOCATION)
        ui.grantRuntimePermission(pkg, Manifest.permission.ACCESS_COARSE_LOCATION)
        ServiceLocator.locationSourceFactory = { FixedLocation() }
    }

    @After
    fun tearDown() = ServiceLocator.reset()

    /** El Executor de la Activity no es visible para Espresso: se reintenta la aserción. */
    private fun eventually(assertion: () -> Unit) {
        val deadline = System.currentTimeMillis() + 5_000
        while (true) {
            try { return assertion() } catch (e: Throwable) {
                if (System.currentTimeMillis() > deadline) throw e
                Thread.sleep(100)
            }
        }
    }

    @Test
    fun showsCountOfReturnedPois() {
        ServiceLocator.poiRepository = FakeRepository { listOf(poi(1), poi(2), poi(3)) }
        ActivityScenario.launch(MapActivity::class.java).use {
            eventually { onView(withId(R.id.counter)).check(matches(withText(org.hamcrest.Matchers.startsWith("3 lugares")))) }
        }
    }

    @Test
    fun showsErrorMessageWhenThereIsNoNetwork() {
        ServiceLocator.poiRepository = FakeRepository { throw PoiException(PoiException.Kind.NO_NETWORK) }
        ActivityScenario.launch(MapActivity::class.java).use {
            eventually { onView(withText(R.string.error_no_network)).check(matches(isDisplayed())) }
        }
    }
}
