package com.synloans.loans.service.user.authentication.login;

import com.synloans.loans.model.authentication.token.Token;

public interface LoginService {

    Token login(String username, String password);

}
