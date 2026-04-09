import ExpoModulesCore
import MapKit

struct LatLng: Record {
  @Field var latitude: Double = 0.0
  @Field var longitude: Double = 0.0
  
  var clLocationCoordinate2D: CLLocationCoordinate2D {
    return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
  }
}

struct Region: Record {
  @Field var latitude: Double = 0.0

  @Field var longitude: Double = 0.0

  @Field var latitudeDelta: Double = 0.0

  @Field var longitudeDelta: Double = 0.0

  var mkCoordinateRegion: MKCoordinateRegion {
    let center = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    let span = MKCoordinateSpan(latitudeDelta: latitudeDelta, longitudeDelta: longitudeDelta)
    return MKCoordinateRegion(center: center, span: span)
  }
}

struct SetRegionOptions: Record {
  @Field var region: Region = Region()

  @Field var animateDuration: Int = 0
}

struct Padding: Record {
  @Field var top: Double = 0.0
  @Field var bottom: Double = 0.0
  @Field var left: Double = 0.0
  @Field var right: Double = 0.0
}

struct FitToCoordinatesOptions: Record {
  @Field var coordinates: [LatLng] = []
  @Field var padding: Padding = Padding()
  @Field var animateDuration: Int = 0
}

struct Marker: Record {
  @Field var id: String = ""
  @Field var coordinate: LatLng = LatLng()
  @Field var icon: SharedRef<UIImage> = SharedRef(UIImage())
}
