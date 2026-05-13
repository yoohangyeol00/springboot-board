# Spring Boot 게시판 프로젝트 정리

## 목차

1. 프로젝트 개요
2. 전체 구조
3. 인증 / 인가 흐름
4. 회원 기능
5. 게시글 CRUD
6. 이미지 업로드
7. 첨부파일
8. 댓글 CRUD
9. 예외 처리 구조
10. DB 구조
11. SQL JOIN / 트랜잭션
12. CORS / 정적 리소스
13. 파일 삭제 정합성
14. 보안 포인트와 개선점

---

## 1. 프로젝트 개요

### 한 줄 요약

JWT 인증 기반의 게시판 프로젝트다.
회원가입/로그인, 게시글 CRUD, 댓글/대댓글, 이미지 업로드, 첨부파일 업로드/다운로드, refresh token 기반 토큰 재발급을 구현했다.

### 주요 기술

| 영역 | 기술 |
|---|---|
| Backend | Spring Boot, Spring Security, MyBatis |
| Database | PostgreSQL |
| Auth | JWT Access Token, Refresh Token |
| File | Local file system, `/uploads/**` static resource |
| Build/Deploy | Docker Compose, GitHub Actions |

### 핵심 특징

- 세션을 사용하지 않는 JWT 기반 stateless 인증
- access token / refresh token 분리
- refresh token은 DB에 원문이 아니라 SHA-256 해시로 저장
- refresh token rotation 적용
- 게시글/댓글/첨부파일 작성자 권한 검증
- 게시글 본문 이미지와 첨부파일 실제 파일 관리
- MyBatis XML 기반 명시적 SQL 작성

---

## 2. 전체 구조

### 패키지 구조

```text
com.board.backend
├─ board
│  ├─ controller
│  ├─ service
│  ├─ mapper
│  ├─ domain
│  ├─ dto
│  └─ exception
├─ member
│  ├─ controller
│  ├─ service
│  ├─ mapper
│  ├─ domain
│  └─ dto
├─ comment
│  ├─ controller
│  ├─ service
│  ├─ mapper
│  ├─ domain
│  ├─ dto
│  └─ exception
├─ attachment
│  ├─ service
│  ├─ mapper
│  ├─ domain
│  ├─ dto
│  └─ exception
├─ image
│  ├─ controller
│  └─ service
└─ global
   ├─ config
   ├─ security
   ├─ common
   └─ exception
```

### 레이어 흐름

```text
Client
  ↓
Security Filter Chain
  ↓
Controller
  ↓
Service
  ↓
Mapper Interface
  ↓
MyBatis XML SQL
  ↓
PostgreSQL
```

### 각 레이어 역할

| 레이어 | 역할 |
|---|---|
| Controller | HTTP 요청/응답 처리 |
| Service | 비즈니스 로직, 권한 검증, 예외 발생, 트랜잭션 |
| Mapper | DB 접근 메서드 정의 |
| Mapper XML | 실제 SQL 작성 |
| Domain | DB 조회 결과 매핑 객체 |
| DTO | 요청/응답 전용 객체 |

---

## 3. 인증 / 인가 흐름

### 한 줄 요약

로그인 성공 시 access token과 refresh token을 발급하고, 이후 API 요청은 `Authorization: Bearer {accessToken}` 헤더로 인증한다.

### 관련 파일

- `SecurityConfig.java`
- `JwtAuthenticationFilter.java`
- `JwtTokenProvider.java`
- `LoginMember.java`
- `TokenHashUtil.java`

### Security 설정

```java
.csrf(csrf -> csrf.disable())
.formLogin(form -> form.disable())
.httpBasic(basic -> basic.disable())
.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

세션, form login, basic auth를 사용하지 않고 JWT 기반으로 인증한다.

### 공개 API

```java
.requestMatchers(
        "/api/members/signup",
        "/api/members/login",
        "/api/members/refresh",
        "/api/members/logout").permitAll()
.requestMatchers(HttpMethod.GET, "/api/boards/**").permitAll()
.requestMatchers("/uploads/**").permitAll()
```

### 보호 API

```java
.anyRequest().authenticated()
```

위 공개 API 외에는 access token 인증이 필요하다.

### JWT 인증 필터 흐름

```text
HTTP 요청
  ↓
Authorization 헤더 확인
  ↓
Bearer token 추출
  ↓
JwtTokenProvider.validateToken()
  ↓
tokenType = access 검증
  ↓
LoginMember 생성
  ↓
UsernamePasswordAuthenticationToken 생성
  ↓
SecurityContextHolder에 저장
  ↓
