# 게시판 CRUD 구현 정리

## 1. 기능 목록

| 도메인 | 기능 |
|--------|------|
| 게시글 | 목록 조회 (페이징), 상세 조회, 작성, 수정, 삭제, 제목/내용 검색 (QueryDSL) |
| 파일 | 업로드, 다운로드, 삭제 |
| 댓글 | 목록 조회, 작성, 수정, 삭제, 작성자 표시 + 작성 시간 |
| 공통 | 작성일/수정일 자동 기록 (BaseEntity), 조회수 증가 |

---

## 2. 삭제 전략

| 도메인 | 방식 | 이유 |
|--------|------|------|
| 게시글 | 소프트 딜리트 | 댓글 등 연관 데이터 보존 |
| 댓글 | 소프트 딜리트 | 게시글과 동일 |
| 파일 | 하드 딜리트 | 실제 파일도 함께 삭제, 용량 관리 |

**소프트 딜리트 구현 방법**
- `BaseEntity`에 `deletedAt` 컬럼 추가
- `@SQLDelete`로 DELETE 쿼리를 UPDATE로 오버라이드
- `@Where(clause = "deleted_at is null")`로 조회 시 자동 필터링

---

## 3. 파일 업로드 구조 설계

> 유지보수 목적 — 인터페이스로 추상화 후 구현체 교체 방식

| 구분 | 설명 |
|------|------|
| `FileStorageService` (interface) | 업로드/다운로드/삭제 추상화 |
| `LocalFileStorageService` (구현체) | 로컬 파일시스템 저장 |
| `S3FileStorageService` (구현체) | 추후 S3로 교체 시 구현체만 변경 |

---

## 4. 페이지네이션 방식

> Offset 기반으로 개발 후 댓글은 Cursor 기반으로 추후 고려


---

## 5. 트러블슈팅 & 리팩토링 기록


### contentType 컬럼 추가 (브라우저 바로보기)

**문제**
> 파일 바로보기 시 브라우저가 파일 종류를 알 수 없어 올바르게 렌더링하지 못함

**원인**
HTTP 응답의 `Content-Type` 헤더에 MIME 타입이 없으면 브라우저가 파일 처리 방식을 판단하지 못함.
File 엔티티에 contentType 정보가 없어 응답 헤더에 포함할 수 없었음

**해결**
- File 엔티티에 `contentType` 컬럼 추가
- 파일 업로드 시 contentType 저장
- 조회 응답 시 헤더에 포함
```
  .contentType(MediaType.parseMediaType(response.getContentType()))
```

---

### 의존성 리팩토링 (File ↔ Post 순환 의존 방지)

**문제**
> File이 Post를 `@ManyToOne`으로 참조하는 구조에서 PostService ↔ FileService 간 강결합 발생

**원인**
FileService에서 Post 객체 없이 `postId(Long)`만 넘기면 JPA 연관관계가 끊겨 FK가 null이 됨.
이를 해결하려다 FileService가 Post 객체를 직접 받게 되면서 의존성이 높아짐

> JPA는 save() 시 매핑된 필드 기반으로 INSERT 쿼리를 생성함
> `@ManyToOne` 필드에 실제 Post 객체(프록시 포함)가 존재해야 FK 컬럼에 값이 채워짐

**해결**
- PostService에서 FileService를 호출해 트랜잭션을 PostService가 관리
- FileService는 `save` 시에만 Post 객체를 받고, 나머지는 `postId`로만 접근
- PostController는 PostService만 호출 (FileService 직접 호출 X)
- DTO에는 도메인 객체 포함 X, ID 등 식별자만 전달

---

### 3. 공통 속성 상속 시 JPA 인식 문제

**문제**
> 글 상세 조회 시 `createdAt`이 null로 반환됨

**원인**
JPA는 기본적으로 `@Entity`가 붙은 클래스의 필드만 매핑 정보로 인식한다.
부모 클래스에 아무 어노테이션이 없으면 JPA가 해당 필드를 무시하고,
SELECT 결과를 리플렉션으로 주입할 때 부모 필드가 null이 됨

**JPA 조회 흐름**
1. Repository에서 조회 메서드 호출
2. JPA가 `@Entity` 클래스 분석 → 매핑된 필드 확인
3. 매핑 정보 기반으로 SELECT 쿼리 생성
4. DB 결과 반환
5. 리플렉션으로 엔티티 필드에 값 주입 → 객체 완성 후 반환

`@MappedSuperclass`가 없으면 2단계에서 부모 필드가 매핑 정보에 포함되지 않아 5단계에서 null이 됨

**해결**
공통 속성을 가진 부모 클래스에 `@MappedSuperclass` 추가
→ 독립 테이블 없이 자식 엔티티 테이블 컬럼으로 포함되어 정상 주입됨
