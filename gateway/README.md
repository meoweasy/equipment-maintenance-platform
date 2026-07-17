# API Gateway and internal networking

## Default mode

Start the platform with:

```powershell
docker compose up --build -d
```

Only the gateway is published on the host:

- public API: `http://127.0.0.1:8080/api/...`
- gateway health: `http://127.0.0.1:8080/health`

The gateway routes only these public prefixes:

- `/api/equipment` and `/api/equipment/**` to `equipment-service:8081`
- `/api/equipment-types` and `/api/equipment-types/**` to `equipment-service:8081`
- `/api/service-requests` and `/api/service-requests/**` to `maintenance-service:8082`

Requests to `/internal/**` through the gateway return `404`.

The services communicate directly through Docker DNS on the `service-internal`
network:

- `http://equipment-service:8081/internal/...`
- `http://maintenance-service:8082/internal/...`

The service and database networks have `internal: true`. The service and
database containers do not publish host ports in the default mode.

## Local debug mode

To expose service and database ports only on the local machine, apply the dev
override explicitly:

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
```

This additionally publishes:

- equipment service: `127.0.0.1:8081`
- maintenance service: `127.0.0.1:8082`
- equipment PostgreSQL: `127.0.0.1:5433`
- maintenance PostgreSQL: `127.0.0.1:5434`

In dev mode, `/internal/**` is reachable directly from the same machine through
ports 8081 and 8082. Do not use the dev override in a shared or production
environment.

## Security boundary

This setup provides network isolation, not service identity authentication. A
new container explicitly attached to `service-internal` can call internal APIs.
For a larger deployment, use Kubernetes NetworkPolicy, mTLS, or an internal
service credential in addition to the network boundary.