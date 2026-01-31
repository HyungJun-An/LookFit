package com.lookfit.global.config;

import com.lookfit.global.common.Role;
import com.lookfit.global.security.JwtAuthenticationFilter;
import com.lookfit.global.security.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Static resources
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/profile").permitAll()

                        // OAuth2 and Login
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Public API endpoints (no authentication required)
                        .requestMatchers("/api/v1/products/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Protected API endpoints (authentication required)
                        .requestMatchers("/api/v1/cart/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/fitting/**").authenticated()
                        .requestMatchers("/api/members/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(userInfoEndpoint -> {})
                        .successHandler(oAuth2SuccessHandler)
                );
        return http.build();
    }
}