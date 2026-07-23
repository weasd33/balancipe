package com.beokay.balancipe.auth.service;

import com.beokay.balancipe.auth.dto.LoginRequest;
import com.beokay.balancipe.auth.dto.LoginResponse;
import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.auth.dto.SignUpResponse;
import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.global.security.CustomUserDetails;
import com.beokay.balancipe.global.security.JwtProvider;
import com.beokay.balancipe.global.security.RefreshTokenRepository;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (request.birthYear() > LocalDate.now().getYear()) {
            throw new BusinessException(ErrorCode.INVALID_BIRTH_YEAR);
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.gender(),
                request.birthYear()
        );
        User savedUser = userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(savedUser.getId(), savedUser.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getId());
        refreshTokenRepository.save(savedUser.getId(), refreshToken);

        return SignUpResponse.of(savedUser.getId(), savedUser.getNickname(), accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException | LockedException | DisabledException e) {
            log.warn("로그인 실패: email={}, reason={}", request.email(), e.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), refreshToken);

        return LoginResponse.of(user.getId(), user.getNickname(), accessToken, refreshToken);
    }
}
