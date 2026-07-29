package com.beokay.balancipe.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(

        @Schema(description = "닉네임 (2~30자)", example = "홍길동")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.balancipe.com/profile/1.png")
        @Size(max = 500, message = "프로필 이미지 URL이 너무 깁니다.")
        String profileImageUrl
) {
}
