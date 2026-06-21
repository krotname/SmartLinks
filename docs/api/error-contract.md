# API Error Contract

Errors should be stable enough for clients to act on them.

- Use consistent status codes for validation failures.
- Keep messages readable but avoid leaking internals.
- Include correlation identifiers where available.

Review cadence: update when the related workflow changes.
