# Feature: User Registration & Authentication

## Summary
An auxiliary API for user registration, authentication, and management of protected versus
public routes, sitting alongside the Pokemon API.

## User stories
- As a new user, I want to register an account, so that I can access protected functionality.
- As a registered user, I want to authenticate, so that I can access protected routes.
- As the system, I want to distinguish protected routes from public routes, so that only
  authenticated users can reach the ones that require it.

## Acceptance criteria
- Given valid registration details, when a user registers, then a user record is created (see
  [user](../models/user.md)).
- Given valid credentials, when a user authenticates, then the system issues a way to access
  protected routes (mechanism unspecified — see Open questions).
- Given a request to a protected route without valid authentication, when processed, then the
  system rejects it (401/403-style response).
- Given a request to a public route, when processed, then no authentication is required.

## Related models
- [user](../models/user.md)

## Out of scope
- Specific auth mechanism (session, JWT, OAuth) is not mandated by the source doc — left as an
  implementation decision.

## Open questions
- Source doc doesn't specify which Pokemon endpoints are public vs. protected — this needs a
  decision (e.g. read endpoints public, write/modify endpoints protected).
- Password policy, token expiry, and roles/permissions model are unspecified.
