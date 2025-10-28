package com.example.xbankbackend.secutiry.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private String rawAccessToken;

    public JwtAuthenticationToken(final String rawAccessToken) {
        super(null);
        this.rawAccessToken = rawAccessToken;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(final UserDetails userDetails) {
        super(userDetails.getAuthorities());
        super.setAuthenticated(true);
        super.eraseCredentials();
    }

    @Override
    public Object getCredentials() {
        return rawAccessToken;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.rawAccessToken = null;
    }
}
