# 왜 이렇게 만들었나 — 설계 결정 이유 정리

> 각 기능을 구현할 때 "이렇게 해야 한다"고 선택한 이유를 중심으로 정리한 문서입니다.  
> "어떻게 구현했는가"는 PRESENTATION.md를 참고하세요.

---

## 목차

1. [인증 방식 — 왜 JWT인가?](#1-인증-방식--왜-jwt인가)
2. [Refresh Token — 왜 DB에 저장하고 해시로 보관하나?](#2-refresh-token--왜-db에-저장하고-해시로-보관하나)
3. [토큰 로테이션 — 왜 재발급마다 기존 토큰을 폐기하나?](#3-토큰-로테이션--왜-재발급마다-기존-토큰을-폐기하나)
4. [tokenType 클레임 — 왜 토큰 종류를 구분하나?](#4-tokentype-클레임--왜-토큰-종류를-구분하나)
5. [ORM 선택 — 왜 JPA 대신 MyBatis인가?](#5-orm-선택--왜-jpa-대신-mybatis인가)
6. [댓글 소프트 삭제 — 왜 실제로 지우지 않나?](#6-댓글-소프트-삭제--왜-실제로-지우지-않나)
7. [대댓글 1단계 제한 — 왜 무한 중첩을 막았나?](#7-대댓글-1단계-제한--왜-무한-중첩을-막았나)
8. [writer 컬럼 스냅샷 — 왜 닉네임을 게시글에 따로 저장하나?](#8-writer-컬럼-스냅샷--왜-닉네임을-게시글에-따로-저장하나)
9. [첨부파일 UUID 파일명 — 왜 원본 파일명으로 저장하지 않나?](#9-첨부파일-uuid-파일명--왜-원본-파일명으로-저장하지-않나)
10. [첨부파일 보상 처리 — 왜 파일 정리를 직접 해야 하나?](#10-첨부파일-보상-처리--왜-파일-정리를-직접-해야-하나)
11. [이미지 삭제 — 왜 게시글 삭제 시 이미지도 직접 지우나?](#11-이미지-삭제--왜-게시글-삭제-시-이미지도-직접-지우나)
12. [커스텀 예외 — 왜 RuntimeException을 그냥 쓰지 않나?](#12-커스텀-예외--왜-runtimeexception을-그냥-쓰지-않나)
13. [GlobalExceptionHandler — 왜 컨트롤러에 try-catch를 안 쓰나?](#13-globalexceptionhandler--왜-컨트롤러에-try-catch를-안-쓰나)
14. [인터페이스 + 구현체 분리 — 왜 BoardService와 BoardServiceImpl을 나눴나?](#14-인터페이스--구현체-분리--왜-boardservice와-boardserviceimpl을-나눴나)
15. [도메인별 패키지 분리 — 왜 기능 단위로 폴더를 나눴나?](#15-도메인별-패키지-분리--왜-기능-단위로-폴더를-나눴나)
16. [DTO 분리 — 왜 엔티티를 직접 응답으로 쓰지 않나?](#16-dto-분리--왜-엔티티를-직접-응답으로-쓰지-않나)
17. [검색 ILIKE — 왜 LIKE 대신 ILIKE인가?](#17-검색-ilike--왜-like-대신-ilike인가)
18. [인덱스 — 왜 이 컬럼들에 인덱스를 걸었나?](#18-인덱스--왜-이-컬럼들에-인덱스를-걸었나)
19. [회원 소프트 삭제 — 왜 회원을 실제로 지우지 않나?](#19-회원-소프트-삭제--왜-회원을-실제로-지우지-않나)
20. [배포 — 왜 Docker + GitHub Actions인가?](#20-배포--왜-docker--github-actions인가)

---

## 1. 인증 방식 — 왜 JWT인가?

**결론: 서버가 상태를 저장하지 않아도 되는 Stateless 인증이 필요했습니다.**

세션 방식은 서버 메모리(또는 DB)에 사용자 로그인 상태를 저장합니다. 요청이 올 때마다 "이 사람이 로그인 중인가?"를 확인하러 서버가 저장소를 조회해야 합니다.

JWT는 다릅니다. 토큰 자체에 사용자 정보(memberId, role)가 들어 있고, 서버가 발급한 서명(Signature)으로 위조 여부를 즉시 판별할 수 있습니다. DB를 뒤질 필요가 없습니다.

```
세션 방식: 요청 → 서버가 DB/메모리에서 세션 조회 → 결과 반환
JWT 방식:  요청 → 서버가 서명만 검증 (DB 조회 없음) → 결과 반환
```

이 차이는 서버를 여러 대로 늘릴 때(수평 확장) 큰 의미가 있습니다.  
세션은 서버 A에 저장된 세션을 서버 B가 모르기 때문에 세션 공유 저장소가 따로 필요합니다.  
JWT는 어느 서버에서 검증해도 결과가 같습니다.

---

## 2. Refresh Token — 왜 DB에 저장하고 해시로 보관하나?

**결론: "토큰을 서버에서 강제로 무효화"하려면 DB가 필요하고, 보안을 위해 원본이 아닌 해시를 저장합니다.**

### 왜 DB에 저장하나?

JWT는 본래 서버가 아무것도 저장하지 않는 방식입니다. 그런데 그러면 한 가지 문제가 생깁니다.

> "한 번 발급한 토큰은 만료될 때까지 취소할 방법이 없다."

로그아웃을 해도 서버는 토큰을 폐기할 수 없습니다. 탈취당해도 마찬가지입니다.

Access Token 수명을 1시간으로 짧게 잡으면 위험 노출 시간이 줄지만, 그러면 사용자가 1시간마다 다시 로그인해야 합니다. 이 불편함을 해결하기 위해 Refresh Token(7일)을 도입했습니다.

Refresh Token은 "Access Token 재발급 전용 토큰"입니다. 이걸 DB에 저장해두면 언제든 서버에서 `revokedAt`을 설정해 폐기할 수 있습니다. 로그아웃 → Refresh Token 폐기 → 이후 재발급 불가.

### 왜 해시로 저장하나?

DB에 원본 토큰 문자열을 그대로 저장하면, DB가 탈취됐을 때 공격자가 그 토큰을 바로 사용할 수 있습니다.

SHA-256은 단방향 함수입니다. 해시값으로 원본을 복원할 수 없습니다.

```
저장: SHA-256(refreshToken) → DB에 64자 해시 저장
검증: 들어온 토큰을 SHA-256 → DB 해시와 비교 (원본은 DB에 없음)
```

비밀번호를 BCrypt로 저장하는 것과 같은 원리입니다. DB가 털려도 토큰 자체는 안전합니다.

---

## 3. 토큰 로테이션 — 왜 재발급마다 기존 토큰을 폐기하나?

**결론: Refresh Token이 탈취됐을 때 피해를 한 번의 사용으로 제한하기 위해서입니다.**

토큰 로테이션 없이 재발급하면 이런 상황이 생깁니다.

```
1. 공격자가 피해자의 Refresh Token을 탈취
2. 피해자는 모른 채 계속 서비스를 이용 (기존 토큰 재사용)
3. 공격자도 동일한 Refresh Token으로 Access Token을 계속 발급받음
4. 피해자가 로그아웃할 때까지 공격자도 계속 접근 가능
```

토큰 로테이션을 적용하면 달라집니다.

```
1. 공격자가 탈취한 Refresh Token으로 재발급 요청
2. 서버: 기존 토큰 폐기 → 새 토큰 발급 (공격자에게 감)
3. 피해자가 다음에 재발급 요청 → "이미 폐기된 토큰"으로 거부
4. 피해자는 다시 로그인해야 하는 상황이 되어 공격을 인지
```

Refresh Token이 탈취돼도 한 번 사용되면 무효화됩니다. 피해 범위가 줄어듭니다.

---

## 4. tokenType 클레임 — 왜 토큰 종류를 구분하나?

**결론: Access Token과 Refresh Token을 서로의 역할에 잘못 사용하는 혼용 공격을 막기 위해서입니다.**

tokenType 없이 두 토큰을 운용하면 이런 공격이 가능합니다.

```
공격 시나리오:
- Refresh Token을 Authorization 헤더에 넣어 API를 호출 → 서버가 서명만 보고 허용할 수도 있음
- Access Token을 /api/members/refresh에 넣어 재발급 요청 → 서버가 서명만 보고 허용할 수도 있음
```

두 토큰의 서명 키가 같다면 서버는 "이 토큰이 어떤 용도로 발급됐는지" 알 수 없습니다.

`tokenType: "access"` / `tokenType: "refresh"` 클레임을 넣으면, 필터에서 Access Token 전용 검증, 재발급 API에서 Refresh Token 전용 검증을 명시적으로 할 수 있습니다. 잘못된 종류의 토큰은 즉시 거부됩니다.

---

## 5. ORM 선택 — 왜 JPA 대신 MyBatis인가?

**결론: 이 프로젝트에서 쓰는 SQL을 직접 제어하고 싶었고, SQL 자체를 공부하는 목적도 있었습니다.**

JPA(Hibernate)는 객체를 저장하면 알아서 SQL을 생성해줍니다. 간단한 CRUD에서는 편리하지만, 복잡한 쿼리를 쓸 때 여러 문제가 생깁니다.

- **N+1 문제**: 게시글 목록을 가져올 때 각 게시글의 작성자 정보를 별도 쿼리로 N번 더 조회하는 문제. `@EntityGraph`, `fetch join` 같은 추가 설정이 필요합니다.
- **예측하기 어려운 SQL**: JPA가 생성하는 SQL이 의도와 다를 수 있고, 디버깅이 어렵습니다.
- **페이지네이션**: `LIMIT/OFFSET` 쿼리를 JPA로 표현할 수 있지만, 복잡한 정렬 조건이 붙으면 JPQL이 길어집니다.

MyBatis는 SQL을 XML에 직접 씁니다.

```xml
<select id="findAll" resultType="Board">
  SELECT b.*, COALESCE(m.nickname, b.writer) as writer
  FROM boards b
  LEFT JOIN members m ON b.member_id = m.id
  ORDER BY b.created_at DESC
  LIMIT #{size} OFFSET #{offset}
</select>
```

어떤 SQL이 실행될지 정확히 알 수 있습니다. 복합 인덱스를 활용하는 쿼리도 의도대로 제어할 수 있습니다. 이 프로젝트처럼 SQL 자체를 배우고 싶은 목적이라면 MyBatis가 더 맞습니다.

---

## 6. 댓글 소프트 삭제 — 왜 실제로 지우지 않나?

**결론: 댓글을 실제로 삭제하면 대댓글 스레드 구조가 깨집니다.**

댓글 구조를 생각해보면 이해가 쉽습니다.

```
댓글 A
  └── 대댓글 A-1
  └── 대댓글 A-2
댓글 B
```

만약 "댓글 A"를 DB에서 실제로 삭제(하드 삭제)하면, 대댓글 A-1과 A-2는 부모가 없어집니다.

- 대댓글도 같이 지우면: 사용자가 "삭제된 댓글에 달린 내 대댓글도 사라졌다"며 당황합니다.
- 대댓글만 남기면: parent_id가 가리키는 행이 없는 고아 레코드가 생겨 구조가 무너집니다.

소프트 삭제는 `deleted_at` 타임스탬프만 설정하고 행은 그대로 둡니다.

```
댓글 A → "삭제된 댓글입니다." 표시 (deleted_at 있음)
  └── 대댓글 A-1 → 그대로 표시
  └── 대댓글 A-2 → 그대로 표시
```

스레드 구조가 유지되고, 대댓글 작성자의 글도 사라지지 않습니다.

---

## 7. 대댓글 1단계 제한 — 왜 무한 중첩을 막았나?

**결론: 무한 중첩은 UI와 쿼리 모두 복잡해지고, 실용적인 이득이 없습니다.**

무한 중첩 댓글을 지원하려면 두 가지가 필요합니다.

**DB 조회**: 재귀 쿼리(PostgreSQL CTE, `WITH RECURSIVE`)가 필요합니다. 깊이 제한 없이 재귀하면 성능이 예측하기 어렵습니다.

```sql
-- 무한 중첩이면 이런 재귀 쿼리가 필요
WITH RECURSIVE comment_tree AS (
  SELECT * FROM comments WHERE parent_id IS NULL
  UNION ALL
  SELECT c.* FROM comments c JOIN comment_tree ct ON c.parent_id = ct.id
)
SELECT * FROM comment_tree;
```

**UI**: 중첩이 깊어질수록 인덴트가 쌓여 모바일에서 읽기 어려워집니다.

네이버, 다음, 유튜브 등 대부분의 서비스가 1단계 대댓글까지만 허용하는 이유가 여기 있습니다. 실용적으로 충분한 기능이고, 구현도 단순합니다.

```java
// parent가 이미 대댓글이면 거부
if (parent.getParentId() != null) {
    throw new IllegalArgumentException("대댓글에는 답글을 작성할 수 없습니다.");
}
```

---

## 8. writer 컬럼 스냅샷 — 왜 닉네임을 게시글에 따로 저장하나?

**결론: 회원이 탈퇴하면 JOIN으로 닉네임을 가져올 수 없게 되기 때문입니다.**

게시글 목록을 보여줄 때 "누가 썼는지" 표시해야 합니다.

단순하게 설계하면 `members` 테이블과 JOIN해서 닉네임을 가져옵니다.

```sql
SELECT b.*, m.nickname FROM boards b JOIN members m ON b.member_id = m.id
```

그런데 회원이 탈퇴해서 `members` 테이블에서 삭제되면, 이 쿼리는 해당 게시글의 닉네임을 가져오지 못합니다.

`writer` 컬럼은 게시글 작성 시점의 닉네임을 "스냅샷"으로 저장해둔 폴백용입니다.

```sql
-- 현재 닉네임이 있으면 그것을, 없으면 작성 당시 닉네임을 사용
SELECT COALESCE(m.nickname, b.writer) as writer FROM boards b LEFT JOIN members m ON ...
```

또한, 닉네임을 변경하면 기존 게시글에도 변경된 닉네임이 반영됩니다. `writer` 컬럼은 회원이 살아있는 동안에는 사용되지 않고, 탈퇴 후에만 폴백으로 쓰입니다.

---

## 9. 첨부파일 UUID 파일명 — 왜 원본 파일명으로 저장하지 않나?

**결론: 원본 파일명으로 저장하면 충돌, 경로 조작 공격, 민감 정보 노출 위험이 있습니다.**

사용자가 올린 파일명을 그대로 저장하면 여러 문제가 생깁니다.

**파일명 충돌**: 두 사용자가 같은 이름의 파일(예: `report.pdf`)을 올리면 하나가 덮어씌워집니다.

**Path Traversal 공격**: 파일명이 `../../etc/passwd`처럼 디렉토리 탈출 문자를 포함할 수 있습니다.

**원본 파일명 노출**: 회사 내부 파일명(`2024-Q4-매출보고서-비공개.xlsx`)이 URL에 노출됩니다.

UUID로 저장하면 이 세 가지가 모두 해결됩니다.

```
원본 파일명: 2024-Q4-매출보고서-비공개.xlsx  → DB에만 보관 (다운로드 시 Content-Disposition에 사용)
저장 파일명: a3f8c2d1-9b4e-4f7a-8c3d-1e5b6a7c8d9e.xlsx  → 디스크에 저장
```

다운로드 시에는 `Content-Disposition: attachment; filename="원본파일명"` 헤더를 붙여서 사용자에게는 원본 파일명으로 보이게 합니다.

---

## 10. 첨부파일 보상 처리 — 왜 파일 정리를 직접 해야 하나?

**결론: DB 트랜잭션과 파일 시스템은 같은 트랜잭션으로 묶을 수 없기 때문입니다.**

DB 작업은 `@Transactional`로 롤백이 자동입니다. 예외가 발생하면 INSERT됐던 행이 자동으로 사라집니다.

파일 시스템은 다릅니다. 파일을 디스크에 저장했다가 이후 DB 작업에서 예외가 나도, 이미 저장된 파일이 자동으로 지워지지 않습니다.

```
시나리오:
1. 파일 A 저장 완료 (디스크에 존재)
2. 파일 B 저장 완료 (디스크에 존재)
3. 파일 C 저장 중 예외 발생!
→ DB는 롤백 → 파일 A, B의 DB 레코드 사라짐
→ 디스크에는 파일 A, B가 여전히 존재 (고아 파일)
```

이 불일치를 해결하려면 catch 블록에서 이미 저장한 파일들을 직접 삭제해야 합니다.

```java
try {
    storageService.save(file);     // 디스크 저장
    attachmentMapper.save(...);    // DB 삽입
} catch (Exception e) {
    deleteFiles(savedStoredNames); // 이미 저장된 파일 정리 (보상 처리)
    throw e;                       // 예외는 다시 던져서 GlobalExceptionHandler가 처리
}
```

이처럼 DB 트랜잭션이 보장하지 못하는 외부 자원(파일, 외부 API 등)은 직접 보상 처리 로직을 작성해야 합니다.

---

## 11. 이미지 삭제 — 왜 게시글 수정/삭제 시 이미지를 직접 지우나?

**결론: DB에서 게시글이 변경/삭제되어도 디스크의 이미지 파일은 자동으로 사라지지 않기 때문입니다.**

Toast UI Editor로 본문에 이미지를 삽입하면, 이미지는 `/uploads/uuid.jpg` 형태로 디스크에 저장됩니다. 이 경로는 게시글 본문(TEXT 컬럼)에 HTML 태그 안에 들어 있습니다. DB는 이 파일의 존재를 별도로 추적하지 않기 때문에, 파일 정리는 서비스 코드에서 직접 해야 합니다.

### 게시글 수정 시 — 왜 "제거된 이미지만" 삭제하나?

수정 전 본문에 이미지 A, B, C가 있고 수정 후 본문에 A, B만 남았다면, 사라진 C만 삭제해야 합니다. A와 B를 같이 지우면 새 본문에서 여전히 표시되어야 할 이미지가 없어집니다.

이를 위해 수정 전후 본문에서 이미지 URL을 각각 추출하고 차집합을 구합니다.

```java
Set<String> oldImageUrls = extractImageUrls(oldContent); // {A, B, C}
Set<String> newImageUrls = extractImageUrls(newContent); // {A, B}
oldImageUrls.removeAll(newImageUrls); // → {C} (사라진 이미지만 남김)
oldImageUrls.forEach(this::deleteImage); // C만 삭제
```

정규식은 `/uploads/attachments/` 경로를 의도적으로 제외합니다.

```
/uploads/(?!attachments/)[^\s"')\]]+
```

첨부파일도 `/uploads/` 하위에 저장되지만, 첨부파일은 `BoardAttachmentService`가 따로 관리합니다. 이미지 정리 로직이 첨부파일까지 건드리면 서로 책임이 충돌하므로, 정규식으로 분리합니다.

### 게시글 삭제 시 — 왜 이미지 삭제를 DB 삭제보다 먼저 하나?

```java
imageService.deleteImages(board.getContent());       // 1. 이미지 삭제
boardMapper.delete(id);                              // 2. DB 삭제
attachmentService.deleteFiles(attachmentStoredNames); // 3. 첨부파일 삭제
```

본문 내용(`board.getContent()`)은 게시글이 DB에서 삭제되기 전에 읽어야 합니다. DB를 먼저 삭제하면 본문을 가져올 방법이 없으므로 이미지 경로를 파싱하지 못합니다. 그래서 이미지 삭제를 반드시 DB 삭제보다 먼저 합니다.

이미지 삭제 실패는 예외를 던지지 않고 로깅만 합니다. 이미지 파일 삭제에 실패했다고 게시글 삭제 자체가 막히는 건 바람직하지 않기 때문입니다.

---

## 12. 커스텀 예외 — 왜 RuntimeException을 그냥 쓰지 않나?

**결론: 예외 타입만 보고 어느 도메인의 어느 작업이 실패했는지 바로 알 수 있어야 정확한 HTTP 상태코드를 반환할 수 있습니다.**

`throw new RuntimeException("게시글을 찾을 수 없습니다.")` 이렇게만 하면 어떤 문제가 생기냐면, `GlobalExceptionHandler`에서 이 예외가 "없는 게시글 조회"인지, "없는 댓글 조회"인지, 아니면 "DB 연결 실패"인지 구별할 수 없습니다.

구별할 수 없으면 전부 500 Internal Server Error로 반환하게 됩니다.

```java
// 이러면 404를 내야 할 상황에서 어떤 에러인지 알 수 없음
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<?> handle(RuntimeException e) { ... }
```

커스텀 예외를 만들면 타입으로 정확하게 구별됩니다.

```java
@ExceptionHandler(BoardNotFoundException.class)
public ResponseEntity<?> handleBoardNotFound(...) {
    return ResponseEntity.status(404)...;
}

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<?> handleForbidden(...) {
    return ResponseEntity.status(403)...;
}
```

`BoardNotFoundException`이 던져지면 404, `AccessDeniedException`이면 403. 명확합니다.

---

## 13. GlobalExceptionHandler — 왜 컨트롤러에 try-catch를 안 쓰나?

**결론: 에러 처리를 한 곳에 모아야 코드가 중복되지 않고, 응답 형식을 통일할 수 있습니다.**

모든 컨트롤러 메서드에 try-catch를 쓰면 이런 모습이 됩니다.

```java
public ResponseEntity<?> getBoard(Long id) {
    try {
        Board board = boardService.findById(id);
        return ResponseEntity.ok(board);
    } catch (BoardNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse(false, "서버 오류"));
    }
}
```

이 코드가 컨트롤러 메서드마다 반복됩니다. `@RestControllerAdvice`를 사용하면 예외 처리를 한 군데에서 합니다.

```java
// 컨트롤러는 예외를 그냥 위로 던지면 됨
public BoardResponse getBoard(Long id) {
    return boardService.findById(id); // BoardNotFoundException 나면 그냥 전파
}

// 예외 처리는 GlobalExceptionHandler가 담당
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<?> handle(BoardNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
    }
}
```

컨트롤러가 비즈니스 로직에만 집중할 수 있고, 에러 응답 형식(`{success: false, message: "..."}`)이 전체적으로 통일됩니다. 프론트엔드도 에러 처리 로직을 하나로 작성할 수 있습니다.

---

## 14. 인터페이스 + 구현체 분리 — 왜 BoardService와 BoardServiceImpl을 나눴나?

**결론: 테스트할 때 실제 DB 없이 가짜 구현체(Mock)로 대체할 수 있고, 구현 방식을 바꿔도 호출하는 쪽 코드를 수정할 필요가 없습니다.**

컨트롤러는 `BoardService` 인터페이스에만 의존합니다.

```java
public class BoardController {
    private final BoardService boardService; // 인터페이스에 의존
}
```

실제 운영에서는 `BoardServiceImpl`이 주입되고, 테스트에서는 `MockBoardService`가 주입될 수 있습니다. 컨트롤러 코드를 전혀 바꾸지 않아도 됩니다.

또한 나중에 구현 방식을 바꿔도(예: MyBatis → JPA) 컨트롤러는 영향을 받지 않습니다. 변경 영향 범위가 구현체 내부로 제한됩니다.

실무에서는 이 패턴이 거의 표준처럼 사용됩니다. 처음에는 오버엔지니어링처럼 보일 수 있지만, 프로젝트가 커질수록 의미가 커집니다.

---

## 15. 도메인별 패키지 분리 — 왜 기능 단위로 폴더를 나눴나?

**결론: 관련된 코드가 한 곳에 모여 있어야 기능을 수정하거나 삭제할 때 영향 범위를 파악하기 쉽습니다.**

두 가지 방식이 있습니다.

**레이어별 분리** (안 좋은 예):
```
controller/  BoardController, MemberController, CommentController ...
service/     BoardService, MemberService, CommentService ...
mapper/      BoardMapper, MemberMapper, CommentMapper ...
```

**도메인별 분리** (현재 방식):
```
board/    controller, service, mapper, domain, dto
member/   controller, service, mapper, domain, dto
comment/  controller, service, mapper, domain, dto
```

"댓글 기능을 통째로 삭제해야 한다"고 할 때, 레이어별 분리라면 controller, service, mapper 폴더를 각각 뒤져서 comment 관련 파일을 찾아야 합니다. 도메인별 분리라면 `comment/` 폴더 하나만 삭제하면 됩니다.

기능을 추가할 때도 마찬가지입니다. `comment/` 폴더 안에서만 작업이 이루어지므로, 다른 도메인에 영향을 미칠 가능성이 낮습니다.

---

## 16. DTO 분리 — 왜 엔티티를 직접 응답으로 쓰지 않나?

**결론: 엔티티를 그대로 반환하면 내부 필드가 모두 외부에 노출되고, API 구조가 DB 구조에 종속됩니다.**

`Board` 엔티티에는 `memberId` 같은 내부 식별자가 있습니다. 이걸 API 응답으로 그대로 보내면 프론트엔드에 "이 게시글의 DB 기본키는 3번 사용자가 쓴 글"이라는 정보가 노출됩니다. 불필요한 정보이고, 악용될 수도 있습니다.

```java
// 엔티티 직접 반환 (나쁜 예)
// → memberId, passwordHash 같은 내부 정보까지 노출될 수 있음
return board;

// DTO로 변환 후 반환 (현재 방식)
// → 외부에 보여줄 필드만 선택해서 반환
return new BoardResponse(board.getId(), board.getTitle(), board.getWriter(), ...);
```

또한 DB 컬럼명이 바뀌어도 DTO 필드명은 유지할 수 있으므로, API 계약(클라이언트와 약속한 응답 형태)이 DB 변경에 영향받지 않습니다.

---

## 17. 검색 ILIKE — 왜 LIKE 대신 ILIKE인가?

**결론: 한국어는 대소문자 구분이 없지만, 영문 검색어를 입력할 때 대소문자를 구분하지 않아야 더 편리합니다.**

`LIKE`는 대소문자를 구분합니다. `spring`으로 검색하면 `Spring`, `SPRING`은 히트되지 않습니다.

PostgreSQL의 `ILIKE`(case-Insensitive LIKE)는 대소문자를 구분하지 않습니다.

```sql
-- LIKE: 'spring'만 매칭
b.title LIKE '%spring%'

-- ILIKE: 'spring', 'Spring', 'SPRING' 모두 매칭
b.title ILIKE '%spring%'
```

사용자 입장에서 검색창에 대소문자를 신경 쓰지 않아도 되는 편의성을 위해 `ILIKE`를 선택했습니다.

---

## 18. 인덱스 — 왜 이 컬럼들에 인덱스를 걸었나?

**결론: 쿼리에서 자주 조회 조건으로 쓰이는 컬럼에 인덱스가 없으면 테이블 전체를 스캔해야 합니다.**

인덱스는 책의 색인과 같습니다. 색인이 없으면 찾는 단어가 있는 페이지를 찾으려고 책 전체를 읽어야 합니다.

**`idx_comments_board_id`**: 특정 게시글의 댓글을 가져올 때 `WHERE board_id = ?` 조건을 씁니다. 이 컬럼에 인덱스가 없으면 모든 댓글 행을 스캔합니다.

**`idx_comments_parent_id`**: 대댓글을 가져올 때 `WHERE parent_id = ?`를 씁니다.

**`idx_comments_board_parent_created` (복합 인덱스)**: 댓글 목록을 "게시글별, 최신순"으로 정렬해 가져오는 쿼리가 `board_id`, `parent_id`, `created_at` 세 컬럼을 동시에 사용합니다. 이 세 컬럼을 묶은 복합 인덱스를 만들면 정렬 쿼리를 인덱스만으로 처리할 수 있습니다.

**`idx_refresh_tokens_token_hash`**: 재발급 요청마다 `WHERE token_hash = ?`로 조회합니다. 해시 값은 고유하고, 이 조회가 매우 빈번하므로 인덱스가 필수입니다.

---

## 19. 회원 소프트 삭제 — 왜 회원을 실제로 지우지 않나?

**결론: 회원을 하드 삭제하면 그 회원이 쓴 게시글, 댓글의 참조가 깨집니다. 또한 법적으로 일정 기간 데이터 보존 의무가 있을 수 있습니다.**

회원 행을 `DELETE`로 지우면, `boards.member_id`가 가리키던 행이 사라집니다. FK 제약이 있으면 삭제 자체가 실패하거나, CASCADE 설정에 따라 그 회원이 쓴 게시글까지 연쇄 삭제됩니다.

대신 `status = 'WITHDRAWN'`, `deleted_at` 설정으로 "탈퇴한 회원"으로 표시만 합니다. 게시글은 그대로 남고, `writer` 스냅샷 컬럼 덕분에 닉네임도 표시됩니다.

또한 로그인 시 `status = 'ACTIVE'` 조건을 확인하므로, 탈퇴 회원은 로그인이 차단됩니다.

---

## 20. 배포 — 왜 Docker + GitHub Actions인가?

**결론: "내 컴퓨터에서는 됐는데 서버에서 안 된다"는 문제를 없애고, 코드 push만으로 자동 배포되게 하기 위해서입니다.**

### 왜 Docker인가?

Spring Boot 앱, PostgreSQL, React Nginx 세 개가 각각의 컨테이너로 실행됩니다.

컨테이너는 실행 환경(OS, JDK 버전, PostgreSQL 버전 등)을 이미지 안에 포함합니다. 개발 PC에서 잘 돌아가던 이미지가 EC2에서도 동일하게 동작합니다. 환경 차이로 생기는 "내 환경에서만 됨" 문제가 사라집니다.

`docker compose`로 세 컨테이너를 한 번에 올리고 내릴 수 있어 배포 명령이 단순해집니다.

### 왜 GitHub Actions인가?

`git push` 이벤트에 반응해 자동으로 배포 스크립트를 실행합니다.

```
git push → GitHub Actions 트리거 → EC2 SSH 접속 → git pull → 빌드 → docker compose 재시작
```

수동으로 EC2에 SSH 접속해서 명령어를 하나씩 치는 과정을 자동화합니다. 사람이 직접 배포하면 실수가 생기고 과정이 문서화되지 않습니다. GitHub Actions는 배포 과정이 `deploy.yml` 파일로 코드화되어 있어 누가 봐도 배포 순서를 알 수 있습니다.

### 왜 Volume을 설정하나?

`docker compose down` 후 `up`을 하면 컨테이너가 새로 만들어집니다. Volume 설정이 없으면 PostgreSQL 데이터가 새 컨테이너와 함께 초기화됩니다. 배포할 때마다 DB가 초기화되면 안 됩니다.

```yaml
volumes:
  board_pg_data:  # 컨테이너가 교체되어도 PostgreSQL 데이터는 이 볼륨에 유지
```

`restart: always`도 같은 이유입니다. EC2 서버가 재부팅되어도 컨테이너가 자동으로 다시 올라옵니다.
