package com.lookfit.global.config;

import com.lookfit.global.common.Role;
import com.lookfit.global.security.JwtAuthenticationFilter;
import com.lookfit.global.security.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "https://look-fit.vercel.app",  // Vercel 프로덕션
            "https://ste-colleges-wires-saints.trycloudflare.com"  // Cloudflare Tunnel
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Static resources
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/profile").permitAll()

                        // OAuth2 and Login
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Public API endpoints (no authentication required)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Review API - 구체적인 경로를 먼저 매칭 (중요!)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/products/*/reviews").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/reviews/**").authenticated()

                        // Product API - 조회는 public, 리뷰 조회도 포함
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/products/**").permitAll()

                        // Protected API endpoints (authentication required)
                        .requestMatchers("/api/v1/cart/**").authenticated()
                        .requestMatchers("/api/v1/wishlist/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/fitting/**").authenticated()
                        .requestMatchers("/api/members/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // 인증 실패 시 401 반환 (SPA 아키텍처 - 리다이렉트 하지 않음)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 모든 인증 실패는 401 반환
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 부족 시 403 반환
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/oauth2/authorization/google")  // 명시적으로 Google OAuth2 로그인 페이지 설정
                        .userInfoEndpoint(userInfoEndpoint -> {})
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            // OAuth2 로그인 실패 시 로그 출력
                            System.err.println("OAuth2 Login Failed: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("http://localhost:5173/login?error=oauth_failed");
                        })
                );
        return http.build();
    }
}