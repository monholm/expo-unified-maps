import ExpoModulesCore
import MapKit

class MapView: ExpoView {
  let mapView = MKMapView()
  let onMapClick = EventDispatcher()
  let onMarkerClick = EventDispatcher()
  private var initialRegionApplied = false

  required init(appContext: AppContext? = nil) {
    super.init(appContext: appContext)
    clipsToBounds = true
    mapView.delegate = self
    addSubview(mapView)

    let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleMapTap))
    mapView.addGestureRecognizer(tapGesture)
  }

  @objc private func handleMapTap(_ gesture: UITapGestureRecognizer) {
    let point = gesture.location(in: mapView)

    // Expand the visible rect so markers whose coordinate is just off-screen
    // but whose icon is still visible are included in the hit test.
    let expandedRect = mapView.visibleMapRect.insetBy(dx: -mapView.visibleMapRect.size.width * 0.1, dy: -mapView.visibleMapRect.size.height * 0.1)
    // only consider annotations that are visible on screen (or just off-screen within the expanded rect) for hit testing
    // to avoid iterating over all annotations which could be a performance issue with many markers
    for annotation in mapView.annotations(in: expandedRect) {
      if let marker = annotation as? MarkerAnnotation,
         let view = mapView.view(for: marker),
         view.frame.contains(point) {
        onMarkerClick([
          "id": marker.id,
          "coordinate": [
            "latitude": marker.coordinate.latitude,
            "longitude": marker.coordinate.longitude
          ]
        ])
        return
      }
    }

    let coordinate = mapView.convert(point, toCoordinateFrom: mapView)
    onMapClick([
      "coordinate": ["latitude": coordinate.latitude, "longitude": coordinate.longitude],
      "point": ["x": point.x, "y": point.y]
    ])
  }

  func setInitialRegion(region: Region?) {
    guard !initialRegionApplied, let region = region else { return }
    initialRegionApplied = true
    mapView.setRegion(region.mkCoordinateRegion, animated: false)
  }

  override func layoutSubviews() {
    mapView.frame = bounds
  }

  func setShowCompass(enabled: Bool?) {
    guard let enabled = enabled else { return }
    mapView.showsCompass = enabled
  }

  func setZoomEnabled(enabled: Bool?) {
    guard let enabled = enabled else { return }
    mapView.isZoomEnabled = enabled
  }
  
  func setScrollEnabled(enabled: Bool?) {
    guard let enabled = enabled else { return }
    mapView.isScrollEnabled = enabled
  }
  
  func setRotateEnabled(enabled: Bool?) {
    guard let enabled = enabled else { return }
    mapView.isRotateEnabled = enabled
  }
  
  func setPitchEnabled(enabled: Bool?) {
    guard let enabled = enabled else { return }
    mapView.isPitchEnabled = enabled
  }

  func setMapPadding(padding: Padding?) {
    guard let padding = padding else { return }
    mapView.layoutMargins = UIEdgeInsets(
      top: padding.top,
      left: padding.left,
      bottom: padding.bottom,
      right: padding.right
    )
  }

  // While iOS supports padding (insets) natively, android does not,
  // so we've decided to implement it on both platforms for consistency.
  func fitToCoordinates(options: FitToCoordinatesOptions, promise: Promise) {
    let coordinates = options.coordinates

    var maxLatitude = -90.0
    var minLatitude = 90.0
    var maxLongitude = -180.0
    var minLongitude = 180.0

    for coord in coordinates {
      maxLatitude = max(maxLatitude, coord.latitude)
      minLatitude = min(minLatitude, coord.latitude)
      maxLongitude = max(maxLongitude, coord.longitude)
      minLongitude = min(minLongitude, coord.longitude)
    }

    let padding = options.padding
    let mapViewHeight = mapView.bounds.size.height
    let mapViewWidth = mapView.bounds.size.width
    let latitudeDelta = maxLatitude - minLatitude
    let longitudeDelta = maxLongitude - minLongitude
    let latPerHeight = latitudeDelta / (mapViewHeight - padding.top - padding.bottom)
    let lngPerWidth = longitudeDelta / (mapViewWidth - padding.left - padding.right)

    maxLatitude += latPerHeight * padding.top
    minLatitude -= latPerHeight * padding.bottom
    maxLongitude += lngPerWidth * padding.right
    minLongitude -= lngPerWidth * padding.left

    let finalLatDelta = max(maxLatitude - minLatitude, 0.000001)
    let finalLngDelta = max(maxLongitude - minLongitude, 0.000001)

    var region = MKCoordinateRegion()
    region.center.latitude = (minLatitude + maxLatitude) / 2
    region.center.longitude = (minLongitude + maxLongitude) / 2
    region.span.latitudeDelta = finalLatDelta
    region.span.longitudeDelta = finalLngDelta

    let duration = Double(options.animateDuration) / 1000.0
    if duration > 0 {
      MKMapView.animate(withDuration: duration, animations: {
        self.mapView.setRegion(region, animated: true)
      }, completion: { _ in
        promise.resolve(nil)
      })
    } else {
      mapView.setRegion(region, animated: false)
      promise.resolve(nil)
    }
  }

  func setRegion(options: SetRegionOptions, promise: Promise) {
    applyRegion(options: options) {
      promise.resolve(nil)
    }
  }

  func setBoundary(region: Region?) {
    if let region = region {
      let boundary = MKMapView.CameraBoundary(coordinateRegion: region.mkCoordinateRegion)
      mapView.setCameraBoundary(boundary, animated: false)
    } else {
      mapView.setCameraBoundary(nil, animated: false)
    }
  }

  private func applyRegion(options: SetRegionOptions, completion: (() -> Void)? = nil) {
    let region = options.region
    let duration = Double(options.animateDuration) / 1000.0
    
    if duration > 0 {
      MKMapView.animate(withDuration: duration, animations: {
        self.mapView.setRegion(region.mkCoordinateRegion, animated: true)
      }, completion: { _ in
        completion?()
      })
    } else {
      mapView.setRegion(region.mkCoordinateRegion, animated: false)
      completion?()
    }
  }

  func setMarkers(markers: [Marker]?) {
    let incoming = markers ?? []
    let existingAnnotations = mapView.annotations.compactMap { $0 as? MarkerAnnotation }
    let existingById = Dictionary(uniqueKeysWithValues: existingAnnotations.map { ($0.id, $0) })
    let incomingIds = Set(incoming.map { $0.id })

    // Remove annotations no longer present
    mapView.removeAnnotations(existingAnnotations.filter { !incomingIds.contains($0.id) })

    // Add new and update existing
    var toAdd: [MarkerAnnotation] = []
    for marker in incoming {
      if let existing = existingById[marker.id] {
        existing.coordinate = marker.coordinate.clLocationCoordinate2D
        existing.icon = marker.icon.ref
        if let view = mapView.view(for: existing) {
          view.image = scaleMarkerIcon(existing.icon)
        }
      } else {
        toAdd.append(MarkerAnnotation(
          id: marker.id,
          coordinate: marker.coordinate.clLocationCoordinate2D,
          icon: marker.icon.ref
        ))
      }
    }
    mapView.addAnnotations(toAdd)
  }
}
