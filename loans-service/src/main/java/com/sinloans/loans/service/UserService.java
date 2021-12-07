package com.sinloans.loans.service;

import com.sinloans.loans.model.entity.Bank;
import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.entity.User;
import com.sinloans.loans.repositories.RoleRepository;
import com.sinloans.loans.repositories.UserRepository;
import com.sinloans.loans.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public User createUser(User user){
        if (user == null){
            log.error("User == null");
            return null;
        }
        if (userRepository.findUserByUsername(user.getUsername()) != null){
            log.error("Пользователь с username={} уже существует", user.getUsername());
            return null;
        }
        return userRepository.save(user);
    }

    public User saveUser(User user){
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
            return null;
        }
        user.setCompany(company);
        user.setRoles(
                Set.of(
                        roleRepository.findByName(UserRole.ROLE_COMPANY)
                )
        );
        return createUser(user);
    }

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
                throw new IllegalArgumentException("Пользователь пытается зарегистрироваться по реквизитам компании, которая не является банком");
            }
        } else{
            company = companyService.create(companyInfo);
            bank = bankService.createBank(company);
        }
        if (bank == null){
            log.error("Не удалось найти/создать банк: {}", companyInfo.getFullName());
            return null;
        }
        user.setCompany(company);
        user.setRoles(
                Set.of(
                        roleRepository.findByName(UserRole.ROLE_COMPANY),
                        roleRepository.findByName(UserRole.ROLE_BANK)
                )
        );
        return createUser(user);
    }
}
