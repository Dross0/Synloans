package com.synloans.loans.service.user;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.user.RoleRepository;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.security.util.JwtService;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import lombok.RequiredArgsConstructor;
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

    private final BCryptPasswordEncoder passwordEncoder;


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
            log.error("???? ?????????????? ??????????/?????????????? ???????????????? ?? ??????={} ?? ??????={}", companyInfo.getInn(), companyInfo.getKpp());
            throw new CompanyNotFoundException("???? ?????????????? ??????????/?????????????? ???????????????? ?? ??????="
                    + companyInfo.getInn() + " ?? ??????=" + companyInfo.getKpp());
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
                throw new BankNotFoundException("???????????????????????? ???????????????? ???????????????????????????????????? ???? ???????????????????? ????????????????, ?????????????? ???? ???????????????? ????????????");
            }
        } else{
            company = companyService.create(companyInfo);
            if (bankService.createBank(company) == null){
                log.error("???? ?????????????? ??????????/?????????????? ????????: {}", companyInfo.getFullName());
                throw new BankNotFoundException("???? ?????????????? ?????????? ?????? ?????????????? ???????? = " + companyInfo.getFullName());
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
