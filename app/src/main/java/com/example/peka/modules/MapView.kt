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
    stops: List<BusStop>, // Ta lista zaktualizuje się automatycznie po pobraniu danych z Firebase
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentStops by rememberUpdatedState(newValue = stops)

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

                // Dodajemy nasłuchiwacz ruchów palcem po mapie
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        // Użytkownik przesuwa mapę -> rysuj nowe punkty
                        updateVisibleMarkers(this@apply, currentStops)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        // Użytkownik przybliża/oddala -> rysuj nowe punkty
                        updateVisibleMarkers(this@apply, currentStops)
                        return true
                    }
                })
            
            }
        },
        update = { mapView ->

            // Uruchamia się od razu po pobraniu danych z Firebase, by pokazać startowe punkty
            updateVisibleMarkers(mapView, currentStops)

            // 2. BLOK UPDATE: Uruchamia się ZAWSZE, gdy zmienna 'stops' się zmieni

//            // Najpierw czyścimy stare pinezki, żeby się nie nakładały (ważne!)
//            mapView.overlays.clear()
//
//            // Przechodzimy przez nową listę przystanków (z Firebase)
//            stops.forEach { stop ->
//                val marker = Marker(mapView).apply {
//                    position = GeoPoint(stop.stop_lat, stop.stop_lon)
//                    title = stop.stop_name
//                    snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
//                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                }
//
//                // Dodajemy pinezkę do mapy
//                mapView.overlays.add(marker)
//            }
//
//            // Opcjonalnie: Jeśli chcemy, by kamera automatycznie skoczyła do pierwszego przystanku z listy
//            if (stops.isNotEmpty()) {
//                val firstStop = GeoPoint(stops[0].stop_lat, stops[0].stop_lon)
//                mapView.controller.animateTo(firstStop)
//            }
//
//            // NAJWAŻNIEJSZE: Wymuszamy na mapie przerysowanie się, by pokazała nowe punkty
//            mapView.invalidate()
        }
    )
}

// Funkcja pomocnicza: Analizuje ekran i rysuje maksymalnie 20 pinezek
fun updateVisibleMarkers(mapView: MapView, allStops: List<BusStop>) {
    // 1. Zawsze czyścimy stare pinezki przed narysowaniem nowych
    mapView.overlays.clear()

    // 2. Jeśli mapa jest oddalona (np. widać całą Wielkopolskę), zrezygnuj z rysowania czegokolwiek
    // Wartość 14.5 to dobry kompromis, przetestuj, co działa dla Ciebie najlepiej
    if (mapView.zoomLevelDouble < 14.5) {
        mapView.invalidate() // Odśwież czystą mapę
        return
    }

    // 3. Wyciągamy granice obszaru, który użytkownik aktualnie widzi na ekranie
    val box = mapView.boundingBox ?: return

    // 4. Szybkie przefiltrowanie wszystkich 3000 przystanków
    val visibleStops = allStops.filter { stop ->
        // Warunek: Współrzędne przystanku muszą mieścić się w "pudełku" ekranu
        stop.stop_lat <= box.latNorth &&
                stop.stop_lat >= box.latSouth &&
                stop.stop_lon <= box.lonEast &&
                stop.stop_lon >= box.lonWest
    }
//    }.take(20) // Magiczna funkcja odcinająca resztę, jeśli na ekranie jest więcej niż 20

    // 5. Zamiana przefiltrowanych obiektów na pinezki
    visibleStops.forEach { stop ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(stop.stop_lat, stop.stop_lon)
            title = stop.stop_name
            snippet = "Kod: ${stop.stop_code} | Strefa: ${stop.zone_id}"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
    }

    // 6. Nakazujemy systemowi Android narysować wszystko na nowo
    mapView.invalidate()
}