Controller에서 @AuthenticationPrincipal 사용
```

### 인증 사용자 객체

```java
public class LoginMember {
    private final Long id;
    private final String loginId;
    private final String role;
}
```

컨트롤러에서는 다음처럼 현재 로그인 사용자를 받는다.

```java
@AuthenticationPrincipal LoginMember loginMember
```

---

## 4. 회원 기능

## 4-1. 회원가입

### 한 줄 요약

`loginId`, `password`, `nickname`을 받아 중복 검사를 하고, 비밀번호를 해싱한 뒤 `members` 테이블에 저장한다.

### API

```http
POST /api/members/signup
Content-Type: application/json
```

```json
{
  "loginId": "user01",
  "password": "password123",
  "nickname": "닉네임"
}
```

### 흐름

```text
회원가입 요청
  ↓
@Valid 요청값 검증
  ↓
loginId 중복 검사
  ↓
nickname 중복 검사
  ↓
passwordEncoder.encode()
  ↓
members INSERT
  ↓
성공 응답
```

### 핵심 로직

```java
Member existingMember = memberMapper.findByLoginId(request.getLoginId());
Member existingNickname = memberMapper.findByNickname(request.getNickname());
String passwordHash = passwordEncoder.encode(request.getPassword());
memberMapper.save(request.getLoginId(), passwordHash, request.getNickname());
```

### DB 기본값

| 컬럼 | 기본값 |
|---|---|
| `role` | `USER` |
| `status` | `ACTIVE` |
| `failed_login_count` | `0` |
| `password_changed_at` | 현재 시간 |
| `created_at` | 현재 시간 |
| `updated_at` | 현재 시간 |

---

## 4-2. 로그인

### 한 줄 요약

`loginId`, `password`를 검증하고 access token과 refresh token을 발급한다.

### API

```http
POST /api/members/login
Content-Type: application/json
```

```json
{
  "loginId": "user01",
  "password": "password123"
}
```

### 흐름

```text
로그인 요청
  ↓
loginId로 회원 조회
  ↓
회원 존재 여부 확인
  ↓
status = ACTIVE 확인
  ↓
passwordEncoder.matches()
  ↓
실패 시 failed_login_count 증가
  ↓
성공 시 실패 횟수 초기화, last_login_at 갱신
  ↓
access token 생성
  ↓
refresh token 생성
  ↓
refresh token SHA-256 해시 DB 저장
  ↓
토큰 응답
```

### 응답

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

---

## 4-3. 토큰 재발급

### 한 줄 요약

refresh token을 검증하고, 기존 refresh token을 폐기한 뒤 새 access token과 refresh token을 발급한다.

### API

```http
POST /api/members/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

### 흐름

```text
refresh 요청
  ↓
refresh token JWT 검증
  ↓
tokenType = refresh 확인
  ↓
JWT subject에서 memberId 추출
  ↓
refresh token SHA-256 해시 생성
  ↓
DB에서 유효한 토큰 조회
  ↓
DB memberId와 JWT memberId 비교
  ↓
회원 ACTIVE 상태 확인
  ↓
기존 refresh token revoke
  ↓
새 access token, refresh token 발급
  ↓
새 refresh token 해시 저장
```

### Refresh Token Rotation

```text
기존 refresh token 사용
  ↓
기존 refresh token 폐기
  ↓
새 refresh token 발급
```

이미 사용된 refresh token은 다시 사용할 수 없다.

---

## 4-4. 로그아웃

### 한 줄 요약

refresh token을 DB에서 폐기해서 이후 재발급을 막는다.

### API

```http
POST /api/members/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

### 흐름

```text
로그아웃 요청
  ↓
refresh token JWT 검증
  ↓
refresh token SHA-256 해시 생성
  ↓
refresh_tokens.revoked_at 갱신
  ↓
성공 응답
```

### 주의

access token은 DB에 저장하지 않으므로 로그아웃 시 즉시 폐기하지 않는다.
대신 refresh token을 폐기해서 access token 재발급을 막는다.

---

## 4-5. 회원 정보 관리

### API

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/members/me` | 내 정보 조회 |
| `PATCH` | `/api/members/me` | 닉네임 수정 |
| `PATCH` | `/api/members/me/password` | 비밀번호 변경 |
| `DELETE` | `/api/members/me` | 회원 탈퇴 |

### 내 정보 조회

```text
JWT 인증
  ↓
LoginMember.id 추출
  ↓
members 조회
  ↓
MemberMeResponse 응답
```

### 닉네임 수정

```text
회원 조회
  ↓
닉네임 중복 검사
  ↓
members.nickname update
```

### 비밀번호 변경

```text
회원 조회
  ↓
현재 비밀번호 검증
  ↓
새 비밀번호 해싱
  ↓
password_hash update
```

