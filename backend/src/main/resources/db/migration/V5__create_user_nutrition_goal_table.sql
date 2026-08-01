-- 사용자별 영양 목표 테이블. 목표 칼로리는 사용자가 최종 확정한 값을 그대로 저장하고,
-- 매크로(탄/단/지) 목표는 입력값(비율 프리셋 또는 체중 기반 공식)과 계산 결과를 함께 비정규화해
-- 조회 시 재계산 없이 바로 응답할 수 있도록 한다.
CREATE TABLE user_nutrition_goal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pregnancy_status VARCHAR(30) NOT NULL,
    reference_year INTEGER NOT NULL,
    target_calorie NUMERIC(10, 2) NOT NULL,
    macro_calculation_method VARCHAR(30) NOT NULL,
    macro_preset_type VARCHAR(30),
    height_cm NUMERIC(6, 2),
    current_weight_kg NUMERIC(6, 2),
    target_weight_kg NUMERIC(6, 2),
    activity_level VARCHAR(20),
    weekly_rate_kg NUMERIC(4, 2),
    protein_per_kg NUMERIC(5, 2),
    fat_per_kg NUMERIC(5, 2),
    target_carbohydrate NUMERIC(10, 2) NOT NULL,
    target_protein NUMERIC(10, 2) NOT NULL,
    target_fat NUMERIC(10, 2) NOT NULL,
    target_dietary_fiber NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_nutrition_goal_user_id UNIQUE (user_id)
);
