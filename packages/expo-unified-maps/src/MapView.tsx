import {requireNativeView} from 'expo';
import type {ComponentType} from 'react';
import type {MapViewNativeProps} from './MapView.types.js';

const NativeView: ComponentType<MapViewNativeProps> =
  requireNativeView('MapView');

export function MapView(props: MapViewNativeProps) {
  const _markers = props.markers?.map(marker => ({
    ...marker,
    // @ts-expect-error -- __expo_shared_object_id__ https://github.com/expo/expo/issues/40985
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    icon: marker.icon.__expo_shared_object_id__,
  }));

  return <NativeView {...props} markers={_markers} />;
}