### 회원 탈퇴

```text
회원 조회
  ↓
비밀번호 검증
  ↓
회원 게시글 본문 이미지 삭제
  ↓
회원 게시글 삭제
  ↓
회원 refresh token 전체 폐기
  ↓
members status = WITHDRAWN
  ↓
deleted_at 설정
```

---

## 5. 게시글 CRUD

### 한 줄 요약

게시글은 로그인한 사용자가 작성하고, 작성자 본인만 수정/삭제할 수 있다.
조회는 비로그인 사용자도 가능하다.
상세 조회 시 조회수가 증가하고, 게시글 수정 시 본문에서 제거된 이미지 파일은 서버에서도 삭제된다.

### 관련 파일

- `BoardController.java`
- `BoardService.java`
- `BoardServiceImpl.java`
- `BoardMapper.java`
- `BoardMapper.xml`
- `BoardCreateRequest.java`
- `BoardUpdateRequest.java`
- `BoardResponse.java`
- `Board.java`

## 5-1. 게시글 생성

### API

```http
POST /api/boards
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "title": "제목",
  "content": "내용"
}
```

### 흐름

```text
게시글 작성 요청
  ↓
JWT 인증 정보에서 로그인 회원 ID 추출
  ↓
boards 테이블에 title, content, writer, member_id 저장
  ↓
생성된 게시글 ID 확인
  ↓
생성된 게시글 다시 조회
  ↓
BoardResponse 응답
```

### SQL

```sql
INSERT INTO boards (
    title,
    content,
    writer,
    member_id
) VALUES (
    #{request.title},
    #{request.content},
    (SELECT nickname FROM members WHERE id = #{memberId}),
    #{memberId}
)
```

## 5-2. 게시글 목록 조회

### API

```http
GET /api/boards?page=1&size=10&searchType=all&keyword=검색어
```

### 흐름

```text
목록 조회 요청
  ↓
page, size로 offset 계산
  ↓
searchType 정규화
  ↓
keyword 정규화
  ↓
boards LEFT JOIN members 조회
  ↓
전체 개수 countAll 조회
  ↓
PageResponse 응답
```

### 검색 타입

```text
all
title
content
writer
```

### SQL 특징

```sql
FROM boards b
LEFT JOIN members m ON b.member_id = m.id
```

작성자명은 다음 방식으로 결정한다.

```sql
COALESCE(m.nickname, b.writer) AS writer
```

`members.nickname`이 있으면 현재 닉네임을 사용하고, 없으면 `boards.writer`를 사용한다.

## 5-3. 게시글 상세 조회

### API

```http
GET /api/boards/{id}
```

### 흐름

```text
상세 조회 요청
  ↓
게시글 존재 확인
  ↓
조회수 1 증가
  ↓
게시글 다시 조회
  ↓
첨부파일 목록 조회
  ↓
BoardResponse 응답
```

### 조회수 증가 SQL

```sql
UPDATE boards
SET view_count = view_count + 1
WHERE id = #{id}
```

## 5-4. 인기 게시글 조회

### API

```http
GET /api/boards/popular?limit=5
```

### SQL

```sql
ORDER BY b.view_count DESC, b.id DESC
LIMIT #{limit}
```

조회수 높은 순, 조회수가 같으면 최신 글 순으로 조회한다.

## 5-5. 게시글 수정

### API

```http
PUT /api/boards/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "title": "수정 제목",
  "content": "수정 내용"
}
```

### 흐름

```text
게시글 수정 요청
  ↓
게시글 존재 확인
  ↓
게시글 작성자와 로그인 회원 ID 비교
  ↓
권한 없으면 403
  ↓
boards 테이블 UPDATE
  ↓
수정 전 content 이미지 목록 추출
  ↓
수정 후 content 이미지 목록 추출
  ↓
수정 후 본문에서 제거된 이미지 계산
  ↓
제거된 이미지 실제 파일 삭제
```

### 핵심 로직

```java
int result = boardMapper.update(id, request);

if (result != 1) {
    throw new BoardUpdateFailedException();
}

imageService.deleteRemovedImages(board.getContent(), request.getContent());
```

### 이미지 삭제 의미

```text
수정 전 content 이미지 URL
  -
수정 후 content 이미지 URL
  =
수정하면서 제거된 이미지 URL
```

예시:

```text
수정 전: /uploads/a.png, /uploads/b.png
수정 후: /uploads/b.png, /uploads/c.png
삭제 대상: /uploads/a.png
```

## 5-6. 게시글 삭제

### API

```http
DELETE /api/boards/{id}
Authorization: Bearer {accessToken}
```

### 흐름

