package com.sinloans.loans.controller;

import com.sinloans.loans.model.Company;
import com.sinloans.loans.model.authentication.AuthenticationRequest;
import com.sinloans.loans.model.authentication.AuthenticationResponse;
import com.sinloans.loans.model.User;
import com.sinloans.loans.model.authentication.RegistrationRequest;
import com.sinloans.loans.security.util.JwtService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody User getUserById(@PathVariable("id") Long id){
        return userService.getUserById(id);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails user = userService.loadUserByUsername(authenticationRequest.getEmail());
        String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
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
        User user = userService.saveUser(registrationRequest.getEmail(), registrationRequest.getPassword(), company);
        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь с email=" + registrationRequest.getEmail() + " уже зарегистрирован");
        }
    }
}
