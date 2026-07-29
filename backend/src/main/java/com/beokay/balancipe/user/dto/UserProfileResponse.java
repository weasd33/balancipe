package com.beokay.balancipe.user.dto;

import com.beokay.balancipe.user.domain.Gender;
import com.beokay.balancipe.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "성별", example = "FEMALE")
        Gender gender,

        @Schema(description = "출생연도", example = "1998")
        int birthYear,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.balancipe.com/profile/1.png")
        String profileImageUrl
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getGender(),
                user.getBirthYear(),
                user.getProfileImageUrl()
        );
    }
}
