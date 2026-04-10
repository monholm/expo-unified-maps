import {MapView} from 'expo-unified-maps';
import {useState} from 'react';
import {StyleSheet, Switch, Text, View} from 'react-native';

export default function GestureControls() {
  const [zoomEnabled, setZoomEnabled] = useState(true);
  const [scrollEnabled, setScrollEnabled] = useState(true);
  const [rotateEnabled, setRotateEnabled] = useState(true);
  const [pitchEnabled, setPitchEnabled] = useState(true);

  return (
    <View style={styles.container}>
      <MapView
        style={{flex: 1}}
        zoomEnabled={zoomEnabled}
        scrollEnabled={scrollEnabled}
        rotateEnabled={rotateEnabled}
        pitchEnabled={pitchEnabled}
      />
      <View style={styles.overlay}>
        <View style={styles.row}>
          <Text>Zoom</Text>
          <Switch value={zoomEnabled} onValueChange={setZoomEnabled} />
        </View>
        <View style={styles.row}>
          <Text>Scroll</Text>
          <Switch value={scrollEnabled} onValueChange={setScrollEnabled} />
        </View>
        <View style={styles.row}>
          <Text>Rotate</Text>
          <Switch value={rotateEnabled} onValueChange={setRotateEnabled} />
        </View>
        <View style={styles.row}>
          <Text>Pitch</Text>
          <Switch value={pitchEnabled} onValueChange={setPitchEnabled} />
        </View>
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
