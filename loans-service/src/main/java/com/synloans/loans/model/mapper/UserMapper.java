package com.synloans.loans.model.mapper;

import com.synloans.loans.model.dto.Profile;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.User;

public class UserMapper {
    public Profile entityToDto(User user){
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
