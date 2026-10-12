# Balancipe

> 레시피 공유와 개인 영양 관리를 한곳에서 하는 서비스의 백엔드

제가 직접 쓰기 위해 만들고 있는 서비스입니다. 레시피를 고를 때마다 영양 성분을 따로 찾아보는 번거로움을 줄이고,
**내 신체 정보와 목표에 맞춘 영양 기준**을 바탕으로 레시피를 고를 수 있게 하는 것이 목표입니다.

**진행 상태**: MVP 개발 중 (Phase 0~2 완료) · 2026년 8월부터 학업 병행으로 일시 중단, 이후 Phase 3부터 재개 예정

<br>

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language / Framework | Java 21, Spring Boot 3.5 |
| Data | Spring Data JPA, QueryDSL, PostgreSQL 16, Redis 7 |
| Migration | Flyway |
| Auth | Spring Security, JWT (jjwt) |
| Test | JUnit 5, Mockito, AssertJ, Testcontainers |
| Infra (local) | Docker Compose |
| Docs | springdoc-openapi (Swagger UI) |

<br>

## 진행 현황

| Phase | 내용 | 상태 |
|---|---|---|
| 0 | 프로젝트 기반 (공통 응답, 전역 예외 처리, 환경별 설정, Docker Compose) | ✅ 완료 |
| 1 | 인증·사용자 (회원가입, 로그인, 토큰 재발급, 로그아웃, 프로필) | ✅ 완료 |
| 2 | 한국인 영양소 섭취기준 조회 + 개인 영양 목표 설정 | ✅ 완료 |
| 3 | 식품영양성분 DB + 공공데이터 Open API 일일 수집 (Spring Batch) | ⏳ 예정 |
| 4 | 식품 등록 요청 + 관리자 승인 | ⏳ 예정 |
| 5 | 레시피 CRUD, 좋아요·스크랩, 재료별 영양 합산 | ⏳ 예정 |
| 6 | 조회수 Redis 버퍼링 후 DB 주기 동기화 | ⏳ 예정 |

상세 체크리스트: [`backend/docs/product/plan.md`](backend/docs/product/plan.md)

<br>

## 구현한 API

