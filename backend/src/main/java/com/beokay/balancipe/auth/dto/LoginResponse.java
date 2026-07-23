package com.beokay.balancipe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {
    public static LoginResponse of(Long userId, String nickname, String accessToken, String refreshToken) {
        return new LoginResponse(userId, nickname, accessToken, refreshToken);
    }
}
