# SmartLinks

[Russian](README.md)


SmartLinks is a high-load URL shortener. It turns long URLs into short identifiers and supports rule-based redirects through Redis-backed routing logic.

## Purpose

The project demonstrates a Java/Spring Boot service with Redis caching, API documentation, Docker/Kubernetes manifests, and tests. The implementation is intended to show scalable link resolution and extensible routing rules.

## Main Functions

- Shorten long URLs into compact identifiers.
- Redirect short links to original URLs.
- Add predicates and rules without rewriting core redirect logic.
- Use Redis for fast access to frequently requested data.
- Expose API documentation through Swagger/OpenAPI.

## Engineering Topics

- SOLID boundaries for routing and predicate logic.
- Design patterns such as Command, Factory, Strategy, and Builder.
- Test coverage target of 90% or higher.
- Testcontainers for Redis and related infrastructure tests.

## Technology Stack

- Java 17
- Spring Boot
- Redis
- Gradle
- Docker and Kubernetes manifests
- Swagger/OpenAPI

## Typical Use Cases

- Marketing campaigns and trackable short links.
- Social media sharing.
- Business integrations through an API.
- Click analytics and reporting.
