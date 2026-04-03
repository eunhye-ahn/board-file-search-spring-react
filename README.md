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

## 3. 커스텀 어노테이션 기반 비밀번호 유효성 검사

**구현 이유**
> @Pattern을 써도되지만 나중에 비밀번호 변경같은 기능을 추가 고려

**구현 방법**
- @ValidPassword 어노테이션 생성
- ConstraintValidator 구현체에서 정책 검사 로직 작성
- @Valid와 동일하게 사용 가능

**검증 레이어**
| 레이어 | 방법 |
|--------|------|
| 서버 | @ValidPassword 커스텀 어노테이션 |
| 클라 | 실시간 유효성 검사 |

**추가
서비스레이어에서 유연하게 유효성검사를 하는 방법도 있다고 함 추가공부필요

---

## 4. 파일 업로드 구조 설계

> 유지보수 목적 — 인터페이스로 추상화 후 구현체 교체 방식

| 구분 | 설명 |
|------|------|
| `FileStorageService` (interface) | 업로드/다운로드/삭제 추상화 |
| `LocalFileStorageService` (구현체) | 개발 테스트용 저장 |
| `CloudinaryFileStorageService` (구현체) | 클라우디너리 외부저장소 저장 |
| `S3FileStorageService` (구현체) | 추후 S3로 교체 시 구현체만 변경 |

**저장소별 다운로드 방식 차이**

| 저장소 | 방식 |
|--------|------|
| Cloudinary, S3 | URL로 브라우저에서 직접 접근 |
| 로컬 | 서버에서 파일을 직접 읽어 바이너리로 변환 후 전송 |

로컬 저장소는 외부에서 직접 접근이 불가능해서 서버를 거쳐야 하므로
Cloudinary, S3 대비 로직이 복잡하고 서버 부하가 발생함

---

## 5. 페이지네이션 방식

> 스프링의 Pagable 라이브러리를 이용하여 Offset 기반으로 개발

---

## 5. 트러블슈팅 & 리팩토링 기록

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

### 공통 속성 상속 시 JPA 인식 문제

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

---

### StackOverflowError - AuthenticationManager 무한 순환 참조

**문제**
> 세션 로그인 추가했는데 테스트 중에 stackoverflowError 발생

**원인**
JWT와 달리 세션은 AuthenticationManager가 인증/인가 처리를 해주는데
UserDetailService 구현체를 안만들어서 AuthenticationManage무한 순환 발생

**세션 Login 흐름**
1. AuthenticationManager가 UserDetailService 호출
2. UserDetailService에서 인증로직처리 (DB조회 등)
4. DB 결과 반환

UserDetailServices : 인터페이스 <- 구현체 필요

---

### StackOverflowError - AuthenticationManager 무한 순환 참조

**문제**
> 세션 로그인 추가했는데 테스트 중에 stackoverflowError 발생

**원인**
`UserDetailsService` 구현체를 만들지 않아서 Spring Security가
`AuthenticationManager` → 기본 `UserDetailsService` → `AuthenticationManager`
순으로 무한 순환 참조 발생

**세션 Login 흐름**
1. 로그인 요청
2. `AuthenticationManager`가 `UserDetailsService` 호출
3. `UserDetailsService`에서 DB 조회 후 유저 정보 반환
4. 인증 성공 → 세션 생성 + `JSESSIONID` 쿠키 발급

**해결**
`UserDetailsService` 인터페이스 구현체 직접 생성

---

# 추가 공부 정리

- 세션의 장점 -> 서버에서 세션 관리
- 강제로그아웃 - 관리자가 서버에서 세션삭제 - 즉시만료
- 중복로그인 - maximumsession(1) - 기존 세션 자동 만료



