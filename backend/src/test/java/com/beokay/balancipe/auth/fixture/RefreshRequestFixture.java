package com.beokay.balancipe.auth.fixture;

import com.beokay.balancipe.auth.dto.RefreshRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefreshRequestFixture {

    VALID_REFRESH_REQUEST(
            new RefreshRequest("refresh-token")
    ),

    BLANK_REFRESH_TOKEN_REQUEST(
            new RefreshRequest("")
    );

    private final RefreshRequest request;
}
