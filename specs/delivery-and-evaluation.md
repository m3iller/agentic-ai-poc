# Delivery, Architecture Constraints & Evaluation Criteria

These items from the source doc are process/quality constraints and submission requirements
rather than product features — captured here instead of under `features/` so they aren't lost.

## Architecture constraints (apply across all features)
- **Clean Architecture**: separation of concerns, independence of components. Specifically:
  - **Data layer**: a specialized data access layer managing persistence-store interactions,
    underlying the API controllers.
  - **Business logic layer**: encapsulates domain rules and validation, architecturally
    independent from both the API and the data access layer.
- **TDD** is the preferred methodology; thorough unit test coverage is required for every core
  component.
- **Database**: a relational database (or equivalent) with a primary entity
  ([pokemon](./models/pokemon.md)) and a secondary collection for user management
  ([user](./models/user.md)); every record needs a unique primary key and at least two
  descriptive attributes.
- **API**: standard HTTP verbs, required parameters, and consistent return structures across
  all CRUD endpoints.

## Mandatory technical requirements
- Host the code in a public Git repository.
- Include tests.
- Proper error handling.
- A frontend that consumes the API — see [frontend-client](./features/frontend-client.md).

## Nice to have
- Caching layer — see [response-caching](./features/response-caching.md).
- Any additional functionality beyond what's specified.

## Delivery
- A comprehensive README: environment setup and technical documentation.
- The application pre-populated with seeded data or mock credentials for demonstration.
- A Dockerfile for containerized execution.

## GenAI tools exercise (separate deliverable, not a product feature)
As part of the interview submission, produce a write-up — independent of the Pokemon
application — demonstrating GenAI tool fluency:
- Scenario: a RESTful API for a simple task management system (own choice of language) with
  CRUD on tasks; each task has `title`, `description`, `status`, `due_date`; tasks belong to a
  user (a basic `User` model is assumed to already exist for this scenario — distinct from this
  project's own [user](./models/user.md) model).
- Deliverables: the prompt used to generate the scaffold/implementation, representative output
  code, and a description of how AI suggestions were validated, corrected/improved, and how
  edge cases/authentication/validation were handled.

## Presentation & code review
- Present to the interview panel (Google Meet or Zoom), screen-sharing the repo or IDE: user
  story, design choices, technical architecture, and a live functionality demo.
- Panel evaluates: Clean Architecture adherence, test coverage/TDD, code quality, functionality
  (bug-free; no browser console warnings is desired), presentation clarity, and GenAI tool
  fluency/critical thinking.

## Open questions
- No explicit deadline or time-box is stated in the source doc.
