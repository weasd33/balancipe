package com.beokay.balancipe.auth.fixture;

import com.beokay.balancipe.auth.dto.LoginRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginRequestFixture {

    VALID_LOGIN_REQUEST(
            new LoginRequest("hello@gmail.com", "password1234")
    ),

    BLANK_EMAIL_REQUEST(
            new LoginRequest("", "password1234")
    ),

    INVALID_EMAIL_FORMAT_REQUEST(
            new LoginRequest("not-an-email", "password1234")
    ),

    BLANK_PASSWORD_REQUEST(
            new LoginRequest("hello@gmail.com", "")
    );

    private final LoginRequest request;
}
