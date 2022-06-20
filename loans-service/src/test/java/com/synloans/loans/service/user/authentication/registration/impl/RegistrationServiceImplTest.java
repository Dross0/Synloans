package com.synloans.loans.service.user.authentication.registration.impl;

import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.user.Role;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.user.RoleRepository;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import com.synloans.loans.service.user.UserService;
import com.synloans.loans.service.user.authentication.registration.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegistrationServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegistrationServiceImplTest {

    @Autowired
    RegistrationService registrationService;

    @MockBean
    UserService userService;

    @MockBean
    CompanyService companyService;

    @MockBean
    BankService bankService;

    @MockBean
    RoleRepository roleRepository;

    @MockBean
    BCryptPasswordEncoder passwordEncoder;

    @Captor
    ArgumentCaptor<Company> companyArgumentCaptor;

    @Test
    @DisplayName("Регистрация пользователя в существующей компании")
    void registerUserForExistCompanyTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(false)
                .build();

        Role role = new Role();
        Company company = new Company();
        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(userService.saveUser(any())).thenAnswer(returnsFirstArg());

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.of(company));

        when(roleRepository.findByName(UserRole.ROLE_COMPANY)).thenReturn(role);

        User user = registrationService.register(registrationRequest);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getCompany()).isEqualTo(company);
        assertThat(user.getRoles()).containsOnly(role);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, times(1)).saveUser(user);
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_COMPANY);
        verify(companyService, never()).create(any());
    }

    @Test
    @DisplayName("Регистрация пользователя в новую компании")
    void registerUserForNewCompanyTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(false)
                .build();

        Role role = new Role();
        Company expectedCompany = new Company();
        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(userService.saveUser(any())).thenAnswer(returnsFirstArg());

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.empty());

        when(companyService.create(any())).thenReturn(expectedCompany);

        when(roleRepository.findByName(UserRole.ROLE_COMPANY)).thenReturn(role);

        User user = registrationService.register(registrationRequest);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getCompany()).isEqualTo(expectedCompany);
        assertThat(user.getRoles()).containsOnly(role);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, times(1)).saveUser(user);
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_COMPANY);
        verify(companyService, times(1)).create(companyArgumentCaptor.capture());

        Company company = companyArgumentCaptor.getValue();
        assertThat(company.getFullName()).isEqualTo(fullName);
        assertThat(company.getShortName()).isEqualTo(shortName);
        assertThat(company.getInn()).isEqualTo(inn);
        assertThat(company.getKpp()).isEqualTo(kpp);
        assertThat(company.getLegalAddress()).isEqualTo(legalAddress);
        assertThat(company.getActualAddress()).isEqualTo(actualAddress);
        assertThat(company.getNodes()).isNull();
    }

    @Test
    @DisplayName("Ошибка при поиске/создании компании при регистрации пользователя компании")
    void registerCompanyNotFoundTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(false)
                .build();

        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.empty());

        when(companyService.create(any())).thenReturn(null);

        Throwable throwable = catchThrowable(() -> registrationService.register(registrationRequest));
        assertThat(throwable).isInstanceOf(CompanyNotFoundException.class);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, never()).saveUser(any());
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, never()).findByName(UserRole.ROLE_COMPANY);
        verify(companyService, times(1)).create(companyArgumentCaptor.capture());

        Company company = companyArgumentCaptor.getValue();
        assertThat(company.getFullName()).isEqualTo(fullName);
        assertThat(company.getShortName()).isEqualTo(shortName);
        assertThat(company.getInn()).isEqualTo(inn);
        assertThat(company.getKpp()).isEqualTo(kpp);
        assertThat(company.getLegalAddress()).isEqualTo(legalAddress);
        assertThat(company.getActualAddress()).isEqualTo(actualAddress);
        assertThat(company.getNodes()).isNull();
    }

    @Test
    @DisplayName("Регистрация пользователя в существующий банк")
    void registerUserForExistBankTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(true)
                .build();

        Role companyRole = new Role();
        Role bankRole = new Role();
        Company company = new Company();
        Bank bank = new Bank();

        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(userService.saveUser(any())).thenAnswer(returnsFirstArg());

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.of(company));

        when(bankService.getByCompany(company)).thenReturn(bank);

        when(roleRepository.findByName(UserRole.ROLE_COMPANY)).thenReturn(companyRole);
        when(roleRepository.findByName(UserRole.ROLE_BANK)).thenReturn(bankRole);

        User user = registrationService.register(registrationRequest);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getCompany()).isEqualTo(company);
        assertThat(user.getRoles()).containsOnly(companyRole, bankRole);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, times(1)).saveUser(user);
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_COMPANY);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_BANK);
        verify(companyService, never()).create(any());
        verify(bankService, never()).createBank(any());
        verify(bankService, times(1)).getByCompany(company);
    }

    @Test
    @DisplayName("Регистрация пользователя в существующий банк, но реквизиты компании, а не банка")
    void registerUserForExistBankButCompanyFoundTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(true)
                .build();

        Company company = new Company();

        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.of(company));

        when(bankService.getByCompany(company)).thenReturn(null);

        Throwable throwable = catchThrowable(() -> registrationService.register(registrationRequest));
        assertThat(throwable).isInstanceOf(BankNotFoundException.class);


        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, never()).saveUser(any());
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, never()).findByName(UserRole.ROLE_COMPANY);
        verify(roleRepository, never()).findByName(UserRole.ROLE_BANK);
        verify(companyService, never()).create(any());
        verify(bankService, never()).createBank(any());
        verify(bankService, times(1)).getByCompany(company);
    }

    @Test
    @DisplayName("Регистрация пользователя в новый банк")
    void registerUserForNewBankTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(true)
                .build();

        Role companyRole = new Role();
        Role bankRole = new Role();
        Company expectedCompany = new Company();

        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(userService.saveUser(any())).thenAnswer(returnsFirstArg());

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.empty());

        when(companyService.create(any())).thenReturn(expectedCompany);

        when(bankService.createBank(expectedCompany)).thenReturn(new Bank());

        when(roleRepository.findByName(UserRole.ROLE_COMPANY)).thenReturn(companyRole);
        when(roleRepository.findByName(UserRole.ROLE_BANK)).thenReturn(bankRole);

        User user = registrationService.register(registrationRequest);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getCompany()).isEqualTo(expectedCompany);
        assertThat(user.getRoles()).containsOnly(companyRole, bankRole);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, times(1)).saveUser(user);
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_COMPANY);
        verify(roleRepository, times(1)).findByName(UserRole.ROLE_BANK);
        verify(companyService, times(1)).create(companyArgumentCaptor.capture());
        verify(bankService, times(1)).createBank(expectedCompany);
        verify(bankService, never()).getByCompany(any());

        Company company = companyArgumentCaptor.getValue();
        assertThat(company.getFullName()).isEqualTo(fullName);
        assertThat(company.getShortName()).isEqualTo(shortName);
        assertThat(company.getInn()).isEqualTo(inn);
        assertThat(company.getKpp()).isEqualTo(kpp);
        assertThat(company.getLegalAddress()).isEqualTo(legalAddress);
        assertThat(company.getActualAddress()).isEqualTo(actualAddress);
        assertThat(company.getNodes()).isNull();
    }

    @Test
    @DisplayName("Регистрация пользователя в новый банк, который не удалось создать")
    void registerUserForNewBankButCreationFailedTest(){
        String username = "user1";
        String password = "qwerty";
        String fullName = "comFN";
        String shortName = "comSN";
        String kpp = "121314";
        String inn = "434353453";
        String legalAddress = "comLA";
        String actualAddress = "comAA";

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email(username)
                .password(password)
                .fullName(fullName)
                .shortName(shortName)
                .kpp(kpp)
                .inn(inn)
                .legalAddress(legalAddress)
                .actualAddress(actualAddress)
                .creditOrganisation(true)
                .build();

        Company expectedCompany = new Company();

        String encodedPassword = "ecdeefe";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(companyService.getByInnAndKpp(inn, kpp)).thenReturn(Optional.empty());

        when(companyService.create(any())).thenReturn(expectedCompany);

        when(bankService.createBank(expectedCompany)).thenReturn(null);

        Throwable throwable = catchThrowable(() -> registrationService.register(registrationRequest));
        assertThat(throwable).isInstanceOf(BankNotFoundException.class);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userService, never()).saveUser(any());
        verify(companyService, times(1)).getByInnAndKpp(inn, kpp);
        verify(roleRepository, never()).findByName(UserRole.ROLE_COMPANY);
        verify(roleRepository, never()).findByName(UserRole.ROLE_BANK);
        verify(companyService, times(1)).create(companyArgumentCaptor.capture());
        verify(bankService, times(1)).createBank(expectedCompany);
        verify(bankService, never()).getByCompany(any());

        Company company = companyArgumentCaptor.getValue();
        assertThat(company.getFullName()).isEqualTo(fullName);
        assertThat(company.getShortName()).isEqualTo(shortName);
        assertThat(company.getInn()).isEqualTo(inn);
        assertThat(company.getKpp()).isEqualTo(kpp);
        assertThat(company.getLegalAddress()).isEqualTo(legalAddress);
        assertThat(company.getActualAddress()).isEqualTo(actualAddress);
        assertThat(company.getNodes()).isNull();
    }


}