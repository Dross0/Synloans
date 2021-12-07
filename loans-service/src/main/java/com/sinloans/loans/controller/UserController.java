package com.sinloans.loans.controller;

import com.sinloans.loans.model.authentication.AuthenticationRequest;
import com.sinloans.loans.model.authentication.AuthenticationResponse;
import com.sinloans.loans.model.authentication.RegistrationRequest;
import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.entity.User;
import com.sinloans.loans.security.util.JwtService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse login(@RequestBody AuthenticationRequest authenticationRequest){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        UserDetails user = userService.loadUserByUsername(authenticationRequest.getEmail());
        String jwt = jwtService.generateToken(user);
        return new AuthenticationResponse(jwt);
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
        User user;
        if (registrationRequest.isCreditOrganisation()){
            user = userService.createBankUser(registrationRequest.getEmail(), registrationRequest.getPassword(), company);
        } else {
            user = userService.createCorpUser(registrationRequest.getEmail(), registrationRequest.getPassword(), company);
        }
        if (user == null){
            log.error("Пользователь с email={} уже зарегистрирован", registrationRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь с email=" + registrationRequest.getEmail() + " уже зарегистрирован");
        }
    }
}
