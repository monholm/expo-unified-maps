import {MapView} from 'expo-unified-maps';
import {useState} from 'react';
import {StyleSheet, Text, useWindowDimensions, View} from 'react-native';
import Slider from '@react-native-community/slider';

export default function MapPadding() {
  const {width, height} = useWindowDimensions();
  const [top, setTop] = useState(0);
  const [bottom, setBottom] = useState(0);
  const [left, setLeft] = useState(0);
  const [right, setRight] = useState(0);
  return (
    <View style={styles.container}>
      <MapView style={{flex: 1}} mapPadding={{top, bottom, left, right}} />
      <View style={styles.overlay}>
        <Text>Top: {Math.round(top)}</Text>
        <Slider
          minimumValue={0}
          maximumValue={height}
          value={top}
          onValueChange={setTop}
        />
        <Text>Bottom: {Math.round(bottom)}</Text>
        <Slider
          minimumValue={0}
          maximumValue={height}
          value={bottom}
          onValueChange={setBottom}
        />
        <Text>Left: {Math.round(left)}</Text>
        <Slider
          minimumValue={0}
          maximumValue={width}
          value={left}
          onValueChange={setLeft}
        />
        <Text>Right: {Math.round(right)}</Text>
        <Slider
          minimumValue={0}
          maximumValue={width}
          value={right}
          onValueChange={setRight}
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
    right: 12,
    backgroundColor: 'white',
    padding: 12,
    borderRadius: 8,
  },
});
