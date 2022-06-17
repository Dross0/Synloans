package com.synloans.loans.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.security.util.JwtService;
import com.synloans.loans.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class BaseControllerTest {
    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected UserService userService;
}
