# Service Boundaries

Service boundaries keep SmartLinks changes scoped.

- Redirect resolution belongs in the application service.
- Cache storage belongs behind the Redis integration.
- Deployment concerns belong in manifests and operational docs.

Review cadence: update when the related workflow changes.
