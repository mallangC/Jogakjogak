package com.zb.jogakjogak.security.config;


import com.zb.jogakjogak.security.jwt.CustomLogoutFilter;
import com.zb.jogakjogak.security.jwt.JWTFilter;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.oauth2.CustomSuccessHandler;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import com.zb.jogakjogak.security.service.CustomOauth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.
                cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration corsConfiguration = new CorsConfiguration();
                        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                        corsConfiguration.setAllowCredentials(true);
                        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                        corsConfiguration.setMaxAge(3600L);
                        corsConfiguration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

                        return corsConfiguration;
                    }}));
        http.
                csrf((auth) -> auth.disable());
        http.
                formLogin((auth) -> auth.disable());
        http.
                httpBasic((auth) -> auth.disable());
        http.
                addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);
        http.
                addFilterBefore(new CustomLogoutFilter(refreshTokenRepository, jwtUtil), LogoutFilter.class);

        http.
                oauth2Login((oauth2) -> oauth2.userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig.userService(customOauth2UserService))
                        .successHandler(customSuccessHandler)
                );
        http.
                authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/",
                                "/oauth2/**",
                                "/member/reissue",
                                "/member/logout",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        .anyRequest().authenticated());
        http.
                sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
