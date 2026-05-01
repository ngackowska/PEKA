package com.example.peka.modules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.peka.database.BusStop
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent


@Composable
fun OSMMapView(
    stops: List<BusStop>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentStops by rememberUpdatedState(newValue = stops)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                setTilesScaledToDpi(true)

                val poznanCenter = GeoPoint(52.4064, 16.9252)
                controller.setCenter(poznanCenter)

                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops)
                        return true
                    }
                })
            
            }
        },
        update = { mapView ->
            updateVisibleMarkers(mapView, currentStops)

        }
    )
}


fun updateVisibleMarkers(mapView: MapView, allStops: List<BusStop>) {
    mapView.overlays.clear()

    if (mapView.zoomLevelDouble < 14.5) {
        mapView.invalidate()
        return
    }


    val box = mapView.boundingBox ?: return

    val visibleStops = allStops.filter { stop ->
        stop.stop_lat <= box.latNorth &&
                stop.stop_lat >= box.latSouth &&
                stop.stop_lon <= box.lonEast &&
                stop.stop_lon >= box.lonWest
    }

    visibleStops.forEach { stop ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(stop.stop_lat, stop.stop_lon)
            title = stop.stop_name
            snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}
