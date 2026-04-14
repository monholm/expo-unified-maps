import ExpoModulesCore

public class MapViewModule: Module {
  public func definition() -> ModuleDefinition {
    Name("MapView")

    View(MapView.self) {
      Events("onMapClick", "onMarkerClick")

      AsyncFunction("setRegion") { (view: MapView, options: SetRegionOptions, promise: Promise) in
        view.setRegion(options: options, promise: promise)
      }


      Prop("initialRegion") { (view: MapView, region: Region?) in
        view.setInitialRegion(region: region)
      }

      Prop("showCompass") { (view: MapView, enabled: Bool?) in
        view.setShowCompass(enabled: enabled)
      }

      Prop("zoomEnabled") { (view: MapView, enabled: Bool?) in
        view.setZoomEnabled(enabled: enabled)
      }
      
      Prop("scrollEnabled") { (view: MapView, enabled: Bool?) in
        view.setScrollEnabled(enabled: enabled)
      }
      
      Prop("rotateEnabled") { (view: MapView, enabled: Bool?) in
        view.setRotateEnabled(enabled: enabled)
      }
      
      Prop("pitchEnabled") { (view: MapView, enabled: Bool?) in
        view.setPitchEnabled(enabled: enabled)
      }

      Prop("mapPadding") { (view: MapView, padding: Padding?) in
        view.setMapPadding(padding: padding)
      }

      Prop("markers") { (view: MapView, markers: [Marker]?) in
        view.setMarkers(markers: markers)
      }

      Prop("boundary") { (view: MapView, region: Region?) in
        view.setBoundary(region: region)
      }
    }
  }
}
