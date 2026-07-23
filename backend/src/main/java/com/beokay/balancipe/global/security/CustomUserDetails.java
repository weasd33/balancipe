package com.beokay.balancipe.global.security;

import com.beokay.balancipe.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// User 도메인 객체를 UserDetails로 감싸는 어댑터.
// User가 UserDetails를 직접 구현하면 도메인이 Spring Security에 종속되므로 이 클래스에서 분리한다.
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Spring Security의 식별자(username)로 이메일을 사용한다
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // false 반환 시 LockedException 발생
    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // false 반환 시 DisabledException 발생 — isAccountNonLocked와 별개로 둘 다 체크됨
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public Long getUserId() {
        return user.getId();
    }
}
