# Performance Budget

Redirect latency should stay low because users hit it on navigation paths.

- Keep cache lookups bounded.
- Avoid blocking calls in redirect decisions.
- Review p95 latency after routing rule changes.

Review cadence: update when the related workflow changes.
