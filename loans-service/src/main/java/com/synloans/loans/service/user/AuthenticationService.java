package com.synloans.loans.service.user;

import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.Company;
import com.synloans.loans.model.entity.User;
import com.synloans.loans.repository.user.RoleRepository;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.security.util.JwtService;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.CreateUserException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserService userService;
    private final CompanyService companyService;
    private final BankService bankService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Setter
    private BCryptPasswordEncoder passwordEncoder;


    public String login(String username, String password){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails user = userService.loadUserByUsername(username);
        return jwtService.generateToken(user);
    }


    @Transactional
    public User register(String username, String password, Company companyInfo, boolean isCreditOrganisation){
        if (isCreditOrganisation){
            return registerBank(username, password, companyInfo);
        } else {
            return registerCompany(username, password, companyInfo);
        }
    }

    @Transactional
    public User registerCompany(String username, String password, Company companyInfo){
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword);
        Company company = companyService.getByInnAndKpp(companyInfo.getInn(), companyInfo.getKpp())
                .orElseGet(() -> companyService.create(companyInfo));
        if (company == null){
            log.error("Не удалось найти/создать компанию с инн={} и кпп={}", companyInfo.getInn(), companyInfo.getKpp());
            throw new CreateUserException("Не удалось найти/создать компанию с инн="
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


    @Transactional
    public User registerBank(String email, String password, Company companyInfo) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        Company company = companyService.getByInnAndKpp(companyInfo.getInn(), companyInfo.getKpp()).orElse(null);
        Bank bank;
        if (company != null){
            bank = bankService.getByCompany(company);
            if (bank == null){
                throw new CreateUserException("Пользователь пытается зарегистрироваться по реквизитам компании, которая не является банком");
            }
        } else{
            company = companyService.create(companyInfo);
            if (bankService.createBank(company) == null){
                log.error("Не удалось найти/создать банк: {}", companyInfo.getFullName());
                throw new CreateUserException("Ну удалось найти или создать банк = " + companyInfo.getFullName());
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
