package com.example.xbankbackend.secutiry.jwt;


import com.example.xbankbackend.models.dto.RefreshTokenDTO;
import com.example.xbankbackend.secutiry.exception.AuthMethodNotSupportedException;
import com.example.xbankbackend.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    public RefreshTokenAuthenticationFilter(final String url,
                                            final AuthenticationSuccessHandler successHandler,
                                            final AuthenticationFailureHandler failureHandler) {
        super(url);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        validateRequest(request);
        RefreshTokenDTO refreshTokenDto = getRefreshTokenDTO(request);
        validateRefreshToken(refreshTokenDto);
        return getAuthenticationManager().authenticate(new RefreshJwtAuthenticationToken(refreshTokenDto.getRefreshToken()));
    }

    private void validateRequest(final HttpServletRequest request) {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            if(logger.isDebugEnabled()) {
                logger.debug("Authentication method not supported. Request method: " + request.getMethod());
            }
            throw new AuthMethodNotSupportedException("Authentication method not supported");
        }
    }

    private static void validateRefreshToken(final RefreshTokenDTO refreshTokenDto) {
        if (StringUtils.isBlank(refreshTokenDto.getRefreshToken())) {
            throw new AuthenticationServiceException("Username or Password not provided");
        }
    }

    private static RefreshTokenDTO getRefreshTokenDTO(final HttpServletRequest request) {
        RefreshTokenDTO refreshTokenDto;
        try {
            refreshTokenDto = JsonUtils.fromReader(request.getReader(), RefreshTokenDTO.class);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid login request payload");
        }
        return refreshTokenDto;
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}

