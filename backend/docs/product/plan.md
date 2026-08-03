# Balancipe 구현 체크리스트

---

## Phase 0: 프로젝트 기반 세팅
- [x] `build.gradle.kts` 의존성 추가
  - [x] `spring-boot-starter-web`
  - [x] `spring-boot-starter-data-jpa`
  - [x] `spring-boot-starter-security`
  - [x] `spring-boot-starter-validation`
  - [x] `spring-boot-starter-batch`
  - [x] `spring-boot-starter-data-redis`
  - [x] `spring-boot-starter-mail`
  - [x] `spring-boot-starter-actuator`
  - [x] `postgresql` 드라이버
  - [x] `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT)
  - [x] `springdoc-openapi-starter-webmvc-ui` (Swagger)
  - [x] `aws-java-sdk-s3` (S3 파일 업로드)
  - [x] `querydsl-jpa` (복잡한 레시피 목록 쿼리)
- [x] `application.yml` 환경별 설정 분리 (`application-local.yml`, `application-prod.yml`)
- [x] `docker-compose.yml` 작성 (PostgreSQL 16, Redis 7)
- [x] 전역 예외 처리 (`@ControllerAdvice` + `ErrorResponse` DTO)
- [x] 공통 응답 형식 정의 (`ApiResponse<T>`)

## Phase 1: 인증 및 사용자 관리
- [x] `User` Entity 및 Repository 구현
- [x] Spring Security 설정 (`SecurityFilterChain`)
- [x] JWT 발급 / 검증 필터 구현 (`JwtAuthenticationFilter`)
- [x] Refresh Token Redis 저장 구현
- [x] 회원가입 API (`POST /api/auth/signup`, 설계: `docs/specs/auth-spec.md` §3)
  - [x] `ErrorCode`에 `DUPLICATE_EMAIL`/`DUPLICATE_NICKNAME`/`INVALID_BIRTH_YEAR` 추가
  - [x] `User` 엔티티에 `create(...)` 정적 팩토리 메서드 추가
  - [x] `auth` 패키지 신설 (`controller`/`service`/`dto`)
  - [x] `SignUpRequest`/`SignUpResponse` DTO (record + Bean Validation + `@Schema`)
  - [x] `AuthService.signUp()` — 이메일/닉네임 중복 검증, 출생연도 상한 검증, 비밀번호 인코딩, User 저장, 토큰 발급 및 Redis 저장
  - [x] `AuthController.signUp()` — `@Operation`, `@Valid`, 201 + `ApiResponse.ok(...)`
  - [x] 테스트: 정상 가입, 이메일 중복, 닉네임 중복, 비밀번호 길이 위반, 출생연도 범위 위반
- [x] 로그인 API (`POST /api/auth/login`, 설계: `docs/specs/auth-spec.md` §4)
  - [x] `ErrorCode`에 `INVALID_CREDENTIALS`(401) 추가
  - [x] `LoginRequest`(email, password) / `LoginResponse`(userId, nickname, accessToken, refreshToken) DTO (record + Bean Validation + `@Schema`)
  - [x] `AuthService.login()` — `AuthenticationManager.authenticate()` 호출, 인증 실패(`BadCredentialsException`/`LockedException`/`DisabledException`) catch 후 `WARN` 로그(이메일+실패원인) 남기고 `BusinessException(INVALID_CREDENTIALS)`로 변환, 성공 시 토큰 발급 및 Redis 저장
  - [x] `AuthController.login()` — `@Operation`, `@Valid`, 200 + `ApiResponse.ok(...)`
  - [x] 테스트: 정상 로그인, 이메일 없음/비밀번호 불일치, BANNED 계정 로그인 시도 (모두 `INVALID_CREDENTIALS`로 응답 확인)
- [x] Token 재발급 API (`POST /api/auth/refresh`, 설계: `docs/specs/auth-spec.md` §5)
  - [x] `ErrorCode`에 `INVALID_REFRESH_TOKEN`(401) 추가
  - [x] `RefreshRequest`(refreshToken) / `RefreshResponse`(accessToken) DTO (record + Bean Validation + `@Schema`)
  - [x] `AuthService.refresh()` — 토큰 파싱(만료/서명 등은 `JwtProvider`가 처리), 타입이 REFRESH인지 확인, Redis 저장값과 일치 확인(재사용 방지), 사용자 role 조회 후 새 accessToken만 발급 (refreshToken은 rotation 없음)
  - [x] `AuthController.refresh()` — `@Operation`, `@Valid`, 200 + `ApiResponse.ok(...)`
  - [x] 테스트: 정상 재발급, 만료된 토큰, 토큰 타입 불일치(access를 refresh 자리에), Redis 값과 불일치(재사용 시도), Redis에 값 없음(로그아웃 이후)
- [x] 로그아웃 API (`POST /api/auth/logout`, 설계: `docs/product/research.md` 로그아웃 리서치 참고)
  - [x] `LogoutRequest`(refreshToken) DTO 신규 생성 (record + Bean Validation + `@Schema`) — `RefreshRequest`와 필드는 동일하지만 의미가 다르므로 재사용하지 않고 분리
  - [x] `AuthService.logout()` — `refreshToken` 파싱(`JwtProvider.parseClaims`), 타입이 REFRESH인지 확인(불일치 시 `INVALID_REFRESH_TOKEN`), Redis 저장값과의 일치 대조 없이 파싱된 userId로 바로 `RefreshTokenRepository.deleteByUserId()` 호출(이미 삭제된 세션이어도 예외 없이 멱등 처리)
  - [x] `AuthController.logout()` — `@Operation`, `@Valid`, 200 + `ApiResponse.ok()` (반환 데이터 없음)
  - [x] 테스트: 정상 로그아웃(Redis 키 삭제 검증), 토큰 타입 불일치(access를 refresh 자리에 → `INVALID_REFRESH_TOKEN`), 만료된 refreshToken(`EXPIRED_TOKEN`), Bean Validation(빈 refreshToken → 400)
- [x] 내 프로필 조회/수정 API (`GET/PUT /api/users/me`, 설계: `docs/product/research.md` 프로필 리서치 참고)
  - [x] `user` 패키지에 `controller`/`service`/`dto` 서브패키지 신설
  - [x] `UserProfileResponse`(userId, email, nickname, gender, birthYear, profileImageUrl) DTO (record + `@Schema`)
  - [x] `UserProfileUpdateRequest`(nickname, profileImageUrl) DTO (record + Bean Validation + `@Schema`)
  - [x] `UserService.getMyProfile()` — `userId`로 조회, `NOT_FOUND` 처리
  - [x] `UserService.updateMyProfile()` — 본인 현재 닉네임과 다를 때만 `DUPLICATE_NICKNAME` 체크, `User.updateProfile()` 호출
  - [x] `UserController` — `GET/PUT /api/users/me`, `@AuthenticationPrincipal CustomUserDetails`로 사용자 식별(코드베이스 최초 도입), `@Operation`, `ApiResponse.ok(...)`
  - [x] 테스트: 정상 조회, 정상 수정, 닉네임 중복(타인), 본인 닉네임과 동일 값(통과), 사용자 없음, Bean Validation(빈 닉네임 → 400)

## Phase 2: 한국인 영양소 섭취기준 + 목표 설정
- [x] Flyway 마이그레이션 인프라 도입
  - [x] `backend/build.gradle.kts`에 `flyway-core`, `flyway-database-postgresql` 의존성 추가
  - [x] `application.yml`에 `spring.flyway.locations`/`baseline-on-migrate: true` 추가
  - [x] `application-local.yml`의 `ddl-auto`를 `create-drop` → `validate`로 변경 + `baseline-version: 0` 추가
  - [x] `application-prod.yml`은 `ddl-auto: validate` 유지 확인 + `baseline-version: 1` 추가(기존 `users` 테이블 때문에 `V1` 스킵 필요)
  - [x] `db/migration/V1__baseline_users_table.sql` 작성 — 기존 `users` 테이블을 Flyway 베이스라인으로 등록
- [x] 나이/성별 기반 기준값 조회 API (`GET /api/nutrition/reference`, 인증 불필요)
  - [x] `nutrition` 도메인 패키지 신설 (`domain`/`repository`/`service`/`controller`/`dto`)
  - [x] Enum 구현: `AgeGroup`, `PregnancyStatus`, `NutrientCode`, `IndicatorType` — `nutrition.domain`
  - [x] `KoreanDietaryReference` Entity 구현 — 영양소 20종(다량영양소 5 + 비타민 10 + 무기질 5), `(referenceYear, gender, ageGroup, pregnancyStatus, nutrientCode, indicatorType)` 복합 유니크, 임신부/수유부 행은 가산값으로 저장. 컬럼명 `value`는 H2 등 예약어 충돌로 `reference_value`로 매핑(V1의 `users` 테이블명 회피와 동일 사유)
  - [x] `db/migration/V2__create_korean_dietary_reference_table.sql` 작성 — 테이블 DDL
  - [x] `IndicatorType`에 `CDRR`(만성질환위험감소섭취량, 나트륨 전용) 추가 — 기존 EAR/RDA/AI/UL 4종만으로는 2020/2025 원본표의 나트륨 지표를 표현할 수 없어 시딩 전에 스키마 보강
  - [x] `db/migration/V3__seed_korean_dietary_reference_2020.sql` 작성 — 사용자 제공 공식 원본 PDF(`2020_한국인_영양소_섭취기준.pdf`)를 pdfplumber 좌표 기반 추출(`extract_words` + x/y 클러스터링)로 표 셀을 정확히 매핑해 대조. `extract_tables()`만으로는 희소 셀이 많은 비타민 A/D/E 표에서 열 정렬이 깨지는 오류를 발견해 좌표 기반 방식으로 전환
  - [x] `db/migration/V4__seed_korean_dietary_reference_2025.sql` 작성 — 동일 방식으로 `2025_한국인_영양소_섭취기준.pdf` 대조
  - [x] 니아신(B3) 상한섭취량은 니코틴산/니코틴아미드 두 값으로 분리되어 있어 단일 컬럼으로 표현 시 오해 소지가 있다고 판단, 사용자 확정으로 EAR/RDA만 시딩하고 UL은 제외
  - [x] 임신부/수유부 부가량은 원본표가 연령 구분 없이 단일 행으로 제공하지만 스키마 조회는 연령대별 매칭이 필요해, 사용자 확정으로 가임기 연령대(`ADOLESCENT_15_18Y`/`ADULT_19_29Y`/`ADULT_30_49Y`/`ADULT_50_64Y`)에 동일 값을 복제해 저장(그 외 연령대에서 임신 상태 조회 시 에러 없이 기준값만 반환되는 graceful degradation)
  - [x] 검증: Docker 샌드박스 제약으로 Testcontainers 자동 테스트 실행 불가 확인 후, 로컬 `docker-compose` Postgres에 V1~V5 전체 마이그레이션을 직접 적용(스키마 재생성 포함)해 무결성 제약 위반 없음 확인. `BaseEntity`의 `created_at`/`updated_at` NOT NULL 컬럼이 INSERT문에 빠져있던 버그를 이 과정에서 발견해 수정. `local` 프로필로 앱을 기동해 `GET /api/nutrition/reference` 실제 응답을 원본표 값과 대조(기준값 및 임신부 2분기 가산 합산 결과 모두 일치 확인)
  - [x] `KoreanDietaryReferenceRepository` 구현 — 파생 쿼리만으로 충분(QueryDSL 불필요)
  - [x] `AgeGroupResolver` 구현 (2026-07-31 확장: 영아 0-11개월 구간도 지원하도록 `resolve(ageYears, ageInMonths)`로 시그니처 변경 — `age`가 0세일 때만 `ageInMonths`를 함께 참고해 0-5개월/6-11개월 구분. `@Component`로 구현해 생성자 주입 가능하게 함(정적 유틸 대신 코드베이스 DI 컨벤션과 일치시킴, 사용자 확정)
  - [x] `KoreanDietaryReferenceService` 구현 — 기준값(`pregnancyStatus=NONE`) + 임신부/수유부 가산값 합산
  - [x] `ErrorCode`에 `NUTRITION_REFERENCE_NOT_FOUND`(404)/`INVALID_PREGNANCY_STATUS`(400) 추가
  - [x] `SecurityConfig`에 `HttpMethod.GET, "/api/nutrition/**"` permitAll 추가
  - [x] `NutritionController.getReference()` — `@Operation`, `ApiResponse.ok(...)`. 쿼리 파라미터 `age`(만 나이, 필수) + `ageInMonths`(선택, `age=0`일 때만 참고)로 확장
  - [x] `build.gradle.kts`에 Testcontainers(JUnit Jupiter + PostgreSQL 모듈) 의존성 추가 — Repository 통합 테스트용, 코드베이스 최초 도입 (Flyway가 자동으로 마이그레이션을 적용하므로 별도 스키마 설정 불필요). 아티팩트명은 `spring-boot-starter-testcontainers`가 아니라 `spring-boot-testcontainers`(BOM에 `-starter-` 접두사 없음)
  - [x] 테스트: `AgeGroupResolverTest`(단위), `KoreanDietaryReferenceServiceTest`(단위, 기준값+가산값 합산/남성+임신 조합 예외), `KoreanDietaryReferenceRepositoryTest`(Testcontainers 통합, 실제 Docker 컨테이너로 검증 완료)
- [x] 전체 연령대 기준 조회 API (`GET /api/nutrition/reference/all`, 인증 불필요)
  - [x] `NutritionController.getAllReferences()` — 위 엔티티/서비스 재사용, 프론트가 `referenceYear`를 바꿔 2번 호출해 2020↔2025 비교
  - [x] 테스트: 데이터 없음(빈 결과) 시 `NUTRITION_REFERENCE_NOT_FOUND` 처리 확인(단위). 2020/2025 실제 값 비교는 V3/V4 시딩 완료 후 별도 확인 필요
- [x] 영양 목표 조회 API (`GET /api/users/me/nutrition-goal`, 인증 필요, 미설정 시 404)
  - [x] `goal` 도메인 패키지 신설 (`domain`/`repository`/`service`/`controller`/`dto`)
  - [x] Enum 구현: `MacroCalculationMethod`, `MacroPresetType`(비율 프리셋 6종 + `CUSTOM`), `ActivityLevel`(활동계수 5단계) — `goal.domain`. `PregnancyStatus`는 이미 `nutrition.domain`에 구현되어 있어(`KoreanDietaryReference`가 사용 중) 재사용하고 `goal.domain`에 중복 생성하지 않음
  - [x] `UserNutritionGoal` Entity 구현 — `userId`(FK 컬럼, `@OneToOne` 미사용), `pregnancyStatus`/`heightCm`/`currentWeightKg`/`targetWeightKg`/`activityLevel`/`weeklyRateKg`/`referenceYear`/`targetCalorie`/계산방식 필드 + 계산 결과(탄/단/지/식이섬유 목표치) 비정규화 저장
  - [x] `db/migration/V5__create_user_nutrition_goal_table.sql` 작성 — 테이블 DDL (V3/V4 시딩 마이그레이션은 아직 미생성이지만 nutrition-spec.md §4.4에서 이미 V5로 확정되어 있어 그대로 사용, Flyway는 버전 번호 연속성을 요구하지 않음)
  - [x] `UserNutritionGoalRepository` 구현 (`findByUserId`)
  - [x] `ErrorCode`에 `NUTRITION_GOAL_NOT_FOUND`(404) 추가
  - [x] `UserNutritionGoalService.getMyGoal()` — 미설정 시 `NUTRITION_GOAL_NOT_FOUND`
  - [x] `UserNutritionGoalController.getMyGoal()` — `@AuthenticationPrincipal CustomUserDetails`, `@Operation`, `ApiResponse.ok(...)`
  - [x] 테스트: 정상 조회, 목표 미설정 시 404 (`UserNutritionGoalServiceTest`, Mockito+AssertJ 컨벤션)
- [x] 영양 목표 설정 API (`PUT /api/users/me/nutrition-goal`, 인증 필요)
  - [x] `CalorieGoalCalculator` 구현 — Mifflin-St Jeor BMR + 활동계수 TDEE + 목표체중 변화율(기본 0.5kg/주, 7700kcal/kg 근사) 기반 목표 칼로리 제안, 순수 계산 로직
  - [x] `NutritionPresetCalculator` 구현 — 방식 A(비율 프리셋, `CUSTOM` 합계 100 검증) / 방식 B(체중 kg당 단백질·지방 g 지정, 잔여 칼로리로 탄수화물 산출)
  - [x] `ErrorCode`에 `INVALID_NUTRITION_GOAL`(400) 추가
  - [x] `UserNutritionGoalService.updateMyGoal()` — 임신상태 검증(남성+non-NONE → `INVALID_PREGNANCY_STATUS`), 계산 위임, `KoreanDietaryReferenceService`로 식이섬유 목표 조회, upsert
    - [x] 방식 A(`RATIO_PRESET`, `CUSTOM` 비율 합계 100 검증) 매크로 계산 처리
    - [x] 방식 B(`WEIGHT_BASED_FORMULA`, 잔여 칼로리 음수 시 `INVALID_NUTRITION_GOAL`) 매크로 계산 처리
    - [x] 목표 칼로리는 클라이언트가 보낸 최종값을 그대로 검증·저장 (서버 재계산 없음)
  - [x] `UserNutritionGoalController.updateMyGoal()` — `@Valid`, `@Operation`, `ApiResponse.ok(...)`
  - [x] 테스트: `NutritionPresetCalculatorTest`(단위, 방식 A/B 각각), 방식 A `CUSTOM` 비율 합계 100 위반, 방식 B 잔여 칼로리 음수, 남성+임신/수유 상태 조합, `NutritionGoalUpdateRequestValidationTest`(Bean Validation)
- [x] 목표 칼로리 자동 제안 API (`GET /api/users/me/nutrition-goal/suggested-calorie`, 인증 필요, 저장 없는 미리보기 전용)
  - [x] `UserNutritionGoalService.suggestCalorie()` — `CalorieGoalCalculator` 재사용, 저장하지 않고 계산 결과만 반환. 쿼리 파라미터는 `NutritionController` 기존 패턴과 동일하게 개별 `@RequestParam`으로 받음(record DTO로 묶지 않음 — GET 엔드포인트는 body가 없어 코드베이스에서 지금까지 record로 만들지 않던 패턴)
  - [x] `UserNutritionGoalController.suggestCalorie()` — `@Operation`, `ApiResponse.ok(...)`
  - [x] 테스트: `CalorieGoalCalculatorTest`(단위, 감량/증량/유지 케이스), `UserNutritionGoalServiceTest`에 정상 응답 케이스 추가

## Phase 3: 식품영양성분 DB + Open API 연동
- [ ] `FoodNutrition` Entity 및 Repository 구현
- [ ] 전문 검색 인덱스 설정 (`pg_trgm` 또는 `to_tsvector`)
- [ ] 공공데이터포털 Open API 클라이언트 구현 (`FoodNutritionApiClient`)
  - [ ] API Key 설정 (`application.yaml`)
  - [ ] 페이지네이션 처리 (100건씩 전체 수집)
- [ ] Spring Batch Job 구성 (`FoodNutritionSyncJobConfig`)
  - [ ] `FoodNutritionApiItemReader` 구현
  - [ ] `FoodNutritionItemProcessor` (DTO → Entity 변환)
  - [ ] `FoodNutritionItemWriter` (UPSERT 처리)
- [ ] `@Scheduled` 일일 새벽 3시 실행 설정
- [ ] Batch 실패 시 재시도 3회 + 로그 기록
- [ ] 식품 검색 API (`GET /api/foods?q=`)
- [ ] 식품 상세 API (`GET /api/foods/{id}`)

## Phase 4: 식품 요청 시스템
- [ ] `FoodRequest` Entity 및 Repository 구현
- [ ] 링크 도메인 화이트리스트 검증기 구현 (`LinkValidator`)
  - [ ] 네이버 스마트스토어, 네이버 쇼핑 허용
  - [ ] 쿠팡 허용
- [ ] S3 파일 업로드 서비스 구현 (`FileUploadService`)
- [ ] 식품 요청 등록 API (`POST /api/foods/requests`)
  - [ ] 사진 또는 링크 중 1개 이상 필수 검증
  - [ ] 링크 화이트리스트 검증 적용
- [ ] 내 식품 요청 목록 API (`GET /api/foods/requests/my`)
- [ ] 관리자 - 요청 목록 API (`GET /api/admin/food-requests`)
- [ ] 관리자 - 요청 승인 API (`PUT /api/admin/food-requests/{id}/approve`)
  - [ ] 영양소 수동 입력 처리 (source=MANUAL)
- [ ] 관리자 - 요청 거절 API (`PUT /api/admin/food-requests/{id}/reject`)
- [ ] ADMIN 권한 체크 (`@PreAuthorize("hasRole('ADMIN')")`)

## Phase 5: 레시피 CRUD
- [ ] `Recipe`, `RecipeIngredient`, `RecipeStep` Entity 구현
- [ ] `RecipeScrap`, `RecipeLike` Entity 구현
- [ ] Querydsl 설정 및 `RecipeQueryRepository` 구현
- [ ] 레시피 목록 API (`GET /api/recipes`)
  - [ ] 정렬: LATEST(최신), POPULAR(조회), LIKED(좋아요), SCRAPED(스크랩)
  - [ ] 페이지네이션 적용
  - [ ] 카드 응답 DTO (`RecipeCardResponse`) 반환
- [ ] 레시피 작성 API (`POST /api/recipes`)
  - [ ] 재료 목록 + 조리 단계 동시 저장
  - [ ] 썸네일 S3 업로드 처리
- [ ] 레시피 상세 API (`GET /api/recipes/{id}`)
  - [ ] 조회수 증가 (Redis 버퍼 적용)
  - [ ] 재료별 영양소 합산 계산하여 반환
- [ ] 레시피 수정 API (`PUT /api/recipes/{id}`)
  - [ ] 본인만 수정 가능 검증
- [ ] 레시피 삭제 API (`DELETE /api/recipes/{id}`)
  - [ ] 본인 또는 ADMIN만 삭제 가능
- [ ] 좋아요 토글 API (`POST/DELETE /api/recipes/{id}/like`)
- [ ] 스크랩 토글 API (`POST/DELETE /api/recipes/{id}/scrap`)

## Phase 6: 조회수 Redis 버퍼 처리
- [ ] Redis `INCR` 명령으로 조회수 버퍼링
- [ ] `@Scheduled` 5분마다 Redis → DB 동기화 (`ViewCountSyncScheduler`)
- [ ] 서버 재시작 시 미반영 조회수 손실 방지 (Redis persistence 설정)

## Phase 7: 테스트
- [ ] 단위 테스트: 링크 검증기, 프리셋 계수 계산, 영양소 합산
- [ ] 통합 테스트: 인증 플로우, 레시피 CRUD, 식품 요청 플로우
- [ ] Batch 테스트: Open API ItemReader 목 테스트
