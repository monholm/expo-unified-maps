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

// Converts our unified anchor point (fractions of the icon's size, 0–1) to an
// MKAnnotationView centerOffset. Android's MarkerOptions.anchor uses the same
// 0–1 coordinate space; iOS uses centerOffset, which is in points measured from
// the image's center. This formula maps one to the other so identical anchorPoint
// values produce identical visual placement on both platforms.
// A nil anchorPoint defaults to (0.5, 1.0) — bottom-center — matching Android's
// default for custom-icon markers.
func centerOffset(for anchor: Point?, imageSize: CGSize) -> CGPoint {
  let ax = anchor?.x ?? 0.5
  let ay = anchor?.y ?? 1.0
  return CGPoint(
    x: (0.5 - ax) * imageSize.width,
    y: (0.5 - ay) * imageSize.height
  )
}

extension MapView: MKMapViewDelegate {
  func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    guard let marker = annotation as? MarkerAnnotation else {
      fatalError("Unexpected annotation type: \(type(of: annotation))")
    }

    let view = mapView.dequeueReusableAnnotationView(withIdentifier: markerReuseId)
      ?? MKAnnotationView(annotation: marker, reuseIdentifier: markerReuseId)

    view.annotation = marker
    let icon = scaleMarkerIcon(marker.icon)
    view.image = icon
    // centerOffset is applied unconditionally — even when no anchorPoint was provided —
    // because MKAnnotationView's default anchors the image center on the coordinate,
    // whereas Google Maps defaults to bottom-center. We unify on Android's default so
    // iOS must explicitly offset every marker.
    view.centerOffset = centerOffset(for: marker.anchorPoint, imageSize: icon.size)
    return view
  }
}
