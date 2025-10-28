package com.example.xbankbackend.secutiry.login;


import com.example.xbankbackend.exception.ServiceException;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.xbankbackend.services.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoginAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    private final PasswordEncoder encoder;

    @Autowired
    public LoginAuthenticationProvider(final UserService userService) {
        this.userService = userService;
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");
        String username = (String) authentication.getPrincipal();
        String password = authentication.getCredentials().toString();
        UserDetails securityUser = authenticateByUsernameAndPassword(username, password);
        return new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
    }

    private UserDetails authenticateByUsernameAndPassword(final String email, final String password) {
        User user = getUser(email);
        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("exception.badCredentials");
        }
        return UserDetails.build(user);
    }

    private User getUser(final String email) {
        try {
            return userService.findByEmail(email);
        } catch (ServiceException e) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
