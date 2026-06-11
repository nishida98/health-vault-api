# HealthVault API Endpoints

This document is the integration reference for API clients and LLM agents.
Every endpoint change must be reflected here in the same pull request.

## Base URL

Local development:

```text
http://localhost:8080
```

## Response Contract

Responses with a body are wrapped in a `data` property:

```json
{
  "data": {}
}
```

Error responses use `errorMessage`:

```json
{
  "errorMessage": "User account not found."
}
```

HTTP status codes without response body, such as `204 No Content`, do not use the `data` wrapper.

## Common Status Codes

- `200 OK`: successful read/update/login/validation with response body.
- `201 Created`: resource created with response body.
- `204 No Content`: resource deleted successfully.
- `400 Bad Request`: invalid path parameter or request body.
- `401 Unauthorized`: invalid credentials or invalid/expired token.
- `404 Not Found`: user, folder, or exam not found.
- `409 Conflict`: duplicated email.

## HealthVault Overview

### Get API Overview

```http
GET /api/v1/healthvault
```

Response:

```json
{
  "data": {
    "name": "HealthVault API",
    "description": "Backend API for centralized and organized patient medical records.",
    "status": "INITIAL_SETUP",
    "capabilities": [
      "Patient account management",
      "Structured medical record storage"
    ]
  }
}
```

## Users

### Create User

```http
POST /api/v1/users
```

Request:

```json
{
  "name": "Jane Doe",
  "nickname": "jane",
  "email": "jane@example.com",
  "password": "strong-password"
}
```

Response `201 Created`:

```json
{
  "data": {
    "id": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
    "name": "Jane Doe",
    "nickname": "jane",
    "email": "jane@example.com",
    "createdAt": "2026-06-11T10:00:00Z",
    "updatedAt": "2026-06-11T10:00:00Z"
  }
}
```

Notes:

- Passwords are never returned.
- Passwords are stored as a hash with a random salt.
- User ids are UUIDv7.

### List Users

```http
GET /api/v1/users
```

Response:

```json
{
  "data": [
    {
      "id": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
      "name": "Jane Doe",
      "nickname": "jane",
      "email": "jane@example.com",
      "createdAt": "2026-06-11T10:00:00Z",
      "updatedAt": "2026-06-11T10:00:00Z"
    }
  ]
}
```

### Get User By Id

```http
GET /api/v1/users/{id}
```

### Replace User

```http
PUT /api/v1/users/{id}
```

Request body is the same as `POST /api/v1/users`. All fields are required.

### Partially Update User

```http
PATCH /api/v1/users/{id}
```

Request:

```json
{
  "name": "Jane Updated",
  "nickname": "jane-updated",
  "email": "jane.updated@example.com",
  "password": "new-strong-password"
}
```

All fields are optional. Sent text fields must not be blank.

### Delete User

```http
DELETE /api/v1/users/{id}
```

Response:

```http
204 No Content
```

## Authentication

