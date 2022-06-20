package com.synloans.loans.service.user.authentication.registration;

import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.user.User;

public interface RegistrationService {

    User register(RegistrationRequest registrationRequest);


}
