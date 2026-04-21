import type {ViewProps} from 'react-native';
import type {SharedRefType} from 'expo';

export interface MapViewNativeProps extends ViewProps {
  boundary?: Region | undefined;
  children?: null;
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
   * Padding (in points) between the map view edges and the region edges.
   *
   * When provided, the region is expanded so that each edge has at least
   * this many points of breathing room from the map view edge.
   */
  padding?: Padding;
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
  /**
   * The point within the icon image that is anchored to the marker's
   * geographic coordinate. Expressed as fractions of the icon's size:
   * `x` and `y` are both in the range 0–1, where (0, 0) is the top-left
   * and (1, 1) is the bottom-right of the icon.
   *
   * Defaults to (0.5, 1.0) (bottom-center) which typically works well for pin-shaped icons.
   */
  anchorPoint?: Point;
}
