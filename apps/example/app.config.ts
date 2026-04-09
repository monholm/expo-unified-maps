import type {ExpoConfig} from 'expo/config';
// eslint-disable-next-line import/no-extraneous-dependencies
import dotenv from 'dotenv';

dotenv.config({path: './.envs/.env'});

export default (): ExpoConfig => ({
  name: 'expo-unified-maps-example',
  slug: 'expo-unified-maps-example',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/icon.png',
  userInterfaceStyle: 'light',
  splash: {
    image: './assets/splash-icon.png',
    resizeMode: 'contain',
    backgroundColor: '#ffffff',
  },
  ios: {
    supportsTablet: true,
    bundleIdentifier: 'com.anonymous.expo-unified-maps-example',
  },
  android: {
    package: 'com.anonymous.expounifiedmapsexample',
    adaptiveIcon: {
      foregroundImage: './assets/adaptive-icon.png',
      backgroundColor: '#ffffff',
    },
    predictiveBackGestureEnabled: false,
  },
  web: {favicon: './assets/favicon.png'},
  plugins: [
    ['expo-unified-maps', {googleMapsApiKey: process.env.MAPS_API_KEY}],
  ],
});
