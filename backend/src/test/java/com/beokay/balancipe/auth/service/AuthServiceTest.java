package com.beokay.balancipe.auth.service;

import com.beokay.balancipe.auth.dto.LoginRequest;
import com.beokay.balancipe.auth.dto.LoginResponse;
import com.beokay.balancipe.auth.dto.LogoutRequest;
import com.beokay.balancipe.auth.dto.RefreshRequest;
import com.beokay.balancipe.auth.dto.RefreshResponse;
import com.beokay.balancipe.auth.dto.SignUpRequest;
import com.beokay.balancipe.auth.dto.SignUpResponse;
import com.beokay.balancipe.auth.fixture.LoginRequestFixture;
import com.beokay.balancipe.auth.fixture.LogoutRequestFixture;
import com.beokay.balancipe.auth.fixture.RefreshRequestFixture;
import com.beokay.balancipe.auth.fixture.SignUpRequestFixture;
import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.global.security.CustomUserDetails;
import com.beokay.balancipe.global.security.JwtProvider;
import com.beokay.balancipe.global.security.RefreshTokenRepository;
import com.beokay.balancipe.global.security.TokenType;
import com.beokay.balancipe.user.domain.Gender;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.domain.UserRole;
import com.beokay.balancipe.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.DUPLICATED_EMAIL_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.DUPLICATED_NICKNAME_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.FUTURE_BIRTH_YEAR_REQUEST;
import static com.beokay.balancipe.auth.fixture.SignUpRequestFixture.VALID_SIGN_UP_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

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

    @Test
    void 로그인에_성공하면_토큰을_발급하고_Redis에_저장한다() {
        LoginRequest request = LoginRequestFixture.VALID_LOGIN_REQUEST.getRequest();

        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(new CustomUserDetails(user));

        given(jwtProvider.generateAccessToken(1L, UserRole.USER)).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(1L)).willReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("홍길동");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(1L, "refresh-token");
    }

    @Test
    void 이메일_또는_비밀번호가_일치하지_않으면_예외를_던진다() {
        LoginRequest request = LoginRequestFixture.VALID_LOGIN_REQUEST.getRequest();
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다."));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(refreshTokenRepository, never()).save(any(), any());
    }

    @Test
    void 정지된_계정으로_로그인하면_예외를_던진다() {
        LoginRequest request = LoginRequestFixture.VALID_LOGIN_REQUEST.getRequest();
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new LockedException("계정이 잠겨 있습니다."));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(refreshTokenRepository, never()).save(any(), any());
    }

    @Test
    void 재발급에_성공하면_새_Access_Token을_반환한다() {
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.REFRESH);
        given(jwtProvider.getUserId(claims)).willReturn(1L);
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.of("refresh-token"));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(1L, UserRole.USER)).willReturn("new-access-token");

        RefreshResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
    }

    @Test
    void 만료된_리프레시_토큰이면_예외를_던진다() {
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();
        given(jwtProvider.parseClaims("refresh-token"))
                .willThrow(new BusinessException(ErrorCode.EXPIRED_TOKEN));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    void 토큰_타입이_REFRESH가_아니면_예외를_던진다() {
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.ACCESS);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void Redis에_저장된_토큰과_일치하지_않으면_예외를_던진다() {
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.REFRESH);
        given(jwtProvider.getUserId(claims)).willReturn(1L);
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.of("other-refresh-token"));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void Redis에_저장된_토큰이_없으면_예외를_던진다() {
        RefreshRequest request = RefreshRequestFixture.VALID_REFRESH_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.REFRESH);
        given(jwtProvider.getUserId(claims)).willReturn(1L);
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void 로그아웃에_성공하면_Redis에서_토큰을_삭제한다() {
        LogoutRequest request = LogoutRequestFixture.VALID_LOGOUT_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.REFRESH);
        given(jwtProvider.getUserId(claims)).willReturn(1L);

        authService.logout(request);

        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void 로그아웃_시_토큰_타입이_REFRESH가_아니면_예외를_던진다() {
        LogoutRequest request = LogoutRequestFixture.VALID_LOGOUT_REQUEST.getRequest();
        Claims claims = mock(Claims.class);
        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(jwtProvider.getTokenType(claims)).willReturn(TokenType.ACCESS);

        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void 로그아웃_시_만료된_리프레시_토큰이면_예외를_던진다() {
        LogoutRequest request = LogoutRequestFixture.VALID_LOGOUT_REQUEST.getRequest();
        given(jwtProvider.parseClaims("refresh-token"))
                .willThrow(new BusinessException(ErrorCode.EXPIRED_TOKEN));

        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);

        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }
}
