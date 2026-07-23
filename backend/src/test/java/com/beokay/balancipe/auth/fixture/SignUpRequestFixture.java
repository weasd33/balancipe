package com.beokay.balancipe.auth.fixture;

import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.user.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SignUpRequestFixture {

    VALID_SIGN_UP_REQUEST(
            new SignUpRequest("hello@gmail.com", "password1234", "홍길동", Gender.MALE, 2000)
    ),

    DUPLICATED_EMAIL_REQUEST(
            new SignUpRequest("hello@gmail.com", "password1234", "짱구", Gender.FEMALE, 2000)
    ),

    DUPLICATED_NICKNAME_REQUEST(
            new SignUpRequest("world@gmail.com", "password1234", "홍길동", Gender.FEMALE, 2000)
    ),

    INVALID_PASSWORD_LENGTH_REQUEST(
            new SignUpRequest("hello@gmail.com", "short12", "홍길동", Gender.MALE, 2000)
    ),

    FUTURE_BIRTH_YEAR_REQUEST(
            new SignUpRequest("hello@gmail.com", "password1234", "홍길동", Gender.MALE, 2999)
    );

    private final SignUpRequest request;
}
