package com.sinloans.loans.controller;

import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.entity.User;
import com.sinloans.loans.model.dto.Profile;
import com.sinloans.loans.service.BankService;
import com.sinloans.loans.service.CompanyService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;
    private final BankService bankService;
    private final CompanyService companyService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Profile getProfile(Authentication authentication){
        User user = userService.getUserByUsername(authentication.getName());
        return fillProfile(user);
    }

    @PostMapping(value = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProfile(@RequestBody Profile newProfile, Authentication authentication){
        User user = userService.getUserByUsername(authentication.getName());
        if (newProfile.getEmail() != null){
            user.setUsername(newProfile.getEmail());
            userService.saveUser(user);
        }
        Company company = user.getCompany();
        applyIfNotNull(company::setFullName, newProfile.getFullName());
        applyIfNotNull(company::setShortName, newProfile.getShortName());
        applyIfNotNull(company::setInn, newProfile.getInn());
        applyIfNotNull(company::setKpp, newProfile.getKpp());
        applyIfNotNull(company::setActualAddress, newProfile.getActualAddress());
        applyIfNotNull(company::setLegalAddress, newProfile.getLegalAddress());

        companyService.save(company);
    }

    private void applyIfNotNull(Consumer<String> consumer, String argument){
        if (argument != null){
            consumer.accept(argument);
        }
    }

    private Profile fillProfile(User user) {
        Company company = user.getCompany();
        return Profile.builder()
                .email(user.getUsername())
                .fullName(company.getFullName())
                .shortName(company.getShortName())
                .inn(company.getInn())
                .kpp(company.getKpp())
                .actualAddress(company.getActualAddress())
                .legalAddress(company.getLegalAddress())
                .creditOrganisation(bankService.getByCompany(company) != null)
                .build();
    }
}