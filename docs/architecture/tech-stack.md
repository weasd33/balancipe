## 기술 스택

### Backend
- **Language:**: Java 21
- **Framework**: Spring Boot 3.5.16
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security 6 + JWT
- **Scheduling**: Spring Batch + `@Scheduled` (Open API 일일 수집)
- **Validation**: Jakarta Validation
- **Database**: PostgreSQL 16
- **Cache**: Redis (조회수, 스크랩수, 좋아요수 실시간 집계 버퍼)
- **File Storage**: AWS S3 (레시피 썸네일, 식품 요청 사진)
- **API Docs**: SpringDoc OpenAPI (Swagger UI)


### Frontend (미정)
- **Framework**: React 18 + TypeScript (별도 프로젝트로 분리, `balancipe-web`)
- **UI Library**: Tailwind CSS + shadcn/ui (쇼핑몰형 카드 레이아웃)
- **상태 관리**: React Query (서버 상태) + Zustand (클라이언트 상태)
- **HTTP Client**: Axios

### 인프라
- **컨테이너**: Docker Compose (local dev)
- **CI/CD**: GitHub Actions