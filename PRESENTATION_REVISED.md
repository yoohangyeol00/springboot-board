# Spring Boot 게시판 프로젝트 발표 문서

> 구현 내용 설명, 면접 질문 대비, 화면 시연 흐름을 한 번에 정리한 문서입니다.

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [패키지 구조와 아키텍처](#3-패키지-구조와-아키텍처)
4. [DB 설계](#4-db-설계)
5. [인증과 보안](#5-인증과-보안)
6. [주요 기능](#6-주요-기능)
7. [API 명세](#7-api-명세)
8. [프론트엔드 구조](#8-프론트엔드-구조)
9. [핵심 설계 결정](#9-핵심-설계-결정)
10. [면접 대비 개념 정리](#10-면접-대비-개념-정리)
11. [예상 질문과 답변](#11-예상-질문과-답변)
12. [발표 시연 흐름](#12-발표-시연-흐름)

---

## 1. 프로젝트 개요

**JWT 인증 기반 풀스택 게시판 애플리케이션**

| 항목 | 내용 |
|---|---|
| 목적 | 회원가입, 로그인, 게시글 CRUD, 댓글/대댓글, 이미지 업로드를 포함한 게시판 구현 |
| 인증 방식 | JWT Access Token + Refresh Token, Refresh Token DB 저장 및 로테이션 |
| 백엔드 | Spring Boot 3.5.0, Java 17, Spring Security, MyBatis, PostgreSQL |
| 프론트엔드 | React 18, TypeScript, Vite, Axios, Toast UI Editor |

### 구현 기능 요약

- 회원: 회원가입, 로그인, 내 정보 조회, 닉네임 수정, 비밀번호 변경, 회원 탈퇴
- 인증: Access Token 인증, Refresh Token 재발급, 로그아웃 시 Refresh Token 폐기
- 게시글: 목록/검색/페이징, 인기글, 상세 조회, 작성, 수정, 삭제
- 댓글: 댓글 작성/수정/삭제, 1단계 대댓글
- 이미지: 에디터 이미지 업로드, 로컬 업로드 파일 정적 서빙, 게시글 삭제 시 본문 이미지 정리
- 예외 처리: `@RestControllerAdvice` 기반 공통 에러 응답

---

## 2. 기술 스택

### 백엔드

| 기술 | 버전 | 역할 |
|---|---:|---|
| Java | 17 | 서버 개발 언어 |
| Spring Boot | 3.5.0 | 애플리케이션 프레임워크 |
| Spring Security | Boot 관리 버전 | 인증/인가, 보안 필터 체인 |
| MyBatis | 3.0.5 starter | SQL Mapper |
| PostgreSQL | Runtime driver | 관계형 DB |
| JJWT | 0.12.6 | JWT 생성/검증 |
| Lombok | Boot 관리 버전 | 반복 코드 감소 |
| Bean Validation | Boot 관리 버전 | 요청 DTO 검증 |

### 프론트엔드

| 기술 | 버전 | 역할 |
|---|---:|---|
| React | 18.2.0 | UI |
| TypeScript | 5.2.0 | 정적 타입 |
| Vite | 5.0.0 | 개발 서버/빌드 |
| React Router | 6.20.0 | 클라이언트 라우팅 |
| Axios | 1.6.0 | HTTP 클라이언트 |
| Toast UI Editor | 3.2.3 | 게시글 에디터 |

---

## 3. 패키지 구조와 아키텍처

### 백엔드 패키지 구조

```text
com.board.backend
├─ board
│  ├─ controller    BoardController
│  ├─ service       BoardService, BoardServiceImpl
│  ├─ mapper        BoardMapper
│  ├─ domain        Board
│  ├─ dto           BoardCreateRequest, BoardUpdateRequest, BoardResponse
│  └─ exception     BoardNotFoundException, BoardCreateFailedException, ...
├─ member
│  ├─ controller    MemberController
│  ├─ service       MemberService, MemberServiceImpl
│  ├─ mapper        MemberMapper, RefreshTokenMapper
│  ├─ domain        Member, RefreshToken
│  └─ dto           MemberLoginRequest, MemberLoginResponse, RefreshTokenRequest, ...
├─ comment
│  ├─ controller    CommentController
│  ├─ service       CommentService, CommentServiceImpl
│  ├─ mapper        CommentMapper
│  ├─ domain        Comment
│  ├─ dto           CommentCreateRequest, CommentUpdateRequest, CommentResponse
│  └─ exception     CommentNotFoundException, CommentCreateFailedException, ...
├─ image
│  ├─ controller    ImageController
│  └─ service       ImageService
└─ global
   ├─ config        SecurityConfig, WebConfig
   ├─ security      JwtTokenProvider, JwtAuthenticationFilter, LoginMember, TokenHashUtil
   ├─ common        PageResponse
   └─ exception     GlobalExceptionHandler, ErrorResponse
```

### 요청 처리 흐름

```text
Client
  -> JwtAuthenticationFilter
  -> Controller
  -> Service
  -> Mapper
  -> PostgreSQL
```

- `Controller`: HTTP 요청/응답 처리, 요청 DTO 검증
- `Service`: 비즈니스 규칙, 소유자 검증, 트랜잭션 단위 처리
- `Mapper`: MyBatis XML과 연결되는 DB 접근 계층
- `GlobalExceptionHandler`: 예외를 HTTP 상태 코드와 공통 에러 응답으로 변환

도메인별로 `board`, `member`, `comment`, `image` 패키지를 분리해 기능 변경의 영향 범위를 줄였습니다.

---

## 4. DB 설계

![ERD](erd.png)

### 관계 요약

```text
members (1) ──< boards (N)
members (1) ──< refresh_tokens (N)
boards  (1) ──< comments (N)
comments (1) ──< comments (N)    self-referencing, 1단계 대댓글
```

### members

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 회원 ID |
| login_id | VARCHAR(50) | 로그인 ID, 활성 회원 기준 unique index |
| password_hash | VARCHAR(255) | BCrypt 기반 비밀번호 해시 |
| nickname | VARCHAR(50) | 닉네임, 활성 회원 기준 unique index |
| role | VARCHAR(20) | 권한, 기본값 `USER` |
| status | VARCHAR(20) | `ACTIVE`, `WITHDRAWN` |
| failed_login_count | INT | 로그인 실패 횟수 |
| last_login_at | TIMESTAMP NULL | 마지막 로그인 시각 |
| password_changed_at | TIMESTAMP | 비밀번호 변경 시각 |
| deleted_at | TIMESTAMP NULL | 탈퇴 처리 시각 |

활성 회원만 중복을 막기 위해 partial unique index를 사용합니다.

```sql
CREATE UNIQUE INDEX uq_members_active_login_id
    ON members(login_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_members_active_nickname
    ON members(nickname)
    WHERE deleted_at IS NULL;
```

### boards

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 게시글 ID |
| member_id | BIGINT FK | 작성 회원 |
| title | VARCHAR | 제목 |
| content | TEXT | 본문 |
| writer | VARCHAR | 작성 당시 닉네임 백업 |
| view_count | INT | 조회수 |
| created_at / updated_at | TIMESTAMP | 생성/수정 시각 |

조회 시에는 `COALESCE(m.nickname, b.writer)`를 사용합니다. 회원이 존재하면 현재 닉네임을 보여주고, 회원 정보가 없을 때는 게시글에 저장된 `writer`를 백업값으로 사용합니다.

### refresh_tokens

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 토큰 ID |
| member_id | BIGINT FK | 회원 ID |
| token_hash | VARCHAR(64) UNIQUE | Refresh Token의 SHA-256 해시 |
| expires_at | TIMESTAMP | 만료 시각 |
| created_at | TIMESTAMP | 발급 시각 |
| revoked_at | TIMESTAMP NULL | 폐기 시각, NULL이면 유효 |

```sql
CREATE INDEX idx_refresh_tokens_member_id ON refresh_tokens(member_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
```

### comments

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 댓글 ID |
| board_id | BIGINT FK | 게시글 ID |
| member_id | BIGINT FK | 작성 회원 |
| parent_id | BIGINT FK NULL | 부모 댓글 ID, NULL이면 일반 댓글 |
| content | TEXT | 댓글 내용 |
| deleted_at | TIMESTAMP NULL | 소프트 삭제 시각 |

```sql
CREATE INDEX idx_comments_board_id ON comments(board_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_board_parent_created
    ON comments(board_id, parent_id, created_at);
```

댓글은 소프트 삭제를 사용합니다. 삭제된 댓글도 대댓글 구조를 유지할 수 있고, 화면에서는 "삭제된 댓글입니다."처럼 표시할 수 있습니다.

### member_audit_logs

`schema-member-jwt.sql`에는 회원 이벤트 기록을 위한 `member_audit_logs` 테이블도 정의되어 있습니다. 현재 서비스 코드에서는 직접 사용하지 않지만, 로그인/탈퇴/비밀번호 변경 같은 보안 이벤트를 추후 기록하기 위한 확장 지점입니다.

---

## 5. 인증과 보안

### 전체 인증 흐름

```text
1. 로그인
   POST /api/members/login
   -> accessToken + refreshToken 발급
   -> refreshToken은 SHA-256 해시로 DB 저장

2. 인증 API 요청
   Authorization: Bearer {accessToken}
   -> JwtAuthenticationFilter가 access token 검증
   -> SecurityContext에 LoginMember 저장
   -> Controller에서 @AuthenticationPrincipal LoginMember 사용

3. Access Token 만료 시 재발급
   POST /api/members/refresh
   -> refresh token 서명/만료/tokenType 검증
   -> DB의 token_hash 조회
   -> 기존 refresh token revoke
   -> 새 accessToken + refreshToken 발급

4. 로그아웃
   POST /api/members/logout
   -> 전달받은 refresh token을 검증하고 DB에서 revoke
```

현재 `SecurityConfig` 기준으로 `/api/members/logout`은 Access Token 없이 호출 가능하도록 열려 있습니다. 대신 요청 바디의 Refresh Token을 검증한 뒤 해당 토큰을 폐기합니다.

### JWT Payload

Access Token:

```json
{
  "sub": "1",
  "tokenType": "access",
  "loginId": "user01",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234571490
}
```

Refresh Token:

```json
{
  "sub": "1",
  "tokenType": "refresh",
  "iat": 1234567890,
  "exp": 1235172290
}
```

`tokenType`을 분리한 이유는 Access Token으로 재발급 API를 호출하거나, Refresh Token으로 일반 API 인증을 시도하는 오용을 막기 위해서입니다.

### Refresh Token 저장 방식

Refresh Token 원문은 DB에 저장하지 않고 SHA-256 해시만 저장합니다.

```java
String tokenHash = TokenHashUtil.sha256(refreshToken);
refreshTokenMapper.save(member.getId(), tokenHash, expiresAt);
```

검증할 때도 요청으로 받은 Refresh Token을 다시 해시한 뒤 DB 값과 비교합니다. DB가 노출되어도 원본 Refresh Token을 바로 재사용하기 어렵게 만들기 위한 설계입니다.

### SecurityConfig 공개/보호 엔드포인트

공개:

```text
OPTIONS /**
POST /api/members/signup
POST /api/members/login
POST /api/members/refresh
POST /api/members/logout
GET  /api/boards/**
/uploads/**
```

보호:

```text
POST   /api/boards
PUT    /api/boards/{id}
DELETE /api/boards/{id}
GET    /api/members/me
PATCH  /api/members/me
PATCH  /api/members/me/password
DELETE /api/members/me
POST   /api/boards/{boardId}/comments
PUT    /api/boards/{boardId}/comments/{commentId}
DELETE /api/boards/{boardId}/comments/{commentId}
POST   /api/images
```

### 비밀번호 보안

`DelegatingPasswordEncoder`를 사용하며 기본적으로 BCrypt 해시를 사용합니다. DB에는 평문 비밀번호가 저장되지 않고, 로그인 시 입력 비밀번호를 같은 방식으로 검증합니다.

---

## 6. 주요 기능

### 6-1. 게시글 목록, 검색, 페이징

```text
GET /api/boards?page=1&size=10&searchType=all&keyword=jwt
```

응답:

```json
{
  "data": [
    { "id": 1, "title": "JWT 정리", "writer": "홍길동", "viewCount": 3 }
  ],
  "page": 1,
  "size": 10,
  "totalCount": 53,
  "totalPages": 6
}
```

검색 조건은 MyBatis `<choose>`로 처리합니다.

- `title`: 제목 검색
- `content`: 내용 검색
- `writer`: 작성자 검색
- `all`: 제목, 내용, 작성자 통합 검색

PostgreSQL의 `ILIKE`를 사용해 대소문자 구분 없이 검색합니다.

### 6-2. 인기글

```text
GET /api/boards/popular?limit=5
```

조회수 내림차순, 같은 조회수에서는 최신 ID 순으로 정렬합니다.

```sql
ORDER BY b.view_count DESC, b.id DESC
LIMIT #{limit}
```

### 6-3. 게시글 상세 조회와 조회수 증가

상세 조회 시 `increaseViewCount(id)`를 먼저 실행한 뒤 `findById(id)`로 최신 게시글 정보를 다시 조회합니다. 정확한 조회수를 응답에 반영하기 위한 방식입니다.

### 6-4. 게시글 수정/삭제 권한

게시글 수정과 삭제는 작성자만 가능합니다.

```java
if (!board.getMemberId().equals(memberId)) {
    throw new AccessDeniedException("...");
}
```

권한이 없으면 `GlobalExceptionHandler`가 403 응답으로 변환합니다.

### 6-5. 댓글과 대댓글

댓글 테이블은 `parent_id`로 자기 자신을 참조합니다.

```text
댓글 A (parent_id = NULL)
  ├─ 대댓글 A-1 (parent_id = A.id)
  └─ 대댓글 A-2 (parent_id = A.id)
댓글 B (parent_id = NULL)
```

대댓글에 다시 대댓글을 다는 것은 제한합니다. 구조가 무한 중첩되면 UI와 쿼리 복잡도가 크게 증가하기 때문에 1단계 대댓글만 허용했습니다.

### 6-6. 이미지 업로드

흐름:

1. 에디터에서 이미지 삽입
2. 프론트엔드가 `POST /api/images`로 multipart 업로드
3. 서버가 UUID 파일명으로 `file.upload-dir`에 저장
4. `/uploads/{filename}` URL 반환
5. 게시글 본문에는 반환된 URL 저장

게시글 삭제 시 본문에서 `/uploads/` 경로를 찾아 관련 이미지 파일을 삭제합니다.

### 6-7. 공통 예외 처리

`GlobalExceptionHandler`가 예외 타입별로 HTTP 상태 코드와 에러 응답을 통일합니다.

| 예외 | 상태 코드 | 상황 |
|---|---:|---|
| MethodArgumentNotValidException | 400 | DTO 검증 실패 |
| MethodArgumentTypeMismatchException | 400 | 경로/쿼리 파라미터 타입 오류 |
| IllegalArgumentException | 400 | 비즈니스 규칙 위반 |
| JwtException | 401 | JWT 서명 오류 또는 만료 |
| AccessDeniedException | 403 | 작성자 권한 없음 |
| BoardNotFoundException | 404 | 게시글 없음 |
| CommentNotFoundException | 404 | 댓글 없음 |
| DataIntegrityViolationException | 409 | DB 제약 조건 위반 |
| BoardCreate/Update/DeleteFailedException | 500 | 게시글 DB 처리 실패 |
| CommentCreate/Update/DeleteFailedException | 500 | 댓글 DB 처리 실패 |
| Exception | 500 | 예상하지 못한 서버 오류 |

공통 응답 형식:

```json
{
  "success": false,
  "message": "에러 메시지"
}
```

---

## 7. API 명세

### 회원

| Method | URL | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/members/signup` | X | 회원가입 |
| POST | `/api/members/login` | X | 로그인, 토큰 발급 |
| POST | `/api/members/refresh` | X | Access/Refresh Token 재발급 |
| POST | `/api/members/logout` | X | Refresh Token 폐기 |
| GET | `/api/members/me` | O | 내 정보 조회 |
| PATCH | `/api/members/me` | O | 닉네임 수정 |
| PATCH | `/api/members/me/password` | O | 비밀번호 변경 |
| DELETE | `/api/members/me` | O | 회원 탈퇴 |

### 게시글

| Method | URL | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/boards` | O | 게시글 작성 |
| GET | `/api/boards?page=1&size=10&searchType=all&keyword=` | X | 목록/검색/페이징 |
| GET | `/api/boards/popular?limit=5` | X | 인기글 조회 |
| GET | `/api/boards/{id}` | X | 상세 조회, 조회수 증가 |
| PUT | `/api/boards/{id}` | O | 게시글 수정 |
| DELETE | `/api/boards/{id}` | O | 게시글 삭제 |

### 댓글

| Method | URL | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/boards/{boardId}/comments` | O | 댓글/대댓글 작성 |
| GET | `/api/boards/{boardId}/comments` | X | 댓글 목록 조회 |
| PUT | `/api/boards/{boardId}/comments/{commentId}` | O | 댓글 수정 |
| DELETE | `/api/boards/{boardId}/comments/{commentId}` | O | 댓글 소프트 삭제 |

### 이미지

| Method | URL | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/images` | O | 이미지 업로드, `{ "url": "..." }` 반환 |

---

## 8. 프론트엔드 구조

### 페이지

```text
/                 BoardList
/login            Login
/signup           Signup
/mypage           MyPage
/boards/new       BoardCreate
/boards/:id       BoardDetail
/boards/:id/edit  BoardEdit
```

### 주요 폴더

```text
frontend/src
├─ features
│  ├─ board
│  │  ├─ api
│  │  ├─ pages
│  │  └─ types
│  ├─ comment
│  └─ member
├─ shared
│  ├─ api
│  └─ utils
├─ App.tsx
├─ main.tsx
└─ index.css
```

### Axios 인터셉터

요청 전 `localStorage`의 Access Token을 읽어 Authorization 헤더에 추가합니다.

```typescript
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

## 9. 핵심 설계 결정

| 결정 | 이유 |
|---|---|
| JWT 기반 stateless 인증 | 서버 세션 저장소 없이 확장하기 쉬움 |
| Refresh Token 도입 | Access Token 수명을 짧게 유지하면서 사용자 경험 보완 |
| Refresh Token SHA-256 저장 | DB 노출 시 원본 토큰 재사용 위험 완화 |
| Refresh Token Rotation | 재발급마다 기존 토큰 폐기, 탈취 토큰 재사용 가능성 축소 |
| `tokenType` claim 사용 | Access/Refresh Token 오용 방지 |
| MyBatis 사용 | SQL을 직접 제어하고 검색/페이징/정렬 쿼리를 명확히 관리 |
| 댓글 소프트 삭제 | 대댓글 구조 보존 |
| 게시글 하드 삭제 | 게시글 삭제 시 연결 댓글과 이미지도 정리 |
| `writer` 백업 컬럼 | 회원 정보 변경/삭제 상황에서 작성자 표시 보완 |
| 도메인별 패키지 분리 | 기능별 응집도 향상, 변경 영향 범위 축소 |
| 공통 예외 처리 | 컨트롤러 코드 단순화, 에러 응답 형식 통일 |

---

## 10. 면접 대비 개념 정리

### JWT와 세션 인증

| 구분 | JWT | 세션 |
|---|---|---|
| 상태 저장 | 토큰은 클라이언트가 보관 | 서버가 세션 저장 |
| 서버 부하 | Access Token 검증은 DB 조회 없이 가능 | 매 요청 세션 조회 필요 |
| 확장성 | 서버 간 세션 공유 부담이 작음 | 세션 공유 저장소 필요 |
| 강제 로그아웃 | Access Token 즉시 무효화는 별도 전략 필요 | 세션 삭제로 즉시 가능 |

현재 구현은 Access Token을 짧게 유지하고, Refresh Token은 DB에서 폐기할 수 있도록 설계했습니다.

### MyBatis 동작 방식

Mapper 인터페이스:

```java
@Mapper
public interface BoardMapper {
    List<Board> findAll(...);
}
```

Mapper XML:

```xml
<select id="findAll" resultMap="BoardResultMap">
  SELECT ...
</select>
```

MyBatis가 인터페이스 메서드와 XML의 SQL을 연결해 런타임에 구현체를 만들어 줍니다.

### DTO를 사용하는 이유

Entity를 직접 API 응답으로 반환하면 내부 필드가 노출될 수 있습니다. 요청/응답 DTO를 분리하면 API 계약을 명확히 만들고, 검증 규칙도 입력 형태에 맞춰 둘 수 있습니다.

### CORS

프론트엔드는 `localhost:5173`, 백엔드는 `localhost:8080`처럼 출처가 다를 수 있습니다. 브라우저 보안 정책 때문에 CORS 설정이 필요하며, 개발 환경에서는 허용 origin을 명시해 해결합니다.

### Soft Delete

물리 삭제:

```sql
DELETE FROM comments WHERE id = 1;
```

소프트 삭제:

```sql
UPDATE comments SET deleted_at = NOW() WHERE id = 1;
```

댓글은 대댓글 구조를 유지해야 하므로 소프트 삭제가 적합합니다.

---

## 11. 예상 질문과 답변

### Q1. Access Token을 localStorage에 저장해도 안전한가요?

A. XSS에 취약할 수 있습니다. 이 프로젝트는 학습/프로토타입 목적이라 localStorage를 사용했지만, 실제 서비스라면 Refresh Token은 HttpOnly Secure Cookie에 저장하고 Access Token은 메모리에 보관하는 방식을 검토하겠습니다.

### Q2. 로그아웃 후 Access Token은 바로 무효화되나요?

A. 현재 구현은 Refresh Token을 DB에서 폐기해 재발급을 막습니다. 이미 발급된 Access Token은 만료 전까지 유효할 수 있습니다. 운영 환경에서는 Access Token 수명을 더 짧게 하거나 Redis blacklist를 도입해 즉시 무효화를 구현할 수 있습니다.

### Q3. Refresh Token을 왜 DB에 해시로 저장했나요?

A. 원본 토큰을 저장하면 DB가 노출됐을 때 공격자가 바로 사용할 수 있습니다. 해시로 저장하면 요청 토큰을 같은 방식으로 해시해 비교할 수 있고, DB 값만으로는 원본 토큰을 복원하기 어렵습니다.

### Q4. 왜 JPA 대신 MyBatis를 선택했나요?

A. 검색, 페이징, 인기글 정렬, 조회수 증가처럼 SQL을 직접 제어해야 하는 부분이 많았습니다. MyBatis를 사용하면 실제 실행 SQL을 명확하게 관리할 수 있어 디버깅과 최적화에 유리합니다.

### Q5. 대댓글을 1단계로 제한한 이유는 무엇인가요?

A. 무한 중첩 댓글은 UI 렌더링과 쿼리가 복잡해집니다. 일반 게시판에서는 1단계 대댓글만으로도 충분한 경우가 많아 복잡도를 줄이는 방향으로 설계했습니다.

### Q6. 이미지가 로컬 디스크에 저장되면 서버가 여러 대일 때 문제가 없나요?

A. 현재 구현은 단일 서버 환경을 가정합니다. 서버가 여러 대라면 S3 같은 객체 스토리지 또는 공유 스토리지를 사용해야 합니다.

### Q7. 댓글 목록에서 N+1 문제가 발생하지 않나요?

A. 댓글 목록은 `boardId` 기준으로 한 번의 쿼리로 가져오고, 작성자 닉네임도 join으로 함께 조회합니다. 프론트엔드에서 `parentId` 기준으로 계층 구조를 구성하므로 댓글별 추가 조회가 발생하지 않습니다.

### Q8. 예외 처리는 어떻게 통일했나요?

A. `@RestControllerAdvice`와 `@ExceptionHandler`를 사용했습니다. 서비스에서는 상황에 맞는 예외를 던지고, 전역 핸들러가 HTTP 상태 코드와 `{ success, message }` 형식으로 응답을 만듭니다.

### Q9. Spring Security에서 세션을 사용하지 않는 이유는 무엇인가요?

A. JWT 기반 인증을 사용하므로 서버 세션을 저장하지 않아도 됩니다. `SessionCreationPolicy.STATELESS`를 설정해 요청마다 토큰으로 인증 상태를 판단합니다.

### Q10. 개선한다면 어떤 점을 먼저 하겠나요?

A. 우선 Refresh Token을 HttpOnly Cookie로 옮기고, Access Token 재발급 자동화와 Redis 기반 Access Token blacklist를 검토하겠습니다. 이미지 저장소는 로컬에서 S3 같은 외부 스토리지로 이전하는 것이 좋습니다.

---

## 12. 발표 시연 흐름

1. 프로젝트 소개: JWT 인증 기반 게시판이며 회원/게시글/댓글/이미지 기능을 구현했다고 설명
2. 회원 기능 시연: 회원가입, 로그인, 내 정보 조회, 닉네임 수정
3. 게시글 기능 시연: 게시글 작성, 목록/검색/페이징, 인기글, 상세 조회
4. 댓글 기능 시연: 댓글 작성, 대댓글 작성, 수정, 삭제
5. 이미지 기능 시연: 에디터 이미지 업로드 후 게시글 저장
6. 인증 구조 설명: Access Token, Refresh Token, DB 해시 저장, 토큰 로테이션
7. DB 설계 설명: `members`, `boards`, `comments`, `refresh_tokens` 관계와 soft delete
8. 코드 구조 설명: Controller, Service, Mapper, GlobalExceptionHandler 계층
9. 개선 방향 설명: HttpOnly Cookie, Redis blacklist, S3 이미지 저장소, 감사 로그 활용
