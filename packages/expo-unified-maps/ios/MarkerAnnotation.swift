import Foundation
import MapKit

class MarkerAnnotation: NSObject, MKAnnotation {
  let id: String
  dynamic var coordinate: CLLocationCoordinate2D
  var icon: UIImage

  init(id: String, coordinate: CLLocationCoordinate2D, icon: UIImage) {
    self.id = id
    self.coordinate = coordinate
    self.icon = icon
    super.init()
  }
}
