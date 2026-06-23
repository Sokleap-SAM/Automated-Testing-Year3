import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  reporter: [
    ['line'],
    ['allure-playwright', {
      resultsDir: 'allure-results',
      detail: true,
      environmentInfo: {
        API: 'http://localhost:3000',
        framework: 'NestJS',
        node: process.version,
      },
    }],
  ],
  use: {
    baseURL: 'http://localhost:3000',
    extraHTTPHeaders: {
      'Accept': 'application/json',
    },
  },
  webServer: {
    command: 'npm run start:dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'api',
      dependencies: ['setup'],
    },
  ],
});
