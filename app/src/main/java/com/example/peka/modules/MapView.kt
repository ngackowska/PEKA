package com.example.peka.modules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.peka.BuildConfig
import com.example.peka.database.BusStop
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource


@Composable
fun OSMMapView(
    stops: List<BusStop>,
    modifier: Modifier = Modifier,
    onMarkerClick: (BusStop) -> Unit
) {
    val context = LocalContext.current

    val currentStops by rememberUpdatedState(newValue = stops)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().cacheMapTileCount = 12
            Configuration.getInstance().cacheMapTileOvershoot = 12



            MapView(ctx).apply {
                // Tutaj wklej swój klucz z MapTiler
                val mapTilerKey = BuildConfig.MAPTILER_API_KEY

                // Tworzymy źródło dla MapTiler Streets v4 Dark
                val mapTilerDark = XYTileSource(
                    "MapTilerStreetsDark",
                    0, // Minimalny zoom
                    20, // Maksymalny zoom
                    256, // Rozmiar kafelka (MapTiler obsługuje też 512 dla ekranów Retina)
                    // TRIK: Zamiast samego ".png", dodajemy parametry autoryzacji!
                    ".png?key=$mapTilerKey",
                    arrayOf(
                        "https://api.maptiler.com/maps/streets-v4-dark/256/"
                    )
                )

                // Ustawiamy MapTiler jako główne źródło mapy!
                setTileSource(mapTilerDark)

                setMultiTouchControls(true)
                isTilesScaledToDpi = true // rozmazana mapa
                //isTilesScaledToDpi = false  // wolniej się ładuje ale ładna
                isVerticalMapRepetitionEnabled = false
                isHorizontalMapRepetitionEnabled = false

                controller.setZoom(15.0)



//                setTilesScaledToDpi(true)


                // ###################
                // TU MOZNA DAC LOKALIZACJE UZYCK
                // ######################

                val poznanCenter = GeoPoint(52.4064, 16.9252)
                controller.setCenter(poznanCenter)

                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops, onMarkerClick)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops, onMarkerClick)
                        return true
                    }
                })
            
            }
        },
        update = { mapView ->
            updateVisibleMarkers(mapView, currentStops, onMarkerClick)

        }
    )
}


fun updateVisibleMarkers(mapView: MapView, allStops: List<BusStop>, onMarkerClick: (BusStop) -> Unit) {
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
//            title = stop.stop_name
//            snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ ->
                onMarkerClick(stop) // Wysyłamy kliknięty przystanek do Compose
                true // ZWRACAMY TRUE: to blokuje domyślny dymek (snippet) biblioteki
            }
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}
