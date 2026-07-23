package com.beokay.balancipe.auth.dto;

import com.beokay.balancipe.auth.fixture.LoginRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestValidationTest {

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
        LoginRequest request = LoginRequestFixture.VALID_LOGIN_REQUEST.getRequest();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 이메일이_비어있으면_검증에_실패한다() {
        LoginRequest request = LoginRequestFixture.BLANK_EMAIL_REQUEST.getRequest();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void 이메일_형식이_올바르지_않으면_검증에_실패한다() {
        LoginRequest request = LoginRequestFixture.INVALID_EMAIL_FORMAT_REQUEST.getRequest();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void 비밀번호가_비어있으면_검증에_실패한다() {
        LoginRequest request = LoginRequestFixture.BLANK_PASSWORD_REQUEST.getRequest();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password");
    }
}
