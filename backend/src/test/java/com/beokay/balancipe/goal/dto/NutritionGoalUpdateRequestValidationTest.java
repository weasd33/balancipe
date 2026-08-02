package com.beokay.balancipe.goal.dto;

import com.beokay.balancipe.goal.fixture.NutritionGoalUpdateRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NutritionGoalUpdateRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void 유효한_요청은_검증을_통과한다() {
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.VALID_RATIO_PRESET_REQUEST.getRequest();

        Set<ConstraintViolation<NutritionGoalUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 키가_없으면_검증에_실패한다() {
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.MISSING_HEIGHT_REQUEST.getRequest();

        Set<ConstraintViolation<NutritionGoalUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("heightCm");
    }

    @Test
    void 목표_칼로리가_0_이하이면_검증에_실패한다() {
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.NEGATIVE_TARGET_CALORIE_REQUEST.getRequest();

        Set<ConstraintViolation<NutritionGoalUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("targetCalorie");
    }
}
