import { test as setup, expect } from '@playwright/test';
import fs from 'node:fs';

setup('authenticate', async ({ request }) => {
  const res = await request.post('/auth/login', {
    data: { email: 'admin@orderzone.net', password: 'secret' },
  });
  expect(res.ok()).toBeTruthy();
  const { access_token } = await res.json();
  fs.mkdirSync('.auth', { recursive: true });
  fs.writeFileSync('.auth/token.json', JSON.stringify({ access_token }));
});
