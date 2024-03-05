package com.synloans.loans.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.service.token.decoder.impl.JwtDecoder;
import com.synloans.loans.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class BaseControllerTest {
    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected JwtDecoder jwtDecoder;

    @MockBean
    protected UserService userService;

    @MockBean
    protected UserDetailsService userDetailsService;
}
