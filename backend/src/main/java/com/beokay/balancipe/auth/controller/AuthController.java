package com.beokay.balancipe.auth.controller;

import com.beokay.balancipe.auth.dto.LoginRequest;
import com.beokay.balancipe.auth.dto.LoginResponse;
import com.beokay.balancipe.auth.dto.RefreshRequest;
import com.beokay.balancipe.auth.dto.RefreshResponse;
import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.auth.dto.SignUpResponse;
import com.beokay.balancipe.auth.service.AuthService;
import com.beokay.balancipe.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호/닉네임 등을 입력받아 회원가입을 처리하고, 가입 즉시 Access/Refresh Token을 발급한다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 인증을 수행하고, 성공 시 Access/Refresh Token을 발급한다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하여 새 Access Token을 발급한다. Refresh Token은 재발급하지 않는다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