### Login

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "jane@example.com",
  "password": "strong-password"
}
```

Response:

```json
{
  "data": {
    "token": "jwt-token",
    "tokenType": "Bearer",
    "expiresAt": "2026-06-11T11:00:00Z",
    "user": {
      "id": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
      "name": "Jane Doe",
      "nickname": "jane",
      "email": "jane@example.com",
      "createdAt": "2026-06-11T10:00:00Z",
      "updatedAt": "2026-06-11T10:00:00Z"
    }
  }
}
```

### Validate Token

```http
POST /api/v1/auth/validate
```

Request:

```json
{
  "token": "jwt-token"
}
```

Response:

```json
{
  "data": {
    "valid": true,
    "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
    "email": "jane@example.com",
    "expiresAt": "2026-06-11T11:00:00Z"
  }
}
```

## Exam Folders

Folders belong to a user and organize medical exams.

### Create Folder

```http
POST /api/v1/users/{userId}/exam-folders
```

Root folder request:

```json
{
  "name": "Lab Exams",
  "parentId": null
}
```

Subfolder request:

```json
{
  "name": "Blood Tests",
  "parentId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2"
}
```

Response `201 Created`:

```json
{
  "data": {
    "id": "018fd6aa-1f38-7c09-b1f5-89d984ab8dc1",
    "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
    "parentId": null,
    "name": "Lab Exams",
    "depth": 1,
    "createdAt": "2026-06-11T10:00:00Z",
    "updatedAt": "2026-06-11T10:00:00Z"
  }
}
```

Rules:

- `name` is required.
- `parentId` is optional.
- A folder with `parentId = null` is a root folder.
- A folder with `parentId` is a child folder.
- Maximum depth is 3.
- Parent folder must belong to the same user.

### Get Folder Tree

```http
GET /api/v1/users/{userId}/exam-folders/tree
```

Response:

```json
{
  "data": [
    {
      "id": "018fd6aa-1f38-7c09-b1f5-89d984ab8dc1",
      "name": "Lab Exams",
      "depth": 1,
      "parentId": null,
      "exams": [],
      "subfolders": [
        {
          "id": "018fd6aa-60af-7fd1-a2ab-cd66da97360a",
          "name": "Blood Tests",
          "depth": 2,
          "parentId": "018fd6aa-1f38-7c09-b1f5-89d984ab8dc1",
          "exams": [
            {
              "id": "018fd6ab-4ef3-77df-9e37-9f09aafd9d51",
              "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
              "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a",
              "performedAt": "2026-01-10",
              "requestingDoctor": "Dr. Alice Smith",
              "examType": "Blood test",
              "result": "Normal blood count.",
              "createdAt": "2026-06-11T10:00:00Z",
              "updatedAt": "2026-06-11T10:00:00Z"
            }
          ],
          "subfolders": []
        }
      ]
    }
  ]
}
```

Frontend usage:

- Render root items from `data`.
- Render nested folders recursively from `subfolders`.
- Render allocated exams from each folder node's `exams`.

## Medical Exams

Medical exams belong to a user and must be allocated to a folder.

### Create Exam

```http
POST /api/v1/users/{userId}/exams
```

Request:

```json
{
  "performedAt": "2026-01-10",
  "requestingDoctor": "Dr. Alice Smith",
  "examType": "Blood test",
  "result": "Normal blood count.",
  "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a"
}
```

Response `201 Created`:

```json
{
  "data": {
    "id": "018fd6ab-4ef3-77df-9e37-9f09aafd9d51",
    "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
    "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a",
    "performedAt": "2026-01-10",
    "requestingDoctor": "Dr. Alice Smith",
    "examType": "Blood test",
    "result": "Normal blood count.",
    "createdAt": "2026-06-11T10:00:00Z",
    "updatedAt": "2026-06-11T10:00:00Z"
  }
}
```

Rules:

- `performedAt` is required and cannot be in the future.
- `requestingDoctor` is required.
- `examType` is required.
- `result` is required.
- `folderId` is required and must belong to the same user.
- Exam ids are UUIDv7.

### List and Search Exams

```http
GET /api/v1/users/{userId}/exams
```

Optional query parameters:

```http
GET /api/v1/users/{userId}/exams?date=2026-01&doctor=alice&examType=blood
```

Search behavior:

- `date`: partial text match against the ISO date string, such as `2026`, `2026-01`, or `2026-01-10`.
- `doctor`: case-insensitive partial match against `requestingDoctor`.
- `examType`: case-insensitive partial match against `examType`.
- When multiple filters are provided, all must match.

Response:

```json
{
  "data": [
    {
      "id": "018fd6ab-4ef3-77df-9e37-9f09aafd9d51",
      "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
      "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a",
      "performedAt": "2026-01-10",
      "requestingDoctor": "Dr. Alice Smith",
      "examType": "Blood test",
      "result": "Normal blood count.",
      "createdAt": "2026-06-11T10:00:00Z",
      "updatedAt": "2026-06-11T10:00:00Z"
    }
  ]
}
```

### Get Exam By Id

```http
GET /api/v1/users/{userId}/exams/{examId}
```

The exam must belong to the given user.

### Replace Exam

```http
PUT /api/v1/users/{userId}/exams/{examId}
```

Request body is the same as `POST /api/v1/users/{userId}/exams`. All fields are required.

### Partially Update Exam

```http
PATCH /api/v1/users/{userId}/exams/{examId}
```

Request:

```json
{
  "performedAt": "2026-01-11",
  "requestingDoctor": "Dr. Alice Smith",
  "examType": "Blood test",
  "result": "Updated result.",
  "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a"
}
```

All fields are optional. Sent text fields must not be blank.

### Move Exam Between Folders

```http
PATCH /api/v1/users/{userId}/exams/{examId}/folder
```

Request:

```json
{
  "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a"
}
```

Response:

```json
{
  "data": {
    "id": "018fd6ab-4ef3-77df-9e37-9f09aafd9d51",
    "userId": "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2",
    "folderId": "018fd6aa-60af-7fd1-a2ab-cd66da97360a",
    "performedAt": "2026-01-10",
    "requestingDoctor": "Dr. Alice Smith",
    "examType": "Blood test",
    "result": "Normal blood count.",
    "createdAt": "2026-06-11T10:00:00Z",
    "updatedAt": "2026-06-11T10:00:00Z"
  }
}
```

### Delete Exam

```http
DELETE /api/v1/users/{userId}/exams/{examId}
```

Response:

```http
204 No Content
```
