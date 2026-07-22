package com.beokay.balancipe.auth.service;

import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.auth.dto.SignUpResponse;
import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.global.security.JwtProvider;
import com.beokay.balancipe.global.security.RefreshTokenRepository;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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
}
