package com.beokay.balancipe.user.dto;

import com.beokay.balancipe.user.fixture.UserProfileUpdateRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileUpdateRequestValidationTest {

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
        UserProfileUpdateRequest request = UserProfileUpdateRequestFixture.VALID_UPDATE_REQUEST.getRequest();

        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 닉네임이_비어있으면_검증에_실패한다() {
        UserProfileUpdateRequest request = UserProfileUpdateRequestFixture.BLANK_NICKNAME_REQUEST.getRequest();

        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // 빈 문자열은 @NotBlank와 @Size(min=2) 둘 다 위반해 violation이 2개 발생함
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsOnly("nickname");
    }

    @Test
    void 닉네임이_2자_미만이면_검증에_실패한다() {
        UserProfileUpdateRequest request = UserProfileUpdateRequestFixture.TOO_SHORT_NICKNAME_REQUEST.getRequest();

        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("nickname");
    }
}
