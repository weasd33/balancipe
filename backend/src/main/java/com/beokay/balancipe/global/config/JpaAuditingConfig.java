package com.beokay.balancipe.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// BalancipeApplication에 @EnableJpaAuditing을 붙이면 @WebMvcTest 슬라이스 테스트 시
// JPA 컨텍스트를 요구해 오류가 발생하므로 별도 설정 클래스로 분리한다.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