```text
게시글 삭제 요청
  ↓
게시글 존재 확인
  ↓
게시글 작성자와 로그인 회원 ID 비교
  ↓
권한 없으면 403
  ↓
첨부파일 저장 파일명 목록 조회
  ↓
본문 이미지 전체 삭제
  ↓
boards 테이블 DELETE
  ↓
첨부파일 실제 파일 삭제
```

### 삭제 시 처리되는 것

- 게시글 row 삭제
- DB cascade로 댓글 삭제
- DB cascade로 첨부파일 row 삭제
- 본문 이미지 실제 파일 삭제
- 첨부파일 실제 파일 삭제

---

## 6. 이미지 업로드

### 한 줄 요약

이미지는 로컬 `uploads` 폴더에 저장하고 `/uploads/{filename}` URL을 반환한다.
게시글 본문에 포함된 이미지 URL은 게시글 수정/삭제/회원 탈퇴 시 실제 파일 삭제에 사용된다.

### 관련 파일

- `ImageController.java`
- `ImageService.java`
- `WebConfig.java`
- `SecurityConfig.java`

### 업로드 API

```http
POST /api/images
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

```text
image: 업로드할 이미지 파일
```

### 저장 흐름

```text
이미지 업로드 요청
  ↓
uploads 폴더 없으면 생성
  ↓
원본 파일명에서 확장자 추출
  ↓
UUID 기반 저장 파일명 생성
  ↓
파일 저장
  ↓
/uploads/{filename} 반환
```

### 응답

```json
{
  "url": "/uploads/abc-123.png"
}
```

### 이미지 URL 추출 정규식

```java
private static final Pattern UPLOAD_IMAGE_PATTERN =
        Pattern.compile("/uploads/(?!attachments/)[^\\s\"')\\]]+");
```

의미:

- `/uploads/image.png`는 본문 이미지로 판단
- `/uploads/attachments/file.pdf`는 첨부파일이므로 제외

### 게시글 수정 시 이미지 삭제

```java
public void deleteRemovedImages(String oldContent, String newContent) {
    Set<String> oldImageUrls = extractImageUrls(oldContent);
    Set<String> newImageUrls = extractImageUrls(newContent);

    oldImageUrls.removeAll(newImageUrls);
    oldImageUrls.forEach(this::deleteImage);
}
```

### 게시글 삭제 / 회원 탈퇴 시 이미지 삭제

```java
public void deleteImages(String content) {
    extractImageUrls(content).forEach(this::deleteImage);
}
```

### 실제 삭제 보안 처리

```java
Path filePath = uploadPath.resolve(filename).normalize();

if (!filePath.startsWith(uploadPath)) {
    return;
}
```

업로드 폴더 밖의 파일을 삭제하지 않도록 방어한다.

---

## 7. 첨부파일

### 한 줄 요약

첨부파일은 실제 파일을 `uploads/attachments`에 저장하고, 파일 메타데이터는 `board_attachments` 테이블에 저장한다.
첨부파일 추가/삭제/교체는 게시글 작성자만 가능하다.

### 관련 파일

- `BoardAttachmentService.java`
- `AttachmentStorageService.java`
- `BoardAttachmentMapper.java`
- `BoardAttachmentMapper.xml`
- `BoardAttachment.java`
- `AttachmentResponse.java`
- `StoredAttachmentFile.java`

## 7-1. 첨부파일 추가

### API

```http
POST /api/boards/{boardId}/attachments
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

```text
files: 첨부할 파일 목록
```

### 흐름

```text
첨부파일 추가 요청
  ↓
게시글 작성자 검증
  ↓
파일 비어있는지 검증
  ↓
파일 크기 10MB 이하 검증
  ↓
게시글당 최대 10개 제한 검증
  ↓
uploads/attachments에 실제 파일 저장
  ↓
board_attachments 테이블에 메타데이터 저장
  ↓
첨부파일 목록 응답
```

### 제한

| 항목 | 제한 |
|---|---|
| 파일당 크기 | 10MB |
| 게시글당 개수 | 10개 |
| 차단 확장자 | `.exe`, `.bat`, `.cmd`, `.com`, `.sh`, `.js`, `.jsp`, `.php`, `.msi` |

### DB 저장 정보

| 컬럼 | 의미 |
|---|---|
| `board_id` | 게시글 ID |
| `original_name` | 원본 파일명 |
| `stored_name` | UUID 저장 파일명 |
| `file_url` | `/uploads/attachments/{storedName}` |
| `file_size` | 파일 크기 |
| `content_type` | MIME 타입 |

### 실패 보정

파일 저장 후 DB 저장이 실패하면 이미 저장한 실제 파일을 삭제한다.

