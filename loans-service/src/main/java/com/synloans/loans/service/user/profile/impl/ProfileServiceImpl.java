package com.synloans.loans.service.user.profile.impl;

import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.user.UserService;
import com.synloans.loans.service.user.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserService userService;
    private final BankService bankService;
    private final CompanyService companyService;

    private final Converter<User, Profile> userProfileConverter;

    @Override
    public Profile getProfile(String username){
        User user = userService.getUserByUsername(username);
        return fillProfile(user);
    }

    @Transactional
    @Override
    public void editProfile(String username, ProfileUpdateRequest updateRequest){
        User user = userService.getUserByUsername(username);
        if (updateRequest.getEmail() != null){
            user.setUsername(updateRequest.getEmail());
            userService.saveUser(user);
        }
        Company company = user.getCompany();

        applyIfNotNull(company::setShortName, updateRequest.getShortName());
        applyIfNotNull(company::setActualAddress, updateRequest.getActualAddress());
        applyIfNotNull(company::setLegalAddress, updateRequest.getLegalAddress());

        companyService.save(company);
    }

    private void applyIfNotNull(Consumer<String> consumer, String argument){
        if (argument != null){
            consumer.accept(argument);
        }
    }

    private Profile fillProfile(User user) {
        Profile profile = userProfileConverter.convert(user);
        profile.setCreditOrganisation(bankService.getByCompany(user.getCompany()) != null);
        return profile;
    }
}
