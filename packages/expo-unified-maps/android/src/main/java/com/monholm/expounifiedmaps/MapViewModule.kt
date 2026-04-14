package com.monholm.expounifiedmaps

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise

class MapViewModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("MapView")

    View(MapView::class) {
      Events("onMapClick", "onMarkerClick")

      AsyncFunction("setRegion") { view: MapView, options: SetRegionOptions, promise: Promise ->
        view.setRegion(options, promise)
      }

      AsyncFunction("fitToCoordinates") { view: MapView, options: FitToCoordinatesOptions, promise: Promise ->
        view.fitToCoordinates(options, promise)
      }

      Prop("initialRegion") { view: MapView, region: Region? ->
        view.setInitialRegion(region)
      }

      Prop("showCompass") { view: MapView, enabled: Boolean? ->
        view.setShowCompass(enabled)
      }

      Prop("zoomEnabled") { view: MapView, enabled: Boolean? ->
        view.setZoomEnabled(enabled)
      }

      Prop("scrollEnabled") { view: MapView, enabled: Boolean? ->
        view.setScrollEnabled(enabled)
      }

      Prop("rotateEnabled") { view: MapView, enabled: Boolean? ->
        view.setRotateEnabled(enabled)
      }

      Prop("pitchEnabled") { view: MapView, enabled: Boolean? ->
        view.setPitchEnabled(enabled)
      }

      Prop("mapPadding") { view: MapView, padding: Padding? ->
        view.setMapPadding(padding)
      }

      Prop("markers") { view: MapView, markers: Array<Marker>? ->
        view.setMarkers(markers)
      }

      Prop("boundary") { view: MapView, region: Region? ->
        view.setBoundary(region)
      }
    }
  }
}
