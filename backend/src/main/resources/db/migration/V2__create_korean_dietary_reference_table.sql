-- 한국인 영양소 섭취기준 참조 테이블. 영양소를 고정 컬럼이 아닌 행(nutrient_code)으로 관리해
-- 향후 영양소/연도 확장 시 스키마 변경 없이 데이터만 추가할 수 있도록 설계.
CREATE TABLE korean_dietary_reference (
    id BIGSERIAL PRIMARY KEY,
    reference_year INTEGER NOT NULL,
    gender VARCHAR(10) NOT NULL,
    age_group VARCHAR(30) NOT NULL,
    pregnancy_status VARCHAR(30) NOT NULL,
    nutrient_code VARCHAR(30) NOT NULL,
    indicator_type VARCHAR(10) NOT NULL,
    reference_value NUMERIC(10, 3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_korean_dietary_reference
        UNIQUE (reference_year, gender, age_group, pregnancy_status, nutrient_code, indicator_type)
);
