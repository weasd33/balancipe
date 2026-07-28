package com.beokay.balancipe.auth.dto;

import com.beokay.balancipe.auth.fixture.LogoutRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LogoutRequestValidationTest {

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
        LogoutRequest request = LogoutRequestFixture.VALID_LOGOUT_REQUEST.getRequest();

        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 리프레시_토큰이_비어있으면_검증에_실패한다() {
        LogoutRequest request = LogoutRequestFixture.BLANK_REFRESH_TOKEN_REQUEST.getRequest();

        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("refreshToken");
    }
}
