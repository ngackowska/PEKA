package com.example.peka.modules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.peka.database.BusStop
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OSMMapView(
    stops: List<BusStop>, // Ta lista zaktualizuje się automatycznie po pobraniu danych z Firebase
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // 1. BLOK FACTORY: Uruchamia się TYLKO RAZ przy starcie ekranu
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                setTilesScaledToDpi(true)

                // Ustawiamy startowy widok na centrum Poznania (lub Rondo Kaponiera)
                val poznanCenter = GeoPoint(52.4064, 16.9252)
                controller.setCenter(poznanCenter)

                // UWAGA: Tu NIE dodajemy pinezek! Zrobimy to w bloku update.
            }
        },
        update = { mapView ->
            // 2. BLOK UPDATE: Uruchamia się ZAWSZE, gdy zmienna 'stops' się zmieni

            // Najpierw czyścimy stare pinezki, żeby się nie nakładały (ważne!)
            mapView.overlays.clear()

            // Przechodzimy przez nową listę przystanków (z Firebase)
            stops.forEach { stop ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(stop.stop_lat, stop.stop_lon)
                    title = stop.stop_name
                    snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                // Dodajemy pinezkę do mapy
                mapView.overlays.add(marker)
            }

            // Opcjonalnie: Jeśli chcemy, by kamera automatycznie skoczyła do pierwszego przystanku z listy
            if (stops.isNotEmpty()) {
                val firstStop = GeoPoint(stops[0].stop_lat, stops[0].stop_lon)
                mapView.controller.animateTo(firstStop)
            }

            // NAJWAŻNIEJSZE: Wymuszamy na mapie przerysowanie się, by pokazała nowe punkty
            mapView.invalidate()
        }
    )
}