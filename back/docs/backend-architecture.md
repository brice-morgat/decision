# Backend Architecture

## Goal

Provide a Spring Boot backend where all poker decision logic remains server-side and evolves independently from the UI.

## Main packages

- `controller`: thin REST endpoints.
- `service`: application use cases and orchestration.
- `service.impl`: concrete service implementations.
- `domain`: business model, split by subdomain.
- `repository`: persistence ports based on Spring Data JPA.
- `dto`: API contracts grouped by feature.
- `mapper`: explicit entity/domain to DTO mapping.
- `config`: technical configuration.
- `exception`: API-safe error model and handlers.

## Main business modules

- `strategyprofile`
- `range`
- `rule`
- `decision`
- `history`
- `referential`

## Design principles

- Controllers never contain poker decision logic.
- DTOs represent transport contracts only.
- Domain objects model poker concepts explicitly.
- Services orchestrate validation, loading and engine execution.
- The current decision engine is intentionally simple and is the extension point for the future rule resolver.
