import {
  MapView,
  type Marker,
  type MapViewNativeFunctions,
} from 'expo-unified-maps';
import type {Except} from 'type-fest';
import {useRef, useState} from 'react';
import {Button, StyleSheet, Switch, Text, View} from 'react-native';
import {useImage} from 'expo-image';

const markers: Except<Marker, 'icon'>[] = [
  {id: 'copenhagen', coordinate: {latitude: 55.6761, longitude: 12.5683}},
  {id: 'aalborg', coordinate: {latitude: 57.0488, longitude: 9.9217}},
  {id: 'aarhus', coordinate: {latitude: 56.1629, longitude: 10.2039}},
];

export default function Main() {
  const mapViewRef = useRef<MapViewNativeFunctions>(null);
  // eslint-disable-next-line @typescript-eslint/no-unsafe-argument, @typescript-eslint/no-require-imports
  const markerIcon = useImage(require('../../assets/tabler-icons/map-pin.png'));
  const [showCompass, setShowCompass] = useState(false);
  const [boundaryEnabled, setBoundaryEnabled] = useState(false);

  return (
    <View style={styles.container}>
      <MapView
        ref={mapViewRef}
        style={{flex: 1}}
        showCompass={showCompass}
        boundary={
          boundaryEnabled ?
            {
              latitude: 55.6761,
              longitude: 12.5683,
              latitudeDelta: 0.3,
              longitudeDelta: 0.3,
            }
          : undefined
        }
        onMarkerClick={({nativeEvent}) => {
          console.log('onMarkerClick', nativeEvent);
        }}
        markers={
          markerIcon === null ? undefined : (
            markers.map(marker => ({...marker, icon: markerIcon}))
          )
        }
      />
      <View style={styles.overlay}>
        <View style={styles.row}>
          <Text>Compass</Text>
          <Switch value={showCompass} onValueChange={setShowCompass} />
        </View>
        <View style={styles.row}>
          <Text>Boundary</Text>
          <Switch value={boundaryEnabled} onValueChange={setBoundaryEnabled} />
        </View>
        <Button
          title='Go to London'
          onPress={() => {
            mapViewRef.current
              ?.setRegion({
                region: {
                  latitude: 51.5074,
                  longitude: -0.1278,
                  latitudeDelta: 0.1,
                  longitudeDelta: 0.1,
                },
                animateDuration: 1000,
              })
              .catch((error: unknown) => {
                console.error('Error setting region:', error);
              });
          }}
        />
        <Button
          title='Fit to Markers'
          onPress={() => {
            mapViewRef.current
              ?.fitToCoordinates({
                coordinates: markers.map(m => m.coordinate),
                padding: {top: 50, right: 50, bottom: 50, left: 50},
                animateDuration: 1000,
              })
              .catch((error: unknown) => {
                console.error('Error fitting to markers:', error);
              });
          }}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1},
  overlay: {
    position: 'absolute',
    bottom: 60,
    left: 12,
    gap: 4,
    backgroundColor: 'white',
    padding: 12,
    borderRadius: 8,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
});
