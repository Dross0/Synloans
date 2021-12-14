package com.synloans.loans.controller.user;

import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.AuthenticationResponse;
import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse login(@RequestBody AuthenticationRequest authenticationRequest){
        try{
            String jwt = authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            return new AuthenticationResponse(jwt);
        } catch (BadCredentialsException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
    }

    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registration(@RequestBody RegistrationRequest registrationRequest) {
        Company company = new Company();
        company.setInn(registrationRequest.getInn());
        company.setKpp(registrationRequest.getKpp());
        company.setFullName(registrationRequest.getFullName());
        company.setShortName(registrationRequest.getShortName());
        company.setActualAddress(registrationRequest.getActualAddress());
        company.setLegalAddress(registrationRequest.getLegalAddress());
        authenticationService.register(
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                company,
                registrationRequest.isCreditOrganisation()
        );
    }
}
