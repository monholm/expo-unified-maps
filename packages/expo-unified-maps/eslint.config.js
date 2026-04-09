import {reactConfig} from '@monholm/eslint-config/react';
import {defineConfig, globalIgnores} from 'eslint/config';

export default defineConfig([
  globalIgnores(['dist', 'plugin/dist']),
  reactConfig,
  {languageOptions: {parserOptions: {projectService: true}}},
]);
