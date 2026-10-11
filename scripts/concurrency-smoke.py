#!/usr/bin/env python3
"""Black-box PostgreSQL inventory merge smoke test; requires a running backend."""
import concurrent.futures
import json
import os
import random
import urllib.error
import urllib.request

BASE_URL = os.getenv("DUCK_STORE_URL", "http://localhost:8080").rstrip("/")
REQUESTS = int(os.getenv("CONCURRENCY_REQUESTS", "20"))
if REQUESTS < 2:
    raise SystemExit("CONCURRENCY_REQUESTS must be at least 2")


def request(method, path, payload=None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        f"{BASE_URL}{path}",
        data=data,
        method=method,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as response:
            body = response.read()
            return response.status, json.loads(body) if body else None
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {path} returned HTTP {exc.code}: {detail}") from exc


# Pick a high, random unit price and verify it is not already present before the run.
for _ in range(100):
    price = f"{100000 + random.randrange(0, 10000)}.{random.randrange(0, 100):02d}"
    _, current = request("GET", "/api/ducks")
    existing = [
        duck for duck in current
        if duck["color"] == "Green"
        and duck["size"] == "XSmall"
        and float(duck["price"]) == float(price)
    ]
    if not existing:
        break
else:
    raise SystemExit("Could not find a price not already used by active inventory")

payload = {
    "color": "Green",
    "size": "XSmall",
    "price": float(price),
    "quantity": 1,
}

with concurrent.futures.ThreadPoolExecutor(max_workers=REQUESTS) as pool:
    futures = [pool.submit(request, "POST", "/api/ducks", payload) for _ in range(REQUESTS)]
    responses = [future.result() for future in concurrent.futures.as_completed(futures)]

_, ducks = request("GET", "/api/ducks")
matches = [
    duck for duck in ducks
    if duck["color"] == "Green"
    and duck["size"] == "XSmall"
    and float(duck["price"]) == float(price)
]

if len(matches) != 1:
    raise SystemExit(f"FAIL: expected 1 active matching row, found {len(matches)}")
if matches[0]["quantity"] != REQUESTS:
    raise SystemExit(
        f"FAIL: expected quantity {REQUESTS}, got {matches[0]['quantity']}"
    )

duck_id = matches[0]["id"]
request("DELETE", f"/api/ducks/{duck_id}")
print(f"PASS: {REQUESTS} concurrent creates produced one row with quantity {REQUESTS}.")
print(f"Cleanup: soft-deleted test duck #{duck_id}.")
