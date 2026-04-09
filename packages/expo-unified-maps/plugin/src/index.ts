import {type ConfigPlugin, withAndroidManifest} from 'expo/config-plugins';

interface Props {
  googleMapsApiKey: string;
}

const withMapsSdkApiKey: ConfigPlugin<Props> = (config, props) =>
  withAndroidManifest(config, configWithProps => {
    const mainApplication =
      configWithProps.modResults.manifest.application?.[0];

    if (mainApplication !== undefined) {
      mainApplication['meta-data'] ??= [];

      mainApplication['meta-data'].push({
        $: {
          'android:name': 'com.google.android.geo.API_KEY',
          'android:value': props.googleMapsApiKey,
        },
      });
    }

    return configWithProps;
  });

export default withMapsSdkApiKey;
