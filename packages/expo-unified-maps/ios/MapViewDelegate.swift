import MapKit

private let markerReuseId = "MarkerAnnotation"
private let screenScale = UIScreen.main.scale

// Images loaded via expo-image's SharedRef arrive with a scale of 1
// (if no 1x/2x/3x filename suffix is provided, which we consciously doesn't instruct the consumer to do add),
// so MapKit treats every pixel as one point — making icons appear much larger than on Android.
// Override the scale to match the device screen scale so MapKit sizes them correctly.
func scaleMarkerIcon(_ image: UIImage) -> UIImage {
  // Skip if the image already has the correct scale (e.g. on repeated calls for the same marker).
  if image.scale == screenScale { return image }
  guard let cgImage = image.cgImage else { return image }
  return UIImage(cgImage: cgImage, scale: screenScale, orientation: image.imageOrientation)
}

extension MapView: MKMapViewDelegate {
  func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    guard let marker = annotation as? MarkerAnnotation else {
      fatalError("Unexpected annotation type: \(type(of: annotation))")
    }

    let view = mapView.dequeueReusableAnnotationView(withIdentifier: markerReuseId)
      ?? MKAnnotationView(annotation: marker, reuseIdentifier: markerReuseId)

    view.annotation = marker
    view.image = scaleMarkerIcon(marker.icon)
    return view
  }
}
