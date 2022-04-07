package com.synloans.loans.controller.user;

import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.AuthenticationResponse;
import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.user.AuthenticationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse login(@RequestBody @Valid AuthenticationRequest authenticationRequest){
        try{
            String jwt = authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            return new AuthenticationResponse(jwt);
        } catch (BadCredentialsException e){
            throw new UserUnauthorizedException("Неверный логин или пароль");
        }
    }

    @ApiOperation(value = "Registration of new user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User successfully register"),
            @ApiResponse(code = 404, message = "Failed search or creating user company"),
            @ApiResponse(code = 401, message = "Error while register new user")
    })
    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registration(@RequestBody @Valid RegistrationRequest registrationRequest) {
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
