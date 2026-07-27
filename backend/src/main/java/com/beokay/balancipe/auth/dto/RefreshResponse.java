package com.beokay.balancipe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshResponse(

        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {
    public static RefreshResponse of(String accessToken) {
        return new RefreshResponse(accessToken);
    }
}
