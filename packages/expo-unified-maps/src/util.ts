import type {LatLng, Region} from './MapView.types.js';

/**
 * Computes the smallest region that contains all the given coordinates.
 *
 * The returned region's center is the midpoint of the bounding box,
 * and its deltas span the full extent of the coordinates.
 */
export function regionForCoordinates(coordinates: LatLng[]): Region {
  let maxLatitude = -90;
  let minLatitude = 90;
  let maxLongitude = -180;
  let minLongitude = 180;

  for (const coord of coordinates) {
    maxLatitude = Math.max(maxLatitude, coord.latitude);
    minLatitude = Math.min(minLatitude, coord.latitude);
    maxLongitude = Math.max(maxLongitude, coord.longitude);
    minLongitude = Math.min(minLongitude, coord.longitude);
  }

  return {
    latitude: (minLatitude + maxLatitude) / 2,
    longitude: (minLongitude + maxLongitude) / 2,
    latitudeDelta: Math.max(maxLatitude - minLatitude, 0.000001),
    longitudeDelta: Math.max(maxLongitude - minLongitude, 0.000001),
  };
}
