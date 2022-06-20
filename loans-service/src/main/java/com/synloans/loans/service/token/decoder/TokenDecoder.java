package com.synloans.loans.service.token.decoder;

import com.synloans.loans.model.authentication.token.Token;

public interface TokenDecoder {

    Token decode(String tokenValue);

}