```text
파일 저장 성공
  ↓
DB INSERT 실패
  ↓
DB rollback
  ↓
저장된 실제 파일 수동 삭제
```

## 7-2. 첨부파일 삭제

### API

```http
DELETE /api/boards/{boardId}/attachments/{attachmentId}
Authorization: Bearer {accessToken}
```

### 흐름

```text
게시글 작성자 검증
  ↓
첨부파일 존재 확인
  ↓
board_attachments row 삭제
  ↓
실제 파일 삭제
```

## 7-3. 첨부파일 교체

### API

```http
PUT /api/boards/{boardId}/attachments/{attachmentId}
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

```text
file: 새 파일
```

### 흐름

```text
게시글 작성자 검증
  ↓
기존 첨부파일 조회
  ↓
새 파일 저장
  ↓
DB 첨부파일 정보 update
  ↓
기존 실제 파일 삭제
  ↓
수정된 첨부파일 응답
```

DB update 실패 시 새로 저장한 파일을 삭제한다.

## 7-4. 첨부파일 다운로드

### API

```http
GET /api/boards/{boardId}/attachments/{attachmentId}/download
```

### 흐름

```text
게시글 존재 확인
  ↓
첨부파일 존재 확인
  ↓
실제 파일 Resource 조회
  ↓
Content-Disposition: attachment
  ↓
원본 파일명으로 다운로드
```

---

## 8. 댓글 CRUD

### 한 줄 요약

댓글은 게시글에 종속된다.
댓글 작성/수정/삭제는 로그인 필요, 조회는 비로그인도 가능하다.
댓글 삭제는 물리 삭제가 아니라 `deleted_at`을 채우는 soft delete 방식이다.

### 관련 파일

- `CommentController.java`
- `CommentService.java`
- `CommentServiceImpl.java`
- `CommentMapper.java`
- `CommentMapper.xml`
- `CommentCreateRequest.java`
- `CommentUpdateRequest.java`
- `CommentResponse.java`
- `Comment.java`

## 8-1. 댓글 작성

### API

```http
POST /api/boards/{boardId}/comments
Authorization: Bearer {accessToken}
Content-Type: application/json
```

일반 댓글:

```json
{
  "content": "댓글 내용"
}
```

대댓글:

```json
{
  "content": "대댓글 내용",
  "parentId": 1
}
```

### 흐름

```text
댓글 작성 요청
  ↓
게시글 존재 확인
  ↓
parentId가 있으면 부모 댓글 검증
  ↓
대댓글의 대댓글인지 확인
  ↓
comments INSERT
```

### 대댓글 제한

```text
댓글
  └─ 대댓글
```

까지만 허용한다.

```text
댓글
  └─ 대댓글
       └─ 대댓글의 대댓글
```

은 허용하지 않는다.

## 8-2. 댓글 조회

### API

```http
GET /api/boards/{boardId}/comments
```

### SQL

```sql
SELECT
    c.id,
    c.board_id,
    c.member_id,
    c.parent_id,
    c.content,
    m.nickname AS writer,
    c.created_at,
    c.updated_at,
    c.deleted_at
FROM comments c
JOIN members m ON c.member_id = m.id
WHERE c.board_id = #{boardId}
ORDER BY
    COALESCE(c.parent_id, c.id) ASC,
    c.parent_id NULLS FIRST,
    c.created_at ASC,
    c.id ASC
```

### 정렬 의미

```text
부모 댓글
  ↓
해당 부모 댓글의 대댓글
  ↓
작성 시간순
```

## 8-3. 댓글 수정

### API

```http
PUT /api/boards/{boardId}/comments/{commentId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "content": "수정 내용"
}
```

### 흐름

```text
댓글 존재 확인
  ↓
요청한 게시글에 속한 댓글인지 확인
  ↓
작성자 본인 검증
  ↓
삭제된 댓글인지 확인
  ↓
comments UPDATE
```

## 8-4. 댓글 삭제

### API

```http
DELETE /api/boards/{boardId}/comments/{commentId}
Authorization: Bearer {accessToken}
```

### 흐름

```text
댓글 존재 확인
  ↓
요청한 게시글에 속한 댓글인지 확인
  ↓
작성자 본인 검증
  ↓
삭제된 댓글인지 확인
  ↓
deleted_at, updated_at 갱신
```

### Soft Delete

```sql
UPDATE comments
SET
    deleted_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = #{id}
  AND deleted_at IS NULL
