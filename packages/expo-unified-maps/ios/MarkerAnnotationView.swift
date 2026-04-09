import ExpoModulesCore
import MapKit

class MarkerAnnotationView: MKAnnotationView {
  unowned var mapView: MapView

  init(annotation: MKAnnotation, reuseIdentifier: String, mapView: MapView) {
    self.mapView = mapView
    super.init(annotation: annotation, reuseIdentifier: reuseIdentifier)
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // Use a simple touch handler to detect marker clicks instead of the build-in `didSelect` method.
  // There's no such concept as `enabled` or `didSelect` on Android.
  override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
    super.touchesEnded(touches, with: event)
    
    guard let annotation = annotation as? MarkerAnnotation else { return }
    
    let markerData: [String: Any] = [
      "id": annotation.id,
      "coordinate": [
        "latitude": annotation.coordinate.latitude,
        "longitude": annotation.coordinate.longitude
      ]
    ]
    
    mapView.onMarkerClick(markerData)
  }
}
