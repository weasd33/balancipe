package com.beokay.balancipe.auth.dto;

import com.beokay.balancipe.auth.fixture.SignUpRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignUpRequestValidationTest {

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
        SignUpRequest request = SignUpRequestFixture.VALID_SIGN_UP_REQUEST.getRequest();

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 비밀번호가_8자_미만이면_검증에_실패한다() {
        SignUpRequest request = SignUpRequestFixture.INVALID_PASSWORD_LENGTH_REQUEST.getRequest();

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password");
    }
}