```

삭제된 댓글은 조회 결과에는 남지만, `deleted = true`이고 내용은 삭제 메시지로 대체된다.

---

## 9. 예외 처리 구조

### 한 줄 요약

각 계층에서 예외를 던지고, `GlobalExceptionHandler`가 모든 예외를 HTTP 상태코드와 공통 에러 응답 형식으로 변환한다.

### 관련 파일

- `GlobalExceptionHandler.java`
- `ErrorResponse.java`
- 도메인별 exception 클래스

### 에러 응답 형식

```java
public class ErrorResponse {
    private boolean success;
    private String message;
}
```

응답 예시:

```json
{
  "success": false,
  "message": "게시글을 찾을 수 없습니다."
}
```

### 상태코드 매핑

| 상태코드 | 예외 | 의미 |
|---|---|---|
| `400` | `MethodArgumentNotValidException` | DTO validation 실패 |
| `400` | `MethodArgumentTypeMismatchException` | 요청 파라미터 타입 불일치 |
| `400` | `IllegalArgumentException` | 잘못된 요청 값 |
| `401` | `JwtException` | JWT 오류 |
| `401` | Security 인증 실패 | 인증 정보 없음 |
| `403` | `AccessDeniedException` | 권한 없음 |
| `404` | `BoardNotFoundException` | 게시글 없음 |
| `404` | `CommentNotFoundException` | 댓글 없음 |
| `404` | `AttachmentNotFoundException` | 첨부파일 없음 |
| `409` | `DataIntegrityViolationException` | DB 제약조건 위반 |
| `500` | `BoardCreate/Update/DeleteFailedException` | 게시글 처리 실패 |
| `500` | `CommentCreate/Update/DeleteFailedException` | 댓글 처리 실패 |
| `500` | `AttachmentSave/Update/DeleteFailedException` | 첨부파일 처리 실패 |
| `500` | `IllegalStateException` | 서버 처리 상태 오류 |
| `500` | `Exception` | 예상하지 못한 서버 오류 |

### 전체 흐름

```text
Controller / Service
  ↓
throw new XxxException()
  ↓
GlobalExceptionHandler
  ↓
HTTP 상태코드 결정
  ↓
ErrorResponse 응답
```

---

## 10. DB 구조

### 전체 관계

```text
members
  ├─ boards
  │  ├─ comments
  │  └─ board_attachments
  ├─ comments
  └─ refresh_tokens
