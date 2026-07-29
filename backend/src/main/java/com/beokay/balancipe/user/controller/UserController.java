package com.beokay.balancipe.user.controller;

import com.beokay.balancipe.global.common.ApiResponse;
import com.beokay.balancipe.global.security.CustomUserDetails;
import com.beokay.balancipe.user.dto.UserProfileResponse;
import com.beokay.balancipe.user.dto.UserProfileUpdateRequest;
import com.beokay.balancipe.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "인증된 사용자의 프로필 정보를 조회한다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse response = userService.getMyProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "내 프로필 수정", description = "인증된 사용자의 닉네임/프로필 이미지를 수정한다.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userService.updateMyProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
