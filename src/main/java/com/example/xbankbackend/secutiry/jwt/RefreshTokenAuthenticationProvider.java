package com.example.xbankbackend.secutiry.jwt;

import com.example.xbankbackend.services.UserDetails;
import com.example.xbankbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    private final JwtTokenProvider tokenProvider;

    @Autowired
    public RefreshTokenAuthenticationProvider(final UserService userService,
                                              final JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        String username = tokenProvider.getUserNameFromJwtToken(token);
        UserDetails userDetails = getUserDetails(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    private UserDetails getUserDetails(String email) {
        return UserDetails.build(userService.findByEmail(email));

    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (RefreshJwtAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
