# Health Endpoint Contract

Health endpoints should support deployment automation and monitoring.

- Liveness should only prove the process can respond.
- Readiness should include required dependencies.
- Keep expensive checks out of frequent probes.

Review cadence: update when the related workflow changes.
