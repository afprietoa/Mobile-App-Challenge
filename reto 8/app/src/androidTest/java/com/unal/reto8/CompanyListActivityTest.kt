package com.unal.reto8

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.reto8.data.CompanyDbHelper
import com.unal.reto8.data.SqliteCompanyRepository
import com.unal.reto8.model.Classification
import com.unal.reto8.model.Company
import com.unal.reto8.ui.CompanyListActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompanyListActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private var scenario: ActivityScenario<CompanyListActivity>? = null

    private val consulting = Classification.CONSULTING.bit
    private val custom = Classification.CUSTOM_DEVELOPMENT.bit

    @Before fun setUp() {
        context.deleteDatabase(CompanyDbHelper.DB_NAME)
    }

    @After fun tearDown() {
        scenario?.close()
        context.deleteDatabase(CompanyDbHelper.DB_NAME)
    }

    private fun seed(vararg companies: Company) {
        val helper = CompanyDbHelper(context)
        val repo = SqliteCompanyRepository(helper)
        companies.forEach { repo.insert(it) }
        helper.close()
    }

    private fun launch() {
        scenario = ActivityScenario.launch(CompanyListActivity::class.java)
    }

    /** La lista se carga en un Executor propio que Espresso no conoce: se reintenta hasta 3 s. */
    private fun eventually(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 3000
        while (true) {
            try {
                check(); return
            } catch (e: Throwable) {
                if (System.currentTimeMillis() > deadline) throw e
                Thread.sleep(100)
            }
        }
    }

    private fun shown(text: String) = eventually { onView(withText(text)).check(matches(isDisplayed())) }
    private fun gone(text: String) = eventually { onView(withText(text)).check(doesNotExist()) }

    @Test fun emptyList_showsEmptyMessage() {
        launch()
        eventually { onView(withText(R.string.list_empty)).check(matches(isDisplayed())) }
    }

    @Test fun insertedCompany_isListed() {
        seed(Company(name = "Acme", classification = consulting))
        launch()
        shown("Acme")
    }

    @Test fun delete_cancelKeepsTheCompany() {
        seed(Company(name = "Acme", classification = consulting))
        launch()
        shown("Acme")
        onView(withId(R.id.button_delete)).perform(click())
        onView(withText(R.string.delete_title)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).perform(click())
        shown("Acme")
    }

    @Test fun delete_confirmRemovesTheCompany() {
        seed(Company(name = "Acme", classification = consulting))
        launch()
        shown("Acme")
        onView(withId(R.id.button_delete)).perform(click())
        onView(withText(R.string.delete_confirm)).perform(click())
        gone("Acme")
        eventually { onView(withText(R.string.list_empty)).check(matches(isDisplayed())) }
    }

    @Test fun searchByName_filtersTheList() {
        seed(
            Company(name = "Alfa", classification = consulting),
            Company(name = "Beta", classification = consulting)
        )
        launch()
        shown("Alfa"); shown("Beta")
        onView(withId(R.id.input_search)).perform(replaceText("alf"), closeSoftKeyboard())
        shown("Alfa")
        gone("Beta")
    }

    @Test fun classificationChip_filtersTheList() {
        seed(
            Company(name = "Alfa", classification = consulting),
            Company(name = "Beta", classification = custom)
        )
        launch()
        shown("Alfa"); shown("Beta")
        onView(withId(R.id.chip_consulting)).perform(click())
        shown("Alfa")
        gone("Beta")
    }

    @Test fun createFromFab_addsCompanyToTheList() {
        launch()
        onView(withId(R.id.fab_add)).perform(click())
        onView(withId(R.id.input_name)).perform(replaceText("Nueva SA"), closeSoftKeyboard())
        onView(withId(R.id.check_consulting)).perform(scrollTo(), click())
        onView(withId(R.id.button_save)).perform(scrollTo(), click())
        shown("Nueva SA")
    }
}
