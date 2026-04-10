import {useState} from 'react';
import {
  FlatList,
  Pressable,
  SafeAreaView,
  StyleSheet,
  Text,
} from 'react-native';
import Main from './screens/Main';
import GestureControls from './screens/GestureControls';
import MapPadding from './screens/MapPadding';

const screens = [
  {key: 'main', title: 'Main', component: Main},
  {
    key: 'gesture-controls',
    title: 'Gesture Controls',
    component: GestureControls,
  },
  {key: 'map-padding', title: 'Map Padding', component: MapPadding},
] as const;

export default function App() {
  const [activeScreen, setActiveScreen] = useState<string | null>(null);

  const Screen = screens.find(s => s.key === activeScreen)?.component;

  if (Screen !== undefined) {
    return (
      // eslint-disable-next-line @typescript-eslint/no-deprecated
      <SafeAreaView style={styles.container}>
        <Pressable
          style={styles.backButton}
          onPress={() => {
            setActiveScreen(null);
          }}>
          <Text style={styles.backText}>Back</Text>
        </Pressable>
        <Screen />
      </SafeAreaView>
    );
  }

  return (
    // eslint-disable-next-line @typescript-eslint/no-deprecated
    <SafeAreaView style={styles.container}>
      <FlatList
        data={screens}
        contentContainerStyle={styles.list}
        renderItem={({item}) => (
          <Pressable
            style={styles.item}
            onPress={() => {
              setActiveScreen(item.key);
            }}>
            <Text style={styles.itemText}>{item.title}</Text>
          </Pressable>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1},
  list: {padding: 12, gap: 8},
  item: {padding: 16, backgroundColor: 'white', borderRadius: 8},
  itemText: {fontSize: 16},
  backButton: {padding: 12},
  backText: {fontSize: 16, color: '#007AFF'},
});
