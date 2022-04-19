package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserProfileConverter implements Converter<User, Profile> {

    @Override
    public Profile convert(User user){
        Company company = user.getCompany();
        return Profile.builder()
                .email(user.getUsername())
                .fullName(company.getFullName())
                .shortName(company.getShortName())
                .inn(company.getInn())
                .kpp(company.getKpp())
                .actualAddress(company.getActualAddress())
                .legalAddress(company.getLegalAddress())
                .build();
    }
}
