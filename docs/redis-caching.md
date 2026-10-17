# Redis caching in PeerForge

## What is cached

PeerForge uses Spring's cache annotations with Redis as the shared cache store:

- Approved mentor cards and public mentor details: 3-minute time-to-live.
- Landing-page statistics and featured reviews: 5-minute time-to-live.

On a cache miss, the service loads the response from PostgreSQL and stores it in Redis. A cache hit returns the stored response without repeating those queries. Successful mentor approval/rejection/profile edits, review approval, and session completion evict the affected entries. Availability, booking checks, payments, dashboards, and authenticated/private responses are not cached.

Caching is **disabled by default**. This keeps local and production startup independent from Redis until it is deliberately configured.

## Run Redis locally

Docker Compose starts a disposable Redis instance bound to `127.0.0.1` only:

```powershell
docker compose -f compose.redis.yml up -d
```

Set these variables in the environment used to launch the Spring Boot app (for IntelliJ, put them in the run configuration):

```text
PEERFORGE_CACHE_ENABLED=true
REDIS_HOST=localhost
REDIS_PORT=6379
```

Then start the backend as usual. Keep the existing PostgreSQL, JWT, and Razorpay environment variables configured as before. To turn caching off, set `PEERFORGE_CACHE_ENABLED=false` or remove it and restart the app.

## Observe the cache

Call a public endpoint twice, such as the mentor listing or landing stats. The first call should populate Redis; the next call should be served from the cache until expiry or eviction. Inspect keys and TTLs locally with:

```powershell
docker compose -f compose.redis.yml exec redis redis-cli --scan
```

For a listed key, inspect its remaining time-to-live with `TTL <key>` in `redis-cli`.

## Production configuration

Only enable the cache after a Redis endpoint is provisioned and reachable from EC2. Set `PEERFORGE_CACHE_ENABLED=true`, `REDIS_HOST`, `REDIS_PORT`, and (when Redis requires authentication) `REDIS_PASSWORD` in the service environment. Keep Redis private to the application network; do not expose port 6379 publicly. The current EC2 deployment has not been configured or changed for Redis.
