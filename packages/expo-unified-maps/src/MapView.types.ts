import type {ViewProps} from 'react-native';
import type {SharedRefType} from 'expo';

export interface MapViewNativeProps extends ViewProps {
  boundary?: Region | undefined;
  /**
   * The initial region to display on the map.
   *
   * This only affects the map the first time it is provided — subsequent changes
   * to this prop are ignored. It does not lock or constrain the map in any way;
   * the user can still freely pan, zoom, and interact with the map after it loads.
   */
  initialRegion?: Region | undefined;
  mapPadding?: Padding | undefined;
  markers?: Marker[] | undefined;
  onMapClick?: ((event: {nativeEvent: MapClickEvent}) => void) | undefined;
  onMarkerClick?: // eslint-disable-next-line no-restricted-syntax
    ((event: {nativeEvent: Omit<Marker, 'icon'>}) => void) | undefined;
  pitchEnabled?: boolean | undefined;
  ref?: React.RefObject<MapViewNativeFunctions | null> | undefined;
  rotateEnabled?: boolean | undefined;
  scrollEnabled?: boolean | undefined;
  showCompass?: boolean | undefined;
  zoomEnabled?: boolean | undefined;
}

export interface MapViewNativeFunctions {
  setRegion: (options: SetRegionOptions) => Promise<void>;
  fitToCoordinates: (options: FitToCoordinatesOptions) => Promise<void>;
}

export interface LatLng {
  latitude: number;
  longitude: number;
}

export interface Region extends LatLng {
  latitudeDelta: number;
  longitudeDelta: number;
}

export interface SetRegionOptions {
  region: Region;
  /**
   * Duration of the animation in milliseconds.
   * If not set, or set to 0, the map will not animate and will jump to the new region.
   */
  animateDuration?: number;
}

export interface Padding {
  top?: number;
  bottom?: number;
  left?: number;
  right?: number;
}

export interface FitToCoordinatesOptions {
  coordinates: LatLng[];
  /**
   * Padding (in points) between the map view edges and the nearest coordinates.
   *
   * For example, setting `right: 40` ensures that the easternmost coordinate
   * will be at least 40 points away from the right edge of the map view.
   * Similarly, `top: 100` ensures the northernmost coordinate is at least
   * 100 points from the top edge.
   *
   * Note that padding only defines a minimum distance. The map fits all
   * coordinates within the padded area, so a coordinate on one axis may
   * already be further from an edge than the padding requires. For example,
   * if the northernmost coordinate is the reason the map can't zoom in
   * further, adding `right` padding won't change the region unless a
   * coordinate is actually closer to the right edge than the specified value.
   */
  padding?: Padding;
  /**
   * Duration of the animation in milliseconds.
   * If not set, or set to 0, the map will not animate and will jump to the new region.
   */
  animateDuration?: number;
}

export interface Point {
  x: number;
  y: number;
}

export interface MapClickEvent {
  coordinate: LatLng;
  /**
   * The point on screen where the click occurred,
   * relative to the map view — not the entire screen.
   */
  point: Point;
}

export interface Marker {
  /**
   * A unique identifier for this marker. Must be unique across all markers.
   */
  id: string;
  coordinate: LatLng;
  // eslint-disable-next-line @typescript-eslint/no-deprecated -- SharedRefType should be replaced with SharedRef but causes incompatibility with expo-image.
  icon: SharedRefType<'image'>;
}
