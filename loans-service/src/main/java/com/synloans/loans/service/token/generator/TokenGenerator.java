package com.synloans.loans.service.token.generator;

import com.synloans.loans.model.authentication.token.Token;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenGenerator {

    Token generateToken(UserDetails userDetails);

}
