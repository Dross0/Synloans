package com.synloans.loans.controller.user.login;

import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.AuthenticationResponse;

public interface LoginController {

    AuthenticationResponse login(AuthenticationRequest authenticationRequest);

}
