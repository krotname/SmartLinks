# Cache Key Inventory

SmartLinks uses cache entries around redirects, metadata lookup, and route decisions.

- Keep key names stable across deploys.
- Include tenant or namespace values where rules may overlap.
- Prefer short TTLs for dynamic routing outcomes.

Review cadence: update when the related workflow changes.
