package com.synloans.loans.service;

import com.synloans.loans.model.dto.Profile;
import com.synloans.loans.model.entity.Company;
import com.synloans.loans.model.entity.User;
import com.synloans.loans.model.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserMapper userMapper = new UserMapper();

    private final UserService userService;
    private final BankService bankService;
    private final CompanyService companyService;

    public Profile getProfile(String username){
        User user = userService.getUserByUsername(username);
        return fillProfile(user);
    }

    @Transactional
    public void editProfile(String username, Profile newProfile){
        User user = userService.getUserByUsername(username);
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
        Profile profile = userMapper.entityToDto(user);
        profile.setCreditOrganisation(bankService.getByCompany(user.getCompany()) != null);
        return profile;
    }
}
