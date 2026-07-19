## 도메인 및 DB 스키마

### 1. 사용자 (User)
```sql
CREATE TABLE users (
    id                BIGSERIAL     PRIMARY KEY,
    email             VARCHAR(100)  NOT NULL,
    password          VARCHAR(255)  NOT NULL,
    nickname          VARCHAR(30)   NOT NULL,
    gender            VARCHAR(10)   NOT NULL,  -- MALE, FEMALE
    birth_year        INTEGER       NOT NULL,  -- 영양소 기준 계산용
    profile_image_url VARCHAR(500),
    role              VARCHAR(10)   NOT NULL,  -- USER, ADMIN (기본값은 Java 코드에서 설정)
    status            VARCHAR(10)   NOT NULL,  -- ACTIVE, INACTIVE, BANNED
    created_at        TIMESTAMP,               -- JPA Auditing 처리
    updated_at        TIMESTAMP,               -- JPA Auditing 처리
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
);
```

### 2. 한국인 영양소 섭취기준 (KoreanDietaryReference)
```sql
CREATE TABLE korean_dietary_reference (
    id           BIGSERIAL PRIMARY KEY,
    gender       VARCHAR(10)    NOT NULL,  -- MALE, FEMALE, ALL
    age_min      INT            NOT NULL,
    age_max      INT            NOT NULL,
    calorie      DECIMAL(8,2),             -- kcal
    protein      DECIMAL(8,2),             -- g
    fat          DECIMAL(8,2),             -- g
    carbohydrate DECIMAL(8,2),             -- g
    fiber        DECIMAL(8,2),             -- g
    sodium       DECIMAL(8,2),             -- mg
    calcium      DECIMAL(8,2),             -- mg
    iron         DECIMAL(8,2),             -- mg
    vitamin_a    DECIMAL(8,2),             -- μg RAE
    vitamin_c    DECIMAL(8,2),             -- mg
    vitamin_d    DECIMAL(8,2),             -- μg
    source_year  INT NOT NULL DEFAULT 2020 -- 기준 연도 (2020 기준)
);
```
> 보건복지부 2020 한국인 영양소 섭취기준 기준값을 애플리케이션 초기 데이터(data.sql)로 삽입

### 3. 사용자 영양 목표 (UserNutritionGoal)
```sql
CREATE TABLE user_nutrition_goal (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    preset_type  VARCHAR(20) NOT NULL DEFAULT 'MAINTAIN', -- MAINTAIN, DIET, BULK, CUSTOM
    calorie      DECIMAL(8,2),
    protein      DECIMAL(8,2),
    fat          DECIMAL(8,2),
    carbohydrate DECIMAL(8,2),
    fiber        DECIMAL(8,2),
    sodium       DECIMAL(8,2),
    calcium      DECIMAL(8,2),
    iron         DECIMAL(8,2),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id)
);
```
> 프리셋 계산 로직: DIET = 기준값 × 0.8, BULK = 기준값 × 1.3, MAINTAIN = 기준값 × 1.0

### 4. 식품영양성분 (FoodNutrition)
```sql
CREATE TABLE food_nutrition (
    id             BIGSERIAL PRIMARY KEY,
    food_code      VARCHAR(50) UNIQUE,     -- Open API 식품코드
    food_name      VARCHAR(255) NOT NULL,
    category       VARCHAR(100),
    manufacturer   VARCHAR(100),
    serving_size   DECIMAL(8,2),           -- g 또는 mL
    serving_unit   VARCHAR(20),
    calorie        DECIMAL(8,2),
    protein        DECIMAL(8,2),
    fat            DECIMAL(8,2),
    carbohydrate   DECIMAL(8,2),
    sugar          DECIMAL(8,2),
    fiber          DECIMAL(8,2),
    sodium         DECIMAL(8,2),
    calcium        DECIMAL(8,2),
    iron           DECIMAL(8,2),
    source         VARCHAR(20) NOT NULL DEFAULT 'API', -- API, MANUAL
    is_verified    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_food_name ON food_nutrition USING gin(to_tsvector('simple', food_name));
```

### 5. 식품 요청 (FoodRequest)
```sql
CREATE TABLE food_request (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id),
    food_name     VARCHAR(255) NOT NULL,
    photo_url     VARCHAR(500),
    link_url      VARCHAR(500),
    link_platform VARCHAR(50),             -- NAVER, COUPANG
    memo          TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    admin_memo    TEXT,
    reviewed_by   BIGINT REFERENCES users(id),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at   TIMESTAMP
);
```

### 6. 레시피 (Recipe)
```sql
CREATE TABLE recipe (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    thumbnail_url    VARCHAR(500),
    total_price      INT,                  -- 원 단위, NULL 허용
    view_count       INT NOT NULL DEFAULT 0,
    scrap_count      INT NOT NULL DEFAULT 0,
    like_count       INT NOT NULL DEFAULT 0,
    status           VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED', -- DRAFT, PUBLISHED
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recipe_created ON recipe(created_at DESC);
CREATE INDEX idx_recipe_like ON recipe(like_count DESC);
CREATE INDEX idx_recipe_view ON recipe(view_count DESC);
```

### 7. 레시피 재료 (RecipeIngredient)
```sql
CREATE TABLE recipe_ingredient (
    id           BIGSERIAL PRIMARY KEY,
    recipe_id    BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    food_id      BIGINT NOT NULL REFERENCES food_nutrition(id),
    quantity     DECIMAL(8,2) NOT NULL,
    unit         VARCHAR(20) NOT NULL      -- g, mL, 개, 큰술, 작은술 등
);
```

### 8. 레시피 조리 단계 (RecipeStep)
```sql
CREATE TABLE recipe_step (
    id          BIGSERIAL PRIMARY KEY,
    recipe_id   BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    step_order  INT NOT NULL,
    description TEXT NOT NULL,
    image_url   VARCHAR(500)
);
```

### 9. 스크랩 / 좋아요 (RecipeScrap, RecipeLike)
```sql
CREATE TABLE recipe_scrap (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    recipe_id  BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, recipe_id)
);

CREATE TABLE recipe_like (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    recipe_id  BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, recipe_id)
);
```