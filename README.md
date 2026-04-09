# Feed Service

The Feed Service manages social feed data for BiteTogether: posts, comments, and likes.

It is a Spring Boot microservice using MongoDB, OpenFeign (for user/friend data), and Kafka producer support.

## Application Business

### What this service owns

- Post creation, update, delete, and retrieval
- Comment creation (including replies), update, delete, and retrieval
- Like/unlike for posts and comments
- Aggregated counters on posts/comments (`likeCount`, `commentCount`, `repliesCount`)
- New feed retrieval based on friend list + recent time window + current map viewport

### Business behavior implemented

- **Ownership checks**: only the creator can update/delete their own post or comment.
- **Nested comments**: comments can be top-level or replies via `parentCommentId`.
- **Counter maintenance**:
  - Creating/deleting comments updates `Post.commentCount`.
  - Creating/deleting replies updates parent `Comment.repliesCount`.
  - Like/unlike updates `Post.likeCount` or `Comment.likeCount`.
- **New feeds logic**:
  - Calls user-service friend endpoint.
  - Computes viewport bounds from `latitude`, `longitude`, `latitudeDelta`, `longitudeDelta`.
  - Fetches posts from friends within the current map viewport and within the last 1 year.
  - Returns newest first.
- **User context usage**:
  - Current user is resolved via shared `UserContextUtils` (JWT claims or forwarded headers such as `X-User-Id`).

### Integration points

- **User service (OpenFeign)**
  - Resolve user info for response enrichment (`UserDTO`)
  - Fetch current friend list for new feed
- **Kafka (producer available)**
  - `NotificationProducer` can send `NotificationEvent` to topic `notification-events`
  - Producer exists in codebase, but no direct invocation from current post/comment/like service flows

## Project Structure

```text
feed-service/
  src/main/java/com/bitetogether/feed/
    configuration/
      mongodb/          # auditing setup
      openfeign/        # auth token forwarding for Feign calls
      security/         # current HTTP security config
    controller/         # REST APIs (Post, Comment, Like)
    dto/
      request/          # incoming API payloads
      response/         # outgoing API payloads
    mapper/             # MapStruct mappers entity <-> DTO
    model/              # MongoDB documents (Post, Comment, Like)
    repository/         # Mongo repositories + UserClient Feign interface
    service/
      impl/             # business logic implementations
      inter/            # service interfaces
      NotificationProducer.java
  src/main/resources/
    application.yml
    application-database.yml
    application-openfeign.yml
    application-openapi.yml
    application-kafka.yml
```

## API Details

Base path: `/api/v1/feeds`

Default pagination values:
- `page=0`
- `size=10`

### Request headers (important)

For write operations and personalization fields (`alreadyLiked`), requests should include user context (typically forwarded by gateway):

- `X-User-Id`
- `X-User-Role`
- `X-User-Email`
- `X-Username`
- `Authorization: Bearer <token>` (also forwarded to user-service by Feign interceptor when present)

### Response wrapper

Controllers return common wrapper types:

- Single payload: `ApiResponse<T>` (status, message, data)
- Paginated payload: `ApiResponsePagination<T>` (status, message, data, currentPage, totalPages, totalElements)

### Post APIs

| Method | Endpoint | Description | Body |
|---|---|---|---|
| `POST` | `/api/v1/feeds` | Create a post | `PostRequest` |
| `GET` | `/api/v1/feeds/{id}` | Get post by id | - |
| `GET` | `/api/v1/feeds/user/{userId}` | Get posts by user (paged) | - |
| `GET` | `/api/v1/feeds/new-feeds` | Get latest friend feed from current map viewport (paged) | - |
| `PUT` | `/api/v1/feeds/{id}` | Update post (owner only) | `PostRequest` |
| `DELETE` | `/api/v1/feeds/{id}` | Delete post (owner only) | - |
| `GET` | `/api/v1/feeds/test` | Health/test endpoint | - |

`PostRequest` fields:
- `placeId` (Long)
- `content` (String)
- `rating` (Integer)
- `photoUrl` (String)
- `latitude` (Double)
- `longitude` (Double)

`PostResponse` key fields:
- `id`, `placeId`, `content`, `rating`, `photoUrl`, `latitude`, `longitude`
- `likeCount`, `commentCount`, `alreadyLiked`
- `user` (`UserDTO`)
- audit fields from `BaseResponse` (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`)

`GET /api/v1/feeds/new-feeds` query params:
- `page` (Integer, default `0`)
- `size` (Integer, default `10`)
- `latitude` (Double, required)
- `longitude` (Double, required)
- `latitudeDelta` (Double, required)
- `longitudeDelta` (Double, required)

### Comment APIs

Base path: `/api/v1/feeds/comments`

| Method | Endpoint | Description | Body |
|---|---|---|---|
| `POST` | `/api/v1/feeds/comments` | Create comment/reply | `CommentRequest` |
| `GET` | `/api/v1/feeds/comments/{commentId}` | Get comment by id | - |
| `GET` | `/api/v1/feeds/comments/post/{postId}` | Get top-level comments by post (paged) | - |
| `GET` | `/api/v1/feeds/comments/user/{userId}` | Get comments by user (paged) | - |
| `GET` | `/api/v1/feeds/comments/{commentId}/replies` | Get direct replies | - |
| `PUT` | `/api/v1/feeds/comments/{commentId}` | Update comment (owner only) | `CommentRequest` |
| `DELETE` | `/api/v1/feeds/comments/{commentId}` | Delete comment (owner only) | - |

`CommentRequest` fields:
- `postId` (String)
- `content` (String)
- `parentCommentId` (String, optional for replies)

`CommentResponse` key fields:
- `id`, `postId`, `content`, `parentCommentId`
- `likeCount`, `repliesCount`, `alreadyLiked`
- `user` (`UserDTO`)
- audit fields from `BaseResponse`

### Like APIs

Base path: `/api/v1/feeds/likes`

| Method | Endpoint | Description | Body |
|---|---|---|---|
| `POST` | `/api/v1/feeds/likes` | Like post or comment | `LikeRequest` |
| `DELETE` | `/api/v1/feeds/likes` | Unlike post or comment | `LikeRequest` |
| `GET` | `/api/v1/feeds/likes/user/{userId}` | Get likes by user (paged) | - |
| `GET` | `/api/v1/feeds/likes/post/{postId}` | Get likes by post (paged) | - |
| `GET` | `/api/v1/feeds/likes/comment/{commentId}` | Get likes by comment (paged) | - |

`LikeRequest` rules:
- At least one of `postId` or `commentId` is required.
- If `commentId` is provided, request is treated as comment-like target.

`LikeResponse` key fields:
- `id`, `postId`, `commentId`
- `user` (`UserDTO`)
- audit fields from `BaseResponse`

## Configuration

Spring profiles included by default in `application.yml`:
- `security`
- `openapi`
- `database`
- `openfeign`
- `kafka`

Key environment variables:
- `SERVER_PORT`
- `SPRING_DATA_MONGODB_URI`
- `USER_SERVICE_URL` (default: `http://localhost:8081`)
- OpenAPI metadata vars (`API_TITLE`, `API_DESCRIPTION`, `API_VERSION`, ...)
