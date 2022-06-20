package com.synloans.loans.service.user.authentication.login.impl;

import com.synloans.loans.model.authentication.token.Token;
import com.synloans.loans.service.token.generator.TokenGenerator;
import com.synloans.loans.service.user.authentication.login.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserDetailsService userDetailsService;

    private final AuthenticationManager authenticationManager;

    private final TokenGenerator tokenGenerator;

    @Override
    public Token login(String username, String password){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails user = userDetailsService.loadUserByUsername(username);
        return tokenGenerator.generateToken(user);
    }
}
