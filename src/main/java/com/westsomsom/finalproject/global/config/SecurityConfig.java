package com.westsomsom.finalproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(channel -> channel.anyRequest().requiresSecure()) // ✅ 모든 요청을 HTTPS로 리디렉트
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // ✅ API 엔드포인트는 CSRF 보호 제외
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/login", "/oauth2/**", "/api/**").permitAll() // ✅ 모든 API 요청 허용
                                .anyRequest().authenticated() // ✅ 나머지 요청은 인증 필요
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );
        return http.build();
    }
}