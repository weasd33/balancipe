package com.beokay.balancipe.global.security;

import com.beokay.balancipe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 인증 시 쓰기가 없으므로 dirty checking 비활성화
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(CustomUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다: " + email));
    }
}
