package com.example.peka.modules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peka.BuildConfig
import com.example.peka.R
import com.example.peka.api.TimeData
import com.example.peka.database.BusStop
import com.example.peka.viewmodels.DashboardViewModel
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
    onMarkerClick: (BusStop) -> Unit,
    selectedStop: BusStop?,
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    val context = LocalContext.current

    val currentStops by rememberUpdatedState(newValue = stops)

    // 1. Zapisujemy referencję do mapy, żebyśmy mogli nią sterować z zewnątrz
    var mapReference by remember { mutableStateOf<MapView?>(null) }

    val currentSelectedStop by rememberUpdatedState(selectedStop)

    val userLocation by dashboardViewModel.userLocation.collectAsState()
    val currentUserLocation by rememberUpdatedState(userLocation) // Najświeższa lokalizacja dla Listenerów mapy

    var isMapCenteredOnUser by remember { mutableStateOf(false) }

    // 2. MAGIA: Kiedy 'selectedStop' się zmieni, odpal ten kod!
    LaunchedEffect(selectedStop) {
        if (selectedStop != null && mapReference != null) {
            val map = mapReference!!
            val mapController = map.controller
            val position = GeoPoint(selectedStop.stop_lat, selectedStop.stop_lon)

            // Ponieważ jesteśmy w LaunchedEffect, wiemy że stan już się zaktualizował,
            // a funkcja updateVisibleMarkers ma już nową wartość selectedStop!
            mapController.animateTo(position)

            if (map.zoomLevelDouble < 15.0) {
                mapController.zoomTo(15.0)
            }
        }
    }

    // 2. NAPRAWIONE CENTROWANIE: Reaguje zarówno na lokalizację, jak i na gotowość mapy
    LaunchedEffect(userLocation, mapReference) {
        val map = mapReference
        val location = userLocation
        if (location != null && map != null && !isMapCenteredOnUser) {
            val position = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(position)
            map.controller.setZoom(16.0)
            isMapCenteredOnUser = true
        }
    }

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

                mapReference = this

                // Ustawiamy MapTiler jako główne źródło mapy!
                setTileSource(mapTilerDark)

                setMultiTouchControls(true)
                isTilesScaledToDpi = true // rozmazana mapa
                //isTilesScaledToDpi = false  // wolniej się ładuje ale ładna
                isVerticalMapRepetitionEnabled = false
                isHorizontalMapRepetitionEnabled = false

                controller.setZoom(15.0)


                val defaultCenter = GeoPoint(52.4064, 16.9252)
                controller.setCenter(defaultCenter)

                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops, onMarkerClick, currentSelectedStop, currentUserLocation)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        updateVisibleMarkers(this@apply, currentStops, onMarkerClick, currentSelectedStop, currentUserLocation)
                        return true
                    }
                })
            
            }
        },
        update = { mapView ->
            updateVisibleMarkers(mapView, currentStops, onMarkerClick, currentSelectedStop, currentUserLocation)

        }
    )
}


fun updateVisibleMarkers(
    mapView: MapView,
    allStops: List<BusStop>,
    onMarkerClick: (BusStop) -> Unit,
    selectedStop: BusStop?,
    userLocation: android.location.Location?
) {
    mapView.overlays.clear()

    if (userLocation != null) {
        val userMarker = Marker(mapView).apply {
            position = GeoPoint(userLocation.latitude, userLocation.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            val userIcon = ContextCompat.getDrawable(mapView.context, R.drawable.location_user)
            icon = userIcon

            setOnMarkerClickListener { _, _ -> true } // Wyłączamy dymek po kliknięciu na własną pozycję
        }
        mapView.overlays.add(userMarker)
    }

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
        var iconDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.location_on)?.mutate()

        val isSelected = (selectedStop != null && selectedStop.stop_code == stop.stop_code)

        if (iconDrawable != null) {
            if (isSelected) {
                // Jeśli to ten kliknięty -> nadaj kolor wybrany
                iconDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.location_on_clicked)?.mutate()
            } else {
                // W przeciwnym razie -> nadaj zwykły kolor
                iconDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.location_on)?.mutate()
            }
        }

        val marker = Marker(mapView).apply {
            position = GeoPoint(stop.stop_lat, stop.stop_lon)
//            title = stop.stop_name
//            snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ ->
                onMarkerClick(stop) // Wysyłamy kliknięty przystanek do Compose


                true // ZWRACAMY TRUE: to blokuje domyślny dymek (snippet) biblioteki
            }
            icon = iconDrawable
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}
