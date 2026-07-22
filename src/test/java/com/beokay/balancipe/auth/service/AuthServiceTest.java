package com.beokay.balancipe.auth.service;

import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.auth.dto.SignUpResponse;
import com.beokay.balancipe.auth.fixture.SignUpRequestFixture;
import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.global.security.JwtProvider;
import com.beokay.balancipe.global.security.RefreshTokenRepository;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.domain.UserRole;
import com.beokay.balancipe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.DUPLICATED_EMAIL_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.DUPLICATED_NICKNAME_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.FUTURE_BIRTH_YEAR_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.VALID_SIGN_UP_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입에_성공하면_토큰을_발급하고_Redis에_저장한다() {
        SignUpRequest request = VALID_SIGN_UP_REQUEST.getRequest();
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-password");

        User savedUser = User.create(request.email(), "encoded-password", request.nickname(),
                request.gender(), request.birthYear());
        ReflectionTestUtils.setField(savedUser, "id", 1L);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        given(jwtProvider.generateAccessToken(1L, UserRole.USER)).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(1L)).willReturn("refresh-token");

        SignUpResponse response = authService.signUp(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(1L, "refresh-token");
    }

    @Test
    void 이메일이_중복되면_예외를_던진다() {
        SignUpRequest request = DUPLICATED_EMAIL_REQUEST.getRequest();
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    void 닉네임이_중복되면_예외를_던진다() {
        SignUpRequest request = DUPLICATED_NICKNAME_REQUEST.getRequest();
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByNickname(request.nickname())).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository, never()).save(any());
    }

    @Test
    void 출생연도가_현재연도를_초과하면_예외를_던진다() {
        SignUpRequest request = FUTURE_BIRTH_YEAR_REQUEST.getRequest();
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_BIRTH_YEAR);

        verify(userRepository, never()).save(any());
    }
}