| Method | Endpoint | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/auth/signup` | 회원가입 | - |
| POST | `/api/auth/login` | 로그인 | - |
| POST | `/api/auth/refresh` | Access Token 재발급 | - |
| POST | `/api/auth/logout` | 로그아웃 | - |
| GET / PUT | `/api/users/me` | 내 프로필 조회·수정 | ✅ |
| GET | `/api/nutrition/reference` | 나이·성별·임신 상태별 영양소 섭취기준 조회 | - |
| GET | `/api/nutrition/reference/all` | 전체 연령대 섭취기준 조회 (2020·2025 비교용) | - |
| GET / PUT | `/api/users/me/nutrition-goal` | 내 영양 목표 조회·설정 | ✅ |
| GET | `/api/users/me/nutrition-goal/suggested-calorie` | 목표 칼로리 제안 (저장 없는 미리보기) | ✅ |

<br>

## 주요 구현과 고민한 점

### 1. 공식 영양소 섭취기준 데이터를 정확하게 옮기기

보건복지부의 2020·2025 한국인 영양소 섭취기준 원본 PDF에서 영양소 20종의 기준값을 추출해 Flyway 시딩 마이그레이션(V3, V4)으로 적재했습니다.

- **표 추출 방식 전환**: 처음에는 pdfplumber의 표 추출 기능을 썼지만, 빈 셀이 많은 비타민 A·D·E 표에서 열이 어긋나는 오류를 발견했습니다. 그래서 글자 좌표를 기준으로 셀을 직접 묶는 방식으로 바꿔 원본 표와 대조했습니다.
- **확장 가능한 스키마**: 영양소를 고정 컬럼이 아니라 `nutrient_code` 행으로 관리해, 영양소가 늘어나도 스키마를 바꿀 필요가 없게 설계했습니다.
- **원본의 예외를 그대로 반영**: 나트륨에만 있는 만성질환위험감소섭취량(CDRR) 지표를 추가했고, 니아신 상한섭취량은 원본에서 두 값으로 나뉘어 있어 하나의 값으로 저장하면 오해가 생길 수 있다고 판단해 제외했습니다.
- **임신부·수유부 기준**: 원본은 연령 구분 없이 가산값만 제공하므로, 가임기 연령대에 같은 가산값을 저장하고 조회 시 기준값과 합산합니다.

### 2. 운영을 고려한 DB 마이그레이션

초기에는 `ddl-auto: create-drop`으로 개발하다가, 참조 데이터 적재를 위해 Flyway를 도입하면서 `validate`로 전환했습니다.
로컬은 매번 빈 DB에서 시작하고 운영 DB에는 이미 `users` 테이블이 있으므로, 프로필별로 `baseline-version`을 다르게 두어 두 환경에서 모두 같은 마이그레이션이 안전하게 적용되도록 했습니다.

### 3. JWT 인증과 Refresh Token 관리

| 항목 | 설계 |
|---|---|
| Access Token | 30분, Stateless 검증 |
| Refresh Token | 14일, Redis에 `refresh:{userId}`로 사용자당 1개 저장 (단일 세션) |
| 재발급 | 토큰 타입 확인 + Redis 저장값과 일치할 때만 발급 (재사용 방지) |
| 로그아웃 | Redis 키 삭제, 이미 삭제된 세션이어도 예외 없이 처리 (멱등) |
| 로그인 실패 | 계정 없음·비밀번호 불일치·정지 계정을 같은 응답으로 처리해 계정 존재 여부 노출 방지 |

설계 근거와 인증 흐름: [`backend/docs/specs/auth-spec.md`](backend/docs/specs/auth-spec.md)

### 4. 목표 칼로리와 탄·단·지 계산

- **목표 칼로리 제안**: Mifflin-St Jeor 공식으로 기초대사량을 구하고, 활동계수와 목표 체중 변화 속도를 반영해 하루 목표 칼로리를 제안합니다.
- **탄·단·지 계산 방식 두 가지**: 비율 프리셋(직접 입력 시 합계 100% 검증) 또는 체중 kg당 단백질·지방량을 정하고 남은 칼로리로 탄수화물을 계산하는 방식을 지원합니다.
- 계산 로직은 엔티티·서비스와 분리된 순수 클래스로 만들어 단위 테스트를 쉽게 했습니다.

### 5. 테스트

- 테스트 케이스 66개 (단위 테스트 + Bean Validation 테스트 + Repository 통합 테스트)
- Repository 통합 테스트는 **Testcontainers**로 실제 PostgreSQL 컨테이너에서 Flyway 마이그레이션까지 포함해 검증
- 마이그레이션을 실제 DB에 적용해 검증하는 과정에서, 공통 엔티티의 `created_at`/`updated_at` NOT NULL 컬럼이 시딩 SQL에서 빠져 있던 버그를 찾아 수정

<br>

## AI Agent와 함께 개발하는 방식

이 프로젝트는 AI Agent(Claude)를 활용해 설계부터 구현까지 진행하고 있습니다. 대화가 길어지거나 새 세션이 시작돼도 맥락을 잃지 않도록, 모든 결정을 문서로 남깁니다.

1. **스펙 문서** (`docs/specs/`): 구현 전에 데이터 모델, API, "왜 이 구조를 선택했는지"를 먼저 정리
2. **구현 체크리스트** (`docs/product/plan.md`): 작업 단위와 테스트 항목을 체크리스트로 관리하고, 구현 중 바뀐 결정과 그 이유를 함께 기록
3. **판단은 직접**: 스키마 범위, 데이터 처리 방식처럼 결과에 영향을 주는 결정은 제가 확정하고, 문서에 "사용자 확정"으로 남깁니다

<br>

## 실행 방법

```bash
cd backend

# PostgreSQL, Redis 실행
docker compose up -d

# 애플리케이션 실행 (local 프로필)
./gradlew bootRun --args='--spring.profiles.active=local'
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- 테스트: `./gradlew test` (Repository 통합 테스트는 Docker 필요)

<br>

## 프로젝트 구조

```
backend/src/main/java/com/beokay/balancipe
├── auth        # 회원가입, 로그인, 토큰 재발급, 로그아웃
├── user        # 사용자 프로필
├── nutrition   # 한국인 영양소 섭취기준 조회
├── goal        # 개인 영양 목표, 칼로리·탄단지 계산
└── global      # 공통 응답, 예외 처리, 보안(JWT), 설정
```