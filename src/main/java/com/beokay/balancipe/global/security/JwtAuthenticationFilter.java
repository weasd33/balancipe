package com.beokay.balancipe.global.security;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtProvider.parseClaims(token);
                if (jwtProvider.getTokenType(claims) == TokenType.ACCESS) {
                    authenticate(claims, request);
                } else {
                    log.debug("Refresh Token으로 API 요청 시도: uri={}", request.getRequestURI());
                }
            } catch (BusinessException e) {
                logTokenException(e, request);
            }
        }

        filterChain.doFilter(request, response);
    }

    // status가 ACTIVE가 아닌 사용자는 Access Token이 만료되기 전이라도 즉시 요청을 차단하기 위해
    // claim만으로 인증 객체를 만들지 않고 매 요청마다 DB에서 최신 상태를 조회한다.
    private void authenticate(Claims claims, HttpServletRequest request) {
        Long userId = jwtProvider.getUserId(claims);

        userRepository.findById(userId)
            .map(CustomUserDetails::new)
            .filter(CustomUserDetails::isEnabled)
            .ifPresent(userDetails -> {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
    }

    // 인증 실패로 처리하되(요청은 익명으로 통과시켜 이후 authorizeHttpRequests 규칙이 401/403을 결정),
    // 원인별로 로그 레벨을 구분해 위변조 시도(서명 불일치 등)와 만료 같은 정상 케이스를 구별한다.
    private void logTokenException(BusinessException e, HttpServletRequest request) {
        switch (e.getErrorCode()) {
            case EXPIRED_TOKEN -> log.debug("만료된 토큰 요청: uri={}", request.getRequestURI());
            case EMPTY_TOKEN -> log.debug("빈 토큰 요청: uri={}", request.getRequestURI());
            default -> log.warn(
                "유효하지 않은 토큰 요청: code={}, uri={}, ip={}",
                e.getErrorCode(), request.getRequestURI(), request.getRemoteAddr()
            );
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