```

### 관계 요약

| 관계 | 설명 |
|---|---|
| `members 1:N boards` | 회원은 여러 게시글 작성 |
| `members 1:N comments` | 회원은 여러 댓글 작성 |
| `members 1:N refresh_tokens` | 회원은 여러 refresh token 보유 가능 |
| `boards 1:N comments` | 게시글은 여러 댓글 보유 |
| `boards 1:N board_attachments` | 게시글은 여러 첨부파일 보유 |
| `comments 1:N comments` | 댓글은 대댓글 보유 가능 |

## 10-1. members

| 컬럼 | 의미 |
|---|---|
| `id` | 회원 PK |
| `login_id` | 로그인 아이디 |
| `password_hash` | 해싱된 비밀번호 |
| `nickname` | 닉네임 |
| `role` | 권한 |
| `status` | 회원 상태 |
| `failed_login_count` | 로그인 실패 횟수 |
| `last_login_at` | 마지막 로그인 시간 |
| `password_changed_at` | 비밀번호 변경 시간 |
| `created_at` | 생성 시간 |
| `updated_at` | 수정 시간 |
| `deleted_at` | 탈퇴 시간 |

### Unique Index

```sql
CREATE UNIQUE INDEX uq_members_active_login_id
    ON members(login_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_members_active_nickname
    ON members(nickname)
    WHERE deleted_at IS NULL;
```

탈퇴하지 않은 회원 기준으로만 아이디/닉네임 중복을 막는다.

## 10-2. refresh_tokens

| 컬럼 | 의미 |
|---|---|
| `id` | refresh token PK |
| `member_id` | 회원 ID |
| `token_hash` | refresh token SHA-256 해시 |
| `expires_at` | 만료 시간 |
| `created_at` | 생성 시간 |
| `revoked_at` | 폐기 시간 |

### FK

```sql
FOREIGN KEY (member_id)
REFERENCES members(id)
ON DELETE CASCADE
```

## 10-3. boards

| 컬럼 | 의미 |
|---|---|
| `id` | 게시글 PK |
| `member_id` | 작성 회원 ID |
| `title` | 제목 |
| `content` | 내용 |
| `writer` | 작성자명 |
| `view_count` | 조회수 |
| `created_at` | 생성 시간 |
| `updated_at` | 수정 시간 |

## 10-4. board_attachments

| 컬럼 | 의미 |
|---|---|
| `id` | 첨부파일 PK |
| `board_id` | 게시글 ID |
| `original_name` | 원본 파일명 |
| `stored_name` | 서버 저장 파일명 |
| `file_url` | 접근 URL |
| `file_size` | 파일 크기 |
| `content_type` | MIME 타입 |
| `created_at` | 생성 시간 |
| `updated_at` | 수정 시간 |

### FK

```sql
FOREIGN KEY (board_id)
REFERENCES boards(id)
ON DELETE CASCADE
```

게시글이 삭제되면 DB 첨부파일 row도 삭제된다.

## 10-5. comments

| 컬럼 | 의미 |
|---|---|
| `id` | 댓글 PK |
| `board_id` | 게시글 ID |
| `member_id` | 작성 회원 ID |
| `parent_id` | 부모 댓글 ID |
| `content` | 댓글 내용 |
| `created_at` | 생성 시간 |
| `updated_at` | 수정 시간 |
| `deleted_at` | 삭제 시간 |

### FK

```sql
FOREIGN KEY (board_id)
REFERENCES boards(id)
ON DELETE CASCADE
```

```sql
FOREIGN KEY (parent_id)
REFERENCES comments(id)
ON DELETE CASCADE
```

### 인덱스

```sql
CREATE INDEX idx_comments_board_id ON comments(board_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_board_parent_created
    ON comments(board_id, parent_id, created_at);
```

---

## 11. SQL JOIN / 트랜잭션

## 11-1. 게시글 조회 JOIN

```sql
FROM boards b
LEFT JOIN members m ON b.member_id = m.id
```

게시글은 `LEFT JOIN`을 사용한다.

이유:

- 회원 정보가 없어도 게시글은 조회될 수 있다.
- `COALESCE(m.nickname, b.writer)`로 작성자명을 보완한다.

## 11-2. 댓글 조회 JOIN

```sql
FROM comments c
JOIN members m ON c.member_id = m.id
```

댓글은 `INNER JOIN`을 사용한다.
댓글 작성자의 닉네임을 `members.nickname`에서 가져온다.

## 11-3. 첨부파일 조회

```sql
FROM board_attachments
WHERE board_id = #{boardId}
ORDER BY id ASC
```

첨부파일은 별도 JOIN 없이 `board_id` 기준으로 조회한다.

## 11-4. Refresh Token 조회

```sql
FROM refresh_tokens
WHERE token_hash = #{tokenHash}
  AND revoked_at IS NULL
  AND expires_at > #{now}
```

유효한 refresh token 조건:

- 해시값 일치
- 폐기되지 않음
- 만료되지 않음

## 11-5. 트랜잭션 적용 메서드

### 게시글

```java
@Transactional
public BoardResponse create(...)
```

```java
@Transactional
public void updateBoard(...)
```

```java
@Transactional
public void deleteBoard(...)
```

### 첨부파일

```java
@Transactional
public List<AttachmentResponse> addAttachments(...)
```

```java
@Transactional
public void deleteAttachment(...)
```

```java
@Transactional
public AttachmentResponse replaceAttachment(...)
```

### 회원 탈퇴

```java
@Transactional
public void withdraw(...)
```

### 주의

DB 작업은 트랜잭션으로 롤백되지만 파일 시스템 작업은 롤백되지 않는다.

---

## 12. CORS / 정적 리소스

### 한 줄 요약

`WebConfig`에서 `/uploads/**` URL을 로컬 업로드 폴더와 연결하고, `SecurityConfig`에서 `/uploads/**` 접근을 인증 없이 허용한다.

### 정적 리소스 설정

```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + absolutePath + "/");
```

### 설정값

```yaml
file:
  upload-dir: uploads
```

### 접근 예시

```text
실제 파일:
uploads/example.png

URL:
/uploads/example.png
```

첨부파일:

```text
실제 파일:
uploads/attachments/report.pdf

URL:
/uploads/attachments/report.pdf
```

### Security

```java
.requestMatchers("/uploads/**").permitAll()
```

업로드된 이미지/첨부파일 URL은 로그인 없이 접근 가능하다.

### CORS

현재 명시적인 CORS 설정 Bean은 없고, `OPTIONS` 요청만 전체 허용되어 있다.

```java
.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

프론트와 백엔드가 다른 origin에서 동작하면 별도의 CORS 설정이 필요할 수 있다.

---

## 13. 파일 삭제 정합성

### 한 줄 요약

이미지와 첨부파일은 로컬 파일 시스템에 저장되므로 DB 트랜잭션만으로 파일 정합성을 완전히 보장할 수 없다.
현재는 첨부파일 추가/교체 보정 로직과 게시글 수정 시 제거 이미지 삭제 로직이 있다.

## 13-1. 게시글 수정 시 이미지 정합성

```text
DB update 성공
  ↓
oldContent와 newContent 비교
  ↓
제거된 이미지 파일 삭제
```

삭제 실패 시 DB 수정은 완료되고 파일은 남을 수 있다.

## 13-2. 게시글 삭제 시 이미지 정합성

```text
본문 이미지 삭제
  ↓
boards DELETE
  ↓
첨부파일 실제 파일 삭제
```

본문 이미지 삭제가 DB 삭제보다 먼저 실행되므로, DB 삭제 실패 시 본문 이미지가 깨질 수 있다.

## 13-3. 첨부파일 추가 정합성

```text
파일 저장 성공
  ↓
DB INSERT 실패
  ↓
DB rollback
  ↓
저장된 파일 수동 삭제
```

## 13-4. 첨부파일 교체 정합성

```text
새 파일 저장 성공
  ↓
DB UPDATE 실패
  ↓
DB rollback
  ↓
새 파일 수동 삭제
```

## 13-5. 첨부파일 삭제 정합성

```text
DB row 삭제
  ↓
실제 파일 삭제
```

실제 파일 삭제 실패 시 DB row는 삭제되고 파일만 남을 수 있다.

### 개선 방향

- 트랜잭션 커밋 이후 파일 삭제
- 미사용 이미지 정리 스케줄러
- 임시 업로드 폴더 도입
- 파일 삭제 실패 재시도 구조
- orphan file 정리 작업

---

## 14. 보안 포인트와 개선점

## 14-1. 현재 보안 포인트

### 비밀번호 해싱

```java
passwordEncoder.encode(request.getPassword())
```

비밀번호는 평문 저장하지 않고 해시로 저장한다.

### JWT tokenType 검증

```java
.claim("tokenType", "access")
.claim("tokenType", "refresh")
```

access token과 refresh token을 구분한다.

### Refresh Token 해시 저장

```java
TokenHashUtil.sha256(refreshToken)
```

DB에는 refresh token 원문을 저장하지 않는다.

### 작성자 권한 검증

게시글, 댓글, 첨부파일 수정/삭제는 작성자 본인만 가능하다.

### 파일 업로드 보안

- 첨부파일 위험 확장자 차단
- 파일 크기 제한
- UUID 저장 파일명 사용
- 경로 조작 방어
- 이미지 삭제 시 업로드 폴더 밖 삭제 방지

## 14-2. 개선점

### access token 즉시 폐기

현재 로그아웃은 refresh token만 폐기한다.
access token은 만료 전까지 유효할 수 있다.

개선:

- access token blacklist
- access token 만료 시간 단축

### 로그인 / refresh 트랜잭션

로그인과 refresh에는 명시적 `@Transactional`이 없다.
특히 refresh token rotation은 기존 토큰 폐기와 새 토큰 저장이 한 트랜잭션으로 묶이면 더 안전하다.

### 이미지 업로드 검증

본문 이미지 업로드는 MIME 타입/확장자 검증이 약하다.

개선:

- 이미지 MIME 타입 검증
- 허용 확장자 whitelist
- 파일 시그니처 검사

### 공통 에러 코드

현재 에러 응답은 `success`, `message`만 있다.

개선 예시:

```json
{
  "success": false,
  "code": "BOARD_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

### 파일 저장소

현재는 로컬 파일 시스템에 저장한다.
서버가 여러 대가 되면 S3 같은 외부 스토리지를 고려해야 한다.

---

## 발표용 요약

```text
이 프로젝트는 Spring Boot와 MyBatis 기반의 게시판 서비스입니다.
인증은 Spring Security와 JWT를 사용했고, access token과 refresh token을 분리했습니다.
refresh token은 DB에 원문이 아니라 SHA-256 해시로 저장하며, 재발급 시 기존 토큰을 폐기하는 rotation 방식을 사용합니다.

게시글은 로그인한 사용자만 작성할 수 있고, 수정/삭제는 작성자 본인만 가능합니다.
조회는 비로그인 사용자도 가능하며, 상세 조회 시 조회수가 증가합니다.
게시글 본문 이미지는 로컬 uploads 폴더에 저장되고, 게시글 수정 시 더 이상 사용하지 않는 이미지는 실제 파일에서도 삭제됩니다.

댓글은 일반 댓글과 1단계 대댓글을 지원하며, 삭제는 soft delete 방식으로 처리합니다.
첨부파일은 uploads/attachments에 저장하고 메타데이터는 DB에 저장합니다.

예외 처리는 GlobalExceptionHandler에서 공통 ErrorResponse로 통일했고,
DB 구조는 members, boards, comments, board_attachments, refresh_tokens를 중심으로 구성되어 있습니다.
```
