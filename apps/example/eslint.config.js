import {reactConfig} from '@monholm/eslint-config/react';
import {defineConfig} from 'eslint/config';

export default defineConfig([
  reactConfig,
  {languageOptions: {parserOptions: {projectService: true}}},
]);
