package com.example.mauro.glovoclient

import com.example.mauro.glovoclient.presenters.ModelPresenter
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.lang.Thread.sleep

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ModelPresenterUnitTest {

    private lateinit var presenter: ModelPresenter
    private lateinit var view: ModelPresenter.View
    private var correctLatitude: Double = -34.576906
    private var correctLongitude: Double = -58.429394
    private var incorrectLatitude: Double = -34.499856
    private var incorrectLongitude: Double = -58.351375

    @Before
    fun setup() {
        presenter = ModelPresenter()
        view = mock()


        presenter.hydrateModel()
        sleep(10000)
    }
    @Test
    fun currentZoneOutOfBounds() {
        assertEquals(true, presenter.IsInsideWorkingArea(correctLatitude, correctLongitude))
    }

    @Test
    fun currentZoneInsideBounds() {
        assertEquals(false, presenter.IsInsideWorkingArea(incorrectLatitude, incorrectLongitude))
    }

    @Test
    fun getBoundsOnExistingCity() {
        val mapBounds = presenter.getBounds("BUE")

        assertEquals(true, mapBounds!=null)
    }
}
