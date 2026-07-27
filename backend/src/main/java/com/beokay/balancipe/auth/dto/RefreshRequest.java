package com.beokay.balancipe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) {
}
