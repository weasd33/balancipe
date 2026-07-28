package com.beokay.balancipe.auth.fixture;

import com.beokay.balancipe.auth.dto.LogoutRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogoutRequestFixture {

    VALID_LOGOUT_REQUEST(
            new LogoutRequest("refresh-token")
    ),

    BLANK_REFRESH_TOKEN_REQUEST(
            new LogoutRequest("")
    );

    private final LogoutRequest request;
}
