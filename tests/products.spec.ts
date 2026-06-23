import { test, expect } from '@playwright/test';
import { epic, feature, severity, attachment } from 'allure-js-commons';
import fs from 'node:fs';

interface Product {
  id: number;
  name: string;
  price: number;
}

function authHeader() {
  const { access_token } = JSON.parse(
    fs.readFileSync('.auth/token.json', 'utf-8'),
  ) as {
    access_token: string;
  };
  return { Authorization: `Bearer ${access_token}` };
}

test('GET /products returns a list', async ({ request }) => {
  await epic('Catalog');
  await feature('Products');
  await severity('normal');

  const res = await test.step('GET /products', () =>
    request.get('/products', { headers: authHeader() }));

  await attachment('response', await res.text(), 'application/json');

  expect(res.status()).toBe(200);
  expect(res.ok()).toBeTruthy();
  const body = (await res.json()) as Product[];
  expect(Array.isArray(body)).toBe(true);
});

test('full CRUD flow: create → read → update → delete → verify 404', async ({
  request,
}) => {
  await epic('Catalog');
  await feature('Products');
  await severity('critical');

  // CREATE
  const created = await test.step('POST /products', async () => {
    const res = await request.post('/products', {
      data: { name: 'Pen', price: 1.5 },
      headers: authHeader(),
    });
    expect(res.status()).toBe(201);
    return res.json() as Promise<Product>;
  });
  expect(created.id).toBeDefined();

  // READ
  await test.step(`GET /products/${created.id}`, async () => {
    const res = await request.get(`/products/${created.id}`, {
      headers: authHeader(),
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toMatchObject({ id: created.id, name: 'Pen' });
  });

  // UPDATE
  await test.step(`PATCH /products/${created.id}`, async () => {
    const res = await request.patch(`/products/${created.id}`, {
      data: { price: 2.0 },
      headers: authHeader(),
    });
    expect(res.status()).toBe(200);
    expect(((await res.json()) as Product).price).toBe(2.0);
  });

  // DELETE
  await test.step(`DELETE /products/${created.id}`, async () => {
    const res = await request.delete(`/products/${created.id}`, {
      headers: authHeader(),
    });
    expect(res.status()).toBe(200);
  });

  // VERIFY 404
  await test.step(`GET /products/${created.id} (expect 404)`, async () => {
    const res = await request.get(`/products/${created.id}`, {
      headers: authHeader(),
    });
    await attachment('404 response', await res.text(), 'application/json');
    expect(res.status()).toBe(404);
  });
});

test.describe('Negative tests', () => {
  test('POST /products with missing name returns 400', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('normal');

    const res = await test.step('POST /products with invalid body', () =>
      request.post('/products', {
        data: { price: 10 },
        headers: authHeader(),
      }));

    await attachment('response', await res.text(), 'application/json');

    expect(res.status()).toBe(400);
  });

  test('POST /products with empty body returns 400', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('normal');

    const res = await test.step('POST /products with empty body', () =>
      request.post('/products', {
        data: {},
        headers: authHeader(),
      }));

    expect(res.status()).toBe(400);
  });

  test('GET /products/:id with non-existent id returns 404', async ({
    request,
  }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('normal');

    const res = await test.step('GET /products/99999', () =>
      request.get('/products/99999', { headers: authHeader() }));

    await attachment('response', await res.text(), 'application/json');

    expect(res.status()).toBe(404);
  });
});

test.describe('Products', () => {
  let productId: number;

  test.beforeEach(async ({ request }) => {
    const r = await request.post('/products', {
      data: { name: 'Test Product', price: 10 },
      headers: authHeader(),
    });
    productId = ((await r.json()) as Product).id; // arrange
  });

  test.afterEach(async ({ request }) => {
    if (productId) {
      await request.delete(`/products/${productId}`, { headers: authHeader() }); // clean up
    }
  });

  test('GET reads a product back', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('normal');

    const res = await test.step(`GET /products/${productId}`, () =>
      request.get(`/products/${productId}`, { headers: authHeader() }));

    await attachment('response', await res.text(), 'application/json');

    expect(res.status()).toBe(200);
    expect(await res.json()).toMatchObject({ id: productId });
  });

  test('PATCH updates a product', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('critical');

    const upd = await test.step(`PATCH /products/${productId}`, () =>
      request.patch(`/products/${productId}`, {
        data: { price: 15 },
        headers: authHeader(),
      }));

    await attachment('response', await upd.text(), 'application/json');

    expect(upd.status()).toBe(200);
    expect(((await upd.json()) as Product).price).toBe(15);
  });

  test('DELETE removes a product', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('critical');

    const del = await test.step(`DELETE /products/${productId}`, () =>
      request.delete(`/products/${productId}`, { headers: authHeader() }));
    expect(del.status()).toBe(200);

    const gone =
      await test.step(`GET /products/${productId} (expect 404)`, () =>
        request.get(`/products/${productId}`, { headers: authHeader() }));
    expect(gone.status()).toBe(404);

    productId = 0; // prevent afterEach from deleting again
  });

  test('POST creates a product', async ({ request }) => {
    await epic('Catalog');
    await feature('Products');
    await severity('critical');

    const create = await test.step('POST /products', () =>
      request.post('/products', {
        data: { name: 'Keyboard', price: 49.9 },
        headers: authHeader(),
      }));

    await attachment(
      'create response',
      await create.text(),
      'application/json',
    );

    expect(create.status()).toBe(201);
    const created = (await create.json()) as Product;
    expect(created).toMatchObject({ name: 'Keyboard', price: 49.9 });
    expect(created.id).toBeDefined();

    await request.delete(`/products/${created.id}`, { headers: authHeader() });
  });
});
