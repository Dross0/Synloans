package com.synloans.loans.service.user.authentication.registration.impl;

import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.user.RoleRepository;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import com.synloans.loans.service.user.UserService;
import com.synloans.loans.service.user.authentication.registration.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final CompanyService companyService;
    private final BankService bankService;
    private final RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegistrationRequest registrationRequest){
        Company company = new Company();
        company.setInn(registrationRequest.getInn());
        company.setKpp(registrationRequest.getKpp());
        company.setFullName(registrationRequest.getFullName());
        company.setShortName(registrationRequest.getShortName());
        company.setActualAddress(registrationRequest.getActualAddress());
        company.setLegalAddress(registrationRequest.getLegalAddress());
        if (registrationRequest.isCreditOrganisation()){
            return registerBank(
                    registrationRequest.getEmail(),
                    registrationRequest.getPassword(),
                    company
            );
        } else {
            return registerCompany(
                    registrationRequest.getEmail(),
                    registrationRequest.getPassword(),
                    company
            );
        }
    }

    private User registerCompany(String username, String password, Company companyInfo){
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword);
        Company company = companyService.getByInnAndKpp(companyInfo.getInn(), companyInfo.getKpp())
                .orElseGet(() -> companyService.create(companyInfo));
        if (company == null){
            log.error("Не удалось найти/создать компанию с инн={} и кпп={}", companyInfo.getInn(), companyInfo.getKpp());
            throw new CompanyNotFoundException("Не удалось найти/создать компанию с инн="
                    + companyInfo.getInn() + " и кпп=" + companyInfo.getKpp());
        }
        user.setCompany(company);
        user.setRoles(
                Set.of(
                        roleRepository.findByName(UserRole.ROLE_COMPANY)
                )
        );
        return userService.saveUser(user);
    }


    private User registerBank(String email, String password, Company companyInfo) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        Company company = companyService.getByInnAndKpp(companyInfo.getInn(), companyInfo.getKpp()).orElse(null);
        Bank bank;
        if (company != null){
            bank = bankService.getByCompany(company);
            if (bank == null){
                throw new BankNotFoundException("Пользователь пытается зарегистрироваться по реквизитам компании, которая не является банком");
            }
        } else{
            company = companyService.create(companyInfo);
            if (bankService.createBank(company) == null){
                log.error("Не удалось найти/создать банк: {}", companyInfo.getFullName());
                throw new BankNotFoundException("Ну удалось найти или создать банк = " + companyInfo.getFullName());
            }
        }
        user.setCompany(company);
        user.setRoles(
                Set.of(
                        roleRepository.findByName(UserRole.ROLE_COMPANY),
                        roleRepository.findByName(UserRole.ROLE_BANK)
                )
        );
        return userService.saveUser(user);
    }
}
