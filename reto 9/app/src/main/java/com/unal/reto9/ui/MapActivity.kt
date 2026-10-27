package com.unal.reto9.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.unal.reto9.R
import com.unal.reto9.data.PoiException
import com.unal.reto9.data.ServiceLocator
import com.unal.reto9.data.SettingsStore
import com.unal.reto9.databinding.ActivityMapBinding
import com.unal.reto9.model.RadiusPolicy
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Overlay
import java.util.Locale
import java.util.concurrent.Executors

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private lateinit var settings: SettingsStore
    private val viewModel: MapViewModel by viewModels()
    private val locationSource by lazy { ServiceLocator.locationSourceFactory(this) }
    private val repository get() = ServiceLocator.poiRepository
    private val io = Executors.newSingleThreadExecutor()

    /** Overlays dibujados por [render]; el overlay de eventos del mapa no se toca. */
    private val drawn = mutableListOf<Overlay>()
    private var requestId = 0

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            if (grants.values.any { it }) locate() else showPermissionDenied()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // osmdroid exige configurar el User-Agent antes de inflar el MapView.
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        settings = SettingsStore(applicationContext)
        addMenuProvider(menuProvider)

        with(binding.map) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(savedInstanceState?.getDouble(STATE_ZOOM) ?: DEFAULT_ZOOM)
            controller.setCenter(
                if (savedInstanceState != null)
                    GeoPoint(savedInstanceState.getDouble(STATE_LAT), savedInstanceState.getDouble(STATE_LON))
                else DEFAULT_CENTER
            )
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let { setOrigin(it, address = null) }
                    return true
                }
            }))
        }

        // Tras rotar: repintar desde el ViewModel, sin GPS ni red.
        viewModel.origin?.let { origin ->
            binding.toolbar.subtitle = viewModel.address ?: formatCoords(origin)
            viewModel.lastQuery?.let { render(origin, it.radiusKm, fit = false) }
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.origin == null) {
            if (hasLocationPermission()) locate() else showPermissionRationale()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
        // Volver de Ajustes: solo se re-consulta si radio o categorías cambiaron.
        val origin = viewModel.origin
        if (origin != null && viewModel.lastQuery != currentQuery(origin)) refresh()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onStop() {
        locationSource.stop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val center = binding.map.mapCenter
        outState.putDouble(STATE_LAT, center.latitude)
        outState.putDouble(STATE_LON, center.longitude)
        outState.putDouble(STATE_ZOOM, binding.map.zoomLevelDouble)
    }

    override fun onDestroy() {
        binding.map.onDetach()
        io.shutdownNow()
        super.onDestroy()
    }

    // ---- Menú -------------------------------------------------------------------------------

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.map_menu, menu)
            val searchItem = menu.findItem(R.id.action_search)
            val search = searchItem.actionView as SearchView
            search.queryHint = getString(R.string.search_hint)
            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchAddress(query)
                    searchItem.collapseActionView()
                    return true
                }

                override fun onQueryTextChange(newText: String) = false
            })
        }

        override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
            R.id.action_locate -> { ensureLocation(); true }
            R.id.action_refresh -> { refresh(); true }
            R.id.action_settings -> { startActivity(Intent(this@MapActivity, SettingsActivity::class.java)); true }
            else -> false
        }
    }

    // ---- Ubicación --------------------------------------------------------------------------

    private fun hasLocationPermission() =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestPermissions() = permissionLauncher.launch(
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    private fun ensureLocation() {
        if (hasLocationPermission()) locate() else requestPermissions()
    }

    private fun showPermissionRationale() {
        Snackbar.make(binding.mapContainer, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.allow) { requestPermissions() }
            .show()
    }

    private fun showPermissionDenied() {
        Snackbar.make(binding.mapContainer, R.string.permission_denied, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.settings) {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                )
            }
            .show()
    }

    private fun locate() {
        binding.progress.isVisible = true
        locationSource.current { location ->
            if (isDestroyed) return@current
            binding.progress.isVisible = false
            if (location == null) {
                Snackbar.make(binding.mapContainer, R.string.location_unavailable, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { locate() }
                    .show()
            } else {
                setOrigin(GeoPoint(location.latitude, location.longitude), address = null)
            }
        }
    }

    /** Fija un nuevo origen (GPS, dirección o toque largo), resuelve su dirección y consulta. */
    private fun setOrigin(point: GeoPoint, address: String?) {
        viewModel.origin = point
        viewModel.address = address
        binding.toolbar.subtitle = address ?: formatCoords(point)
        binding.map.controller.animateTo(point)
        if (address == null) resolveAddress(point)
        refresh()
    }

    @Suppress("DEPRECATION")
    private fun resolveAddress(point: GeoPoint) {
        if (!Geocoder.isPresent()) return
        io.execute {
            val text = runCatching {
                Geocoder(this, Locale.getDefault()).getFromLocation(point.latitude, point.longitude, 1)
                    ?.firstOrNull()?.getAddressLine(0)
            }.getOrNull() ?: return@execute
            runOnUiThread {
                if (isDestroyed || viewModel.origin != point) return@runOnUiThread
                viewModel.address = text
                binding.toolbar.subtitle = text
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun searchAddress(text: String) {
        if (text.isBlank()) return
        io.execute {
            val found = if (Geocoder.isPresent()) runCatching {
                Geocoder(this, Locale.getDefault()).getFromLocationName(text, 1)?.firstOrNull()
            }.getOrNull() else null
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (found == null) {
                    Snackbar.make(binding.mapContainer, R.string.address_not_found, Snackbar.LENGTH_LONG).show()
                } else {
                    setOrigin(GeoPoint(found.latitude, found.longitude), found.getAddressLine(0) ?: text)
                }
            }
        }
    }

    // ---- Consulta y dibujo ------------------------------------------------------------------

    private fun currentQuery(origin: GeoPoint) =
        PoiQuery(origin.latitude, origin.longitude, settings.radiusKm, settings.categories)

    private fun refresh() {
        val origin = viewModel.origin ?: return ensureLocation()
        val query = currentQuery(origin)
        val id = ++requestId

        if (query.categories.isEmpty()) {
            viewModel.lastQuery = query
            viewModel.lastResult = emptyList()
            binding.progress.isVisible = false
            render(origin, query.radiusKm, fit = true)
            Snackbar.make(binding.mapContainer, R.string.no_categories_selected, Snackbar.LENGTH_LONG).show()
            return
        }

        binding.progress.isVisible = true
        io.execute {
            val result = runCatching {
                repository.search(query.lat, query.lon, RadiusPolicy.toMeters(query.radiusKm), query.categories)
            }
            runOnUiThread {
                if (isDestroyed || id != requestId) return@runOnUiThread   // respuesta obsoleta
                binding.progress.isVisible = false
                result.onSuccess { pois ->
                    viewModel.lastQuery = query
                    viewModel.lastResult = pois
                    render(origin, query.radiusKm, fit = true)
                    if (pois.isEmpty()) {
                        Snackbar.make(binding.mapContainer, R.string.no_pois_in_radius, Snackbar.LENGTH_LONG).show()
                    }
                }.onFailure { showError(it as? PoiException) }
            }
        }
    }

    private fun render(origin: GeoPoint, radiusKm: Int, fit: Boolean) {
        val map = binding.map
        map.overlays.removeAll(drawn.toSet())
        drawn.clear()
        val circle = PoiMarkers.radiusCircle(origin, RadiusPolicy.toMeters(radiusKm))
        drawn += circle
        drawn += PoiMarkers.userMarker(map, origin, getString(R.string.you_are_here))
        viewModel.lastResult.forEach { drawn += PoiMarkers.poiMarker(map, it) }
        map.overlays.addAll(drawn)

        val count = viewModel.lastResult.size
        binding.counter.text = resources.getQuantityString(R.plurals.poi_count, count, count, radiusKm)
        binding.counter.isVisible = true
        if (fit) {
            // Se ajusta al círculo del radio (caja siempre no degenerada), tras el layout.
            val box = BoundingBox.fromGeoPoints(circle.actualPoints)
            map.post { map.zoomToBoundingBox(box, true, 80) }
        }
        map.invalidate()
    }

    private fun showError(error: PoiException?) {
        val message = when (error?.kind) {
            PoiException.Kind.NO_NETWORK -> R.string.error_no_network
            PoiException.Kind.TIMEOUT -> R.string.error_timeout
            PoiException.Kind.SERVER_BUSY -> R.string.error_server_busy
            else -> R.string.error_bad_response
        }
        Snackbar.make(binding.mapContainer, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { refresh() }
            .show()
    }

    private fun formatCoords(p: GeoPoint) = String.format(Locale.US, "%.4f, %.4f", p.latitude, p.longitude)

    companion object {
        private const val STATE_LAT = "state_lat"
        private const val STATE_LON = "state_lon"
        private const val STATE_ZOOM = "state_zoom"
        private const val DEFAULT_ZOOM = 12.0
        private val DEFAULT_CENTER = GeoPoint(4.6097, -74.0817)   // Bogotá
    }
}
