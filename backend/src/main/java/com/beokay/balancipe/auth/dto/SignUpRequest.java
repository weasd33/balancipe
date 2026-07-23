package com.beokay.balancipe.auth.dto;

import com.beokay.balancipe.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호 (8~20자)", example = "password1234")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        String password,

        @Schema(description = "닉네임 (2~30자)", example = "홍길동")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "성별", example = "FEMALE")
        @NotNull(message = "성별을 선택해주세요.")
        Gender gender,

        @Schema(description = "출생연도", example = "1998")
        @Min(value = 1900, message = "유효하지 않은 출생연도입니다.")
        int birthYear
) {
}
