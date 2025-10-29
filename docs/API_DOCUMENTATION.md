# NoteKeeper API Documentation

## Overview

NoteKeeper is a collaborative note-taking application built with Spring Boot that allows users to create, organize, and share notes within workspaces. The application supports multiple users with role-based permissions and provides a RESTful API for all operations.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently, the API does not implement authentication. All endpoints are publicly accessible.

## Core Entities

### User

- **id**: String (UUID)
- **username**: String
- **email**: String
- **firstName**: String
- **lastName**: String
- **createdAt**: DateTime
- **updatedAt**: DateTime

### Workspace

- **id**: String (UUID)
- **name**: String
- **description**: String (optional)
- **icon**: String (emoji)
- **ownerId**: String (UUID)
- **isDefault**: Boolean
- **createdAt**: DateTime

### Page (Note)

- **id**: String (UUID)
- **title**: String
- **content**: String (markdown)
- **workspaceId**: String (UUID)
- **createdBy**: String (UUID)
- **createdAt**: DateTime
- **updatedAt**: DateTime

### Tag

- **id**: String (UUID)
- **name**: String
- **color**: String (hex)
- **workspaceId**: String (UUID)

### Workspace Member

- **id**: String (UUID)
- **workspaceId**: String (UUID)
- **userId**: String (UUID)
- **role**: String (OWNER, EDITOR, VIEWER)
- **joinedAt**: DateTime

## API Endpoints

### User Management

#### Get All Users

```http
GET /users
```

#### Get User by ID

```http
GET /users/{id}
```

#### Create User

```http
POST /users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Update User

```http
PUT /users/{id}
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john.updated@example.com",
  "firstName": "John",
  "lastName": "Doe Updated"
}
```

#### Delete User

```http
DELETE /users/{id}
```

### Workspace Management

#### Get All Workspaces

```http
GET /workspaces
```

#### Get Workspace by ID

```http
GET /workspaces/{id}
```

#### Create Workspace

```http
POST /workspaces
Content-Type: application/json

{
  "name": "My Workspace",
  "description": "A workspace for my notes",
  "icon": "📝"
}
```

#### Update Workspace

```http
PUT /workspaces/{id}
Content-Type: application/json

{
  "name": "Updated Workspace",
  "description": "Updated description",
  "icon": "📚"
}
```

#### Delete Workspace

```http
DELETE /workspaces/{id}
```

### Workspace Members

#### Get Workspace Members

```http
GET /workspaces/{workspaceId}/members
```

#### Add Member to Workspace

```http
POST /workspaces/{workspaceId}/members?userId={userId}&role={role}
```

**Roles:** OWNER, EDITOR, VIEWER

#### Update Member Role

```http
PUT /workspaces/{workspaceId}/members/{userId}/role?role={newRole}
```

#### Remove Member from Workspace

```http
DELETE /workspaces/{workspaceId}/members/{userId}
```

#### Check Membership

```http
GET /workspaces/{workspaceId}/members/check/{userId}
```

Returns: `true` or `false`

### Page Management

#### Get All Pages

```http
GET /pages
```

#### Get Pages by Workspace

```http
GET /pages?workspaceId={workspaceId}
```

#### Get Page by ID

```http
GET /pages/{id}
```

#### Create Page

```http
POST /pages
Content-Type: application/json

{
  "title": "My Note",
  "content": "# My Note Content\n\nThis is a markdown note.",
  "workspaceId": "workspace-uuid"
}
```

#### Update Page

```http
PUT /pages/{id}
Content-Type: application/json

{
  "title": "Updated Note",
  "content": "# Updated Content\n\nNew content here."
}
```

#### Delete Page

```http
DELETE /pages/{id}
```

### Tag Management

#### Get All Tags

```http
GET /tags
```

#### Get Tags by Workspace

```http
GET /tags?workspaceId={workspaceId}
```

#### Get Tag by ID

```http
GET /tags/{id}
```

#### Create Tag

```http
POST /tags
Content-Type: application/json

{
  "name": "Important",
  "color": "#FF0000",
  "workspaceId": "workspace-uuid"
}
```

#### Update Tag

```http
PUT /tags/{id}
Content-Type: application/json

{
  "name": "Very Important",
  "color": "#FF0000"
}
```

#### Delete Tag

```http
DELETE /tags/{id}
```

### Location Management

#### Get All Locations

```http
GET /locations
```

#### Get Location by ID

```http
GET /locations/{id}
```

#### Create Location

```http
POST /locations
Content-Type: application/json

{
  "name": "New York",
  "country": "USA"
}
```

#### Update Location

```http
PUT /locations/{id}
Content-Type: application/json

{
  "name": "New York City",
  "country": "USA"
}
```

#### Delete Location

```http
DELETE /locations/{id}
```

## Response Formats

### Success Response

```json
{
  "status": "success",
  "data": { ... },
  "message": "Operation completed successfully"
}
```

### Error Response

```json
{
  "status": "error",
  "message": "Error description",
  "timestamp": "2025-01-01T12:00:00Z"
}
```

## HTTP Status Codes

- **200 OK**: Successful GET request
- **201 Created**: Successful POST request
- **204 No Content**: Successful DELETE request
- **400 Bad Request**: Invalid request data
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

## Data Validation

### User Validation

- Username: Required, unique, 3-50 characters
- Email: Required, valid email format, unique
- First Name: Required, 1-50 characters
- Last Name: Required, 1-50 characters

### Workspace Validation

- Name: Required, 1-100 characters
- Description: Optional, max 500 characters
- Icon: Optional, emoji character

### Page Validation

- Title: Required, 1-200 characters
- Content: Required, markdown format
- Workspace ID: Required, valid UUID

### Tag Validation

- Name: Required, 1-50 characters, unique per workspace
- Color: Required, valid hex color (#RRGGBB)
- Workspace ID: Required, valid UUID

## Rate Limiting

Currently not implemented.

## Versioning

API version is not specified in URLs. Future versions will use URL versioning (e.g., `/api/v2/`).

## Error Handling

All endpoints return consistent error responses with appropriate HTTP status codes and descriptive messages.

## Testing

Use tools like Postman or curl to test the API endpoints. The application runs on `http://localhost:8080` by default.
