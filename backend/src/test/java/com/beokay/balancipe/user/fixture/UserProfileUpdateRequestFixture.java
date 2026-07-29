package com.beokay.balancipe.user.fixture;

import com.beokay.balancipe.user.dto.UserProfileUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserProfileUpdateRequestFixture {

    VALID_UPDATE_REQUEST(
            new UserProfileUpdateRequest("짱구", "https://cdn.balancipe.com/profile/1.png")
    ),

    BLANK_NICKNAME_REQUEST(
            new UserProfileUpdateRequest("", "https://cdn.balancipe.com/profile/1.png")
    ),

    TOO_SHORT_NICKNAME_REQUEST(
            new UserProfileUpdateRequest("a", "https://cdn.balancipe.com/profile/1.png")
    );

    private final UserProfileUpdateRequest request;
}
