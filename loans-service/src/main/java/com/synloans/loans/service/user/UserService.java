package com.synloans.loans.service.user;

import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.Company;
import com.synloans.loans.model.entity.User;
import com.synloans.loans.repositories.RoleRepository;
import com.synloans.loans.repositories.UserRepository;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.security.util.JwtService;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.CreateUserException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final BankService bankService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Setter
    private BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null){
            log.error("Пользователь с usename={} не найден", username);
            throw new UsernameNotFoundException("Пользователь с username=" + username + " не найден");
        }
        return user;
    }

    public String login(String username, String password){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails user = loadUserByUsername(username);
        return jwtService.generateToken(user);
    }

    public User getUserByUsername(String username){
        return userRepository.findUserByUsername(username);
    }


    public Collection<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User createUser(String username, String password, Company companyInfo, boolean isCreditOrganisation){
        if (isCreditOrganisation){
            return createBankUser(username, password, companyInfo);
        } else {
            return createCorpUser(username, password, companyInfo);
        }
    }

    @Transactional
    public User saveUser(User user){
        if (user == null){
            log.error("User == null");
            throw new CreateUserException("Пользователь не задан");
        }
        if (userRepository.findUserByUsername(user.getUsername()) != null){
            log.error("Пользователь с username={} уже существует", user.getUsername());
            throw new CreateUserException("Пользователь с username=" + user.getUsername() + " уже существует");
        }
        return userRepository.save(user);
    }


    @Transactional
    public User createCorpUser(String username, String password, Company companyInfo){
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
        return saveUser(user);
    }

    @Transactional
    public boolean deleteById(Long id){
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public User createBankUser(String email, String password, Company companyInfo) {
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
        return saveUser(user);
    }
}
