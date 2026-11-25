package com.unal.reto10

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.unal.reto10.data.FakeCollectionPointRepository
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.LoadState
import com.unal.reto10.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/** Ejecuta las tareas en el mismo hilo, de forma determinista. */
private class DirectExecutor : AbstractExecutorService() {
    private var down = false
    override fun execute(command: Runnable) = command.run()
    override fun shutdown() { down = true }
    override fun shutdownNow(): MutableList<Runnable> { down = true; return mutableListOf() }
    override fun isShutdown() = down
    override fun isTerminated() = down
    override fun awaitTermination(timeout: Long, unit: TimeUnit) = true
}

class MainViewModelTest {
    @get:Rule val rule = InstantTaskExecutorRule()

    private fun vm(repo: FakeCollectionPointRepository) = MainViewModel(repo, DirectExecutor(), pageSize = 20)

    @Test fun successLoadsFirstPageAndTotal() {
        val vm = vm(FakeCollectionPointRepository(FakeCollectionPointRepository.sample(45)))
        vm.search(null, "")
        val s = vm.state.value as LoadState.Success
        assertEquals(20, s.items.size)
        assertEquals(45, s.total)
    }

    @Test fun emptyResult() {
        val vm = vm(FakeCollectionPointRepository(FakeCollectionPointRepository.sample(3)))
        vm.search(null, "zzz")
        assertEquals(LoadState.Empty, vm.state.value)
    }

    @Test fun errorIsReportedWithKindAndRetryRecovers() {
        val repo = FakeCollectionPointRepository(FakeCollectionPointRepository.sample(3), error = ErrorKind.NO_NETWORK)
        val vm = vm(repo)
        vm.search(null, "")
        assertEquals(LoadState.Error(ErrorKind.NO_NETWORK), vm.state.value)
        repo.error = null
        vm.retry()
        assertTrue(vm.state.value is LoadState.Success)
    }

    @Test fun loadMoreAccumulatesWithoutDuplicatesUntilTheEnd() {
        val vm = vm(FakeCollectionPointRepository(FakeCollectionPointRepository.sample(45)))
        vm.search(null, "")
        vm.loadMore(); vm.loadMore()
        val s = vm.state.value as LoadState.Success
        assertEquals(45, s.items.size)
        assertEquals(45, s.items.map { it.businessName }.distinct().size)
        assertFalse(vm.hasMore())
        vm.loadMore() // no hay más: no debe consultar de nuevo
        assertEquals(45, (vm.state.value as LoadState.Success).items.size)
    }

    @Test fun filtersApplyMunicipalityAndText() {
        val points = FakeCollectionPointRepository.sample(5, "Ocaña") + FakeCollectionPointRepository.sample(4, "Abrego")
        val vm = vm(FakeCollectionPointRepository(points))
        vm.search("Abrego", "punto 02")
        assertEquals(1, (vm.state.value as LoadState.Success).items.size)
    }

    @Test fun staleResponseIsDiscarded() {
        // Ejecutor que difiere la primera tarea para simular dos búsquedas rápidas.
        val queue = ArrayDeque<Runnable>()
        val deferred = object : AbstractExecutorService() {
            override fun execute(command: Runnable) { queue.add(command) }
            override fun shutdown() {}
            override fun shutdownNow() = mutableListOf<Runnable>()
            override fun isShutdown() = false
            override fun isTerminated() = false
            override fun awaitTermination(timeout: Long, unit: TimeUnit) = true
        }
        val repo = FakeCollectionPointRepository(
            FakeCollectionPointRepository.sample(3, "Ocaña") + FakeCollectionPointRepository.sample(7, "Abrego")
        )
        val vm = MainViewModel(repo, deferred, 20)
        vm.search("Ocaña", "")
        vm.search("Abrego", "")
        while (queue.isNotEmpty()) queue.removeFirst().run()
        assertEquals(7, (vm.state.value as LoadState.Success).items.size)
    }

    @Test fun municipalitiesAreLoadedOnce() {
        val vm = vm(FakeCollectionPointRepository(FakeCollectionPointRepository.sample(3)))
        vm.loadMunicipalities()
        assertEquals(listOf("Ocaña"), vm.municipalities.value)
    }
}
