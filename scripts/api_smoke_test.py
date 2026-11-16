#!/usr/bin/env python3
"""Exercise a running coursework API. Use only a disposable/test database.
Run: python scripts/api_smoke_test.py
Optional env: API_BASE_URL, ADMIN_EMAIL, ADMIN_PASSWORD. No tokens are printed.
"""
import getpass
import json
import os
import urllib.error
import urllib.request
import uuid

BASE = os.environ.get('API_BASE_URL', 'http://localhost:8080/api').rstrip('/')

def request(method, path, data=None, token=None, expected=200, key=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if key:
        headers['Idempotency-Key'] = key
    req = urllib.request.Request(BASE + path, data=None if data is None else json.dumps(data).encode(), headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            status, raw = r.status, r.read()
    except urllib.error.HTTPError as e:
        status, raw = e.code, e.read()
    if status != expected:
        raise AssertionError(f'{method} {path}: expected {expected}, received {status}: {raw.decode()[:250]}')
    return json.loads(raw) if raw else None

def run():
    suffix = uuid.uuid4().hex[:10]
    email = f'smoke-{suffix}@example.test'
    password = uuid.uuid4().hex + 'Aa1!'
    admin_email = os.environ.get('ADMIN_EMAIL', 'admin@ceylon.test')
    admin_password = os.environ.get('ADMIN_PASSWORD') or getpass.getpass('Administrator password: ')
    request('GET', '/health')
    admin = request('POST', '/auth/login', {'email': admin_email, 'password': admin_password})['accessToken']
    u = request('POST', '/auth/register', {'name': 'Smoke Test Customer', 'email': email, 'password': password}, expected=201)
    assert u['role'] == 'CUSTOMER' and 'passwordHash' not in u
    token = request('POST', '/auth/login', {'email': email, 'password': password})['accessToken']
    request('GET', '/cart', expected=401)
    request('POST', '/categories', {'name': 'Denied'}, token, expected=403)
    category = request('POST', '/categories', {'name': 'Smoke ' + suffix}, admin, 201)
    food_data = {'name': 'Smoke test rice', 'description': 'Fictional test item', 'price': 800, 'categoryId': category['id'], 'image': 'photo-1512058564366-18510be2db19', 'vegetarian': True, 'status': 'AVAILABLE'}
    food = request('POST', '/foods', food_data, admin, 201)
    food_data['price'] = 900
    request('PUT', '/foods/' + food['id'], food_data, admin)
    cart = request('POST', '/cart/items', {'foodId': food['id'], 'quantity': 2}, token)
    assert cart['total'] == 2050
    request('PUT', '/cart/items/' + food['id'], {'quantity': 3}, token)
    request('PUT', '/cart/items/' + food['id'], {'quantity': 0}, token, 400)
    # A fresh access token must see the same persisted cart.
    token = request('POST', '/auth/login', {'email': email, 'password': password})['accessToken']
    assert request('GET', '/cart', token=token)['total'] == 2950
    key = str(uuid.uuid4())
    checkout = {'address': '10 Fictional Test Street', 'phone': '0700000000', 'paymentMethod': 'CASH_ON_DELIVERY'}
    order = request('POST', '/orders', checkout, token, 201, key)
    assert order['total'] == 2950 and order['paymentStatus'] == 'PENDING'
    repeated = request('POST', '/orders', checkout, token, 201, key)
    assert repeated['id'] == order['id']
    assert not request('GET', '/cart', token=token)['items']
    request('PUT', '/orders/' + order['id'] + '/status', {'status': 'DELIVERED'}, admin, 409)
    for status in ('PREPARING', 'DELIVERED'):
        request('PUT', '/orders/' + order['id'] + '/status', {'status': status}, admin)
    request('PUT', '/payments/' + order['paymentId'] + '/status', {'status': 'COMPLETED'}, token, 403)
    request('PUT', '/payments/' + order['paymentId'] + '/status', {'status': 'COMPLETED'}, admin)
    assert request('GET', '/orders/' + order['id'], token=token)['paymentStatus'] == 'COMPLETED'
    food_data['price'] = 1200
    request('PUT', '/foods/' + food['id'], food_data, admin)
    assert request('GET', '/orders/' + order['id'], token=token)['items'][0]['unitPrice'] == 900
    request('DELETE', '/foods/' + food['id'], token=admin, expected=204)
    request('GET', '/foods/' + food['id'], expected=404)
    request('PUT', '/users/me', {'name': 'Updated Smoke Customer'}, token)
    print('PASS: real HTTP auth, permissions, catalog CRUD, persistent cart, checkout, idempotency, statuses, cash payment, historical prices and profile.')
    return order['id']

if __name__ == '__main__':
    run()
