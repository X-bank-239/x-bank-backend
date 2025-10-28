package com.example.xbankbackend.config;


import com.example.xbankbackend.exception.ErrorResponseHandler;
import com.example.xbankbackend.secutiry.jwt.JwtTokenProvider;
import com.example.xbankbackend.secutiry.jwt.RefreshTokenAuthenticationFilter;
import com.example.xbankbackend.secutiry.jwt.TokenAuthenticationFilter;
import com.example.xbankbackend.secutiry.login.LoginAuthenticationFilter;
import com.example.xbankbackend.secutiry.matcher.SkipPathRequestMatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfig {
    public static final String SIGNIN_ENTRY_POINT = "/auth/signin";
    public static final String SIGNUP_ENTRY_POINT = "/auth/signup";
    public static final String SWAGGER_ENTRY_POINT = "/swagger-ui/**";
    public static final String API_DOCS_ENTRY_POINT = "/api-docs/**";
    public static final String TOKEN_REFRESH_ENTRY_POINT = "/auth/refreshToken";
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    private final ErrorResponseHandler accessDeniedHandler;

    private final AuthenticationFailureHandler failureHandler;

    public SecurityConfig(final JwtTokenProvider jwtTokenProvider,
                          final AuthenticationManager authenticationManager,
                          @Qualifier("loginAuthenticationSuccessHandler") final AuthenticationSuccessHandler authenticationSuccessHandler,
                          @Qualifier("oauth2AuthenticationSuccessHandler") final AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
                          final ErrorResponseHandler accessDeniedHandler,
                          final AuthenticationFailureHandler failureHandler)  {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SIGNIN_ENTRY_POINT).permitAll()
                        .requestMatchers(SIGNUP_ENTRY_POINT).permitAll()
                        .requestMatchers(SWAGGER_ENTRY_POINT).permitAll()
                        .requestMatchers(API_DOCS_ENTRY_POINT).permitAll()
                        .requestMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(buildLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildRefreshTokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
        http.oauth2Login(configurer -> configurer
                .authorizationEndpoint(config -> config
                        .authorizationRequestRepository(authorizationRequestRepository()))
                .failureHandler(failureHandler)
                .successHandler(oauth2AuthenticationSuccessHandler));

        return http.build();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    protected TokenAuthenticationFilter buildTokenAuthenticationFilter() {
        List<String> pathsToSkip = new ArrayList<>(Arrays.asList(SIGNIN_ENTRY_POINT, SIGNUP_ENTRY_POINT,
                SWAGGER_ENTRY_POINT, API_DOCS_ENTRY_POINT, TOKEN_REFRESH_ENTRY_POINT));
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip);
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(jwtTokenProvider, matcher, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    protected LoginAuthenticationFilter buildLoginProcessingFilter() {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter(SIGNIN_ENTRY_POINT,
                authenticationSuccessHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    protected RefreshTokenAuthenticationFilter buildRefreshTokenProcessingFilter() {
        RefreshTokenAuthenticationFilter filter = new RefreshTokenAuthenticationFilter(TOKEN_REFRESH_ENTRY_POINT,
                authenticationSuccessHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

}