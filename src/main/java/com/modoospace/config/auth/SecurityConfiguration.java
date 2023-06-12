package com.modoospace.config.auth;

import com.modoospace.member.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf().disable().cors().disable()
        .headers().frameOptions().disable() // h2-console화면을 사용하기 위해 해당 옵션들을 disable
        .and()
        .authorizeHttpRequests(request -> request
            .antMatchers("/").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/space").hasRole(Role.HOST.name())
            .antMatchers(HttpMethod.PUT, "/api/v1/member").hasRole(Role.ADMIN.name())
            .anyRequest().authenticated()
        )
        .oauth2Login().userInfoEndpoint()
        .userService(customOAuth2UserService); // 로그인 성공 후 후속조치를 진행할 UserServie 인터페이스의 구현체 등록

    http.logout()
        .logoutSuccessUrl("/")
        .invalidateHttpSession(true); // 세션 날리기
    return http.build();
  }
}
