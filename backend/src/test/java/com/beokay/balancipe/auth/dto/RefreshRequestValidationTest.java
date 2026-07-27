package com.beokay.balancipe.auth.dto;

import com.beokay.balancipe.auth.fixture.RefreshRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshRequestValidationTest {

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
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();

        Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 리프레시_토큰이_비어있으면_검증에_실패한다() {
        RefreshRequest request = RefreshRequestFixture.BLANK_REFRESH_TOKEN_REQUEST.getRequest();

        Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("refreshToken");
    }
}
