import Foundation
import MapKit

class MarkerAnnotation: NSObject, MKAnnotation {
  let id: String
  dynamic var coordinate: CLLocationCoordinate2D
  var icon: UIImage
  var anchorPoint: Point?

  init(id: String, coordinate: CLLocationCoordinate2D, icon: UIImage, anchorPoint: Point?) {
    self.id = id
    self.coordinate = coordinate
    self.icon = icon
    self.anchorPoint = anchorPoint
    super.init()
  }
}
