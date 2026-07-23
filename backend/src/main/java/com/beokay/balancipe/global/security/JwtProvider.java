package com.beokay.balancipe.global.security;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.global.properties.JwtProperties;
import com.beokay.balancipe.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, UserRole role) {
        return generateToken(userId, TokenType.ACCESS, role, jwtProperties.accessTokenExpiration());
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, TokenType.REFRESH, null, jwtProperties.refreshTokenExpiration());
    }

    // 실패 원인(만료/서명위조/구조손상 등)에 따라 세분화된 ErrorCode를 담아 던진다.
    // 만료는 클라이언트 재발급 흐름에서 발생하는 정상적인 상황이지만,
    // 서명 불일치·구조 손상은 위변조 시도일 수 있어 호출부에서 로그 레벨을 다르게 처리한다.
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new BusinessException(ErrorCode.MALFORMED_TOKEN);
        } catch (SignatureException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_SIGNATURE);
        } catch (UnsupportedJwtException e) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.EMPTY_TOKEN);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public TokenType getTokenType(Claims claims) {
        return TokenType.valueOf(claims.get(CLAIM_TYPE, String.class));
    }

    private String generateToken(Long userId, TokenType type, UserRole role, long expiration) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(CLAIM_TYPE, type.name())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expiration));

        if (role != null) {
            builder.claim(CLAIM_ROLE, role.name());
        }

        return builder.signWith(key).compact();
    }
}
