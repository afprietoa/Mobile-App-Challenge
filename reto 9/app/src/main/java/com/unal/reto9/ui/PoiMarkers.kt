package com.unal.reto9.ui

import androidx.core.content.ContextCompat
import com.unal.reto9.R
import com.unal.reto9.model.PointOfInterest
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Locale
import kotlin.math.roundToInt

object PoiMarkers {

    fun userMarker(map: MapView, point: GeoPoint, title: String) = Marker(map).apply {
        position = point
        this.title = title
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        icon = ContextCompat.getDrawable(map.context, R.drawable.ic_user_dot)
    }

    fun poiMarker(map: MapView, poi: PointOfInterest) = Marker(map).apply {
        position = GeoPoint(poi.lat, poi.lon)
        title = poi.name                                    // requisito del reto: mostrar el nombre
        snippet = "${map.context.getString(poi.category.label)} · ${formatDistance(poi.distanceMeters)}"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        icon = ContextCompat.getDrawable(map.context, R.drawable.ic_poi)
    }

    /** Círculo de [radiusMeters] alrededor del origen. */
    fun radiusCircle(origin: GeoPoint, radiusMeters: Int) = Polygon().apply {
        points = Polygon.pointsAsCircle(origin, radiusMeters.toDouble())
        fillPaint.color = 0x1A1976D2
        outlinePaint.color = 0xFF1976D2.toInt()
        outlinePaint.strokeWidth = 3f
        infoWindow = null
    }

    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${meters.roundToInt()} m"
        else String.format(Locale.getDefault(), "%.1f km", meters / 1000)
}
