# Model: User

## Description
The secondary collection for user management, referenced by the source doc's Database
requirement ("...a secondary collection for user management") and by the auxiliary
registration/authentication API.

## Fields
| Field | Type | Required | Notes |
|---|---|---|---|
| id | identifier (PK) | yes | Unique primary key |
| username or email | string | yes | Login identifier — source doc doesn't specify which |
| password | string (hashed) | yes | Never stored/returned in plaintext |
| role(s) | string/collection | no | Needed only if protected-route authorization is role-based (open question) |

Per the source doc's minimum: at least two descriptive attributes beyond the primary key (here:
login identifier + password, at minimum).

## Relationships
- None to `Pokemon` specified in the source doc.

## Used by
- [user-authentication](../features/user-authentication.md)
- [frontend-client](../features/frontend-client.md)

## Open questions
- Whether login is by username or email is unspecified.
- Whether roles/permissions are needed at all, or whether "protected vs. public routes" just
  means "authenticated vs. not," is unspecified.
