package com.synloans.loans.service.bank.impl;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.repository.company.BankRepository;
import com.synloans.loans.service.bank.BankService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BankServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankServiceImplTest {

    @Autowired
    BankService bankService;

    @MockBean
    BankRepository bankRepository;

    @Test
    @DisplayName("Создание банка с лицензией")
    void createBankWithLicenseTest(){
        Company company = new Company();
        Document license = new Document();

        when(bankRepository.save(any(Bank.class))).thenAnswer(returnsFirstArg());

        Bank bank = bankService.createBank(company, license);

        assertThat(bank.getLicense()).isEqualTo(license);
        assertThat(bank.getCompany()).isEqualTo(company);

        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    @DisplayName("Создание банка без лицензией")
    void createBankWithoutLicenseTest(){
        Company company = new Company();

        when(bankRepository.save(any(Bank.class))).thenAnswer(returnsFirstArg());

        Bank bank = bankService.createBank(company);

        assertThat(bank.getLicense()).isNull();
        assertThat(bank.getCompany()).isEqualTo(company);

        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    @DisplayName("Сохранение банка")
    void saveBankTest(){
        Bank expectedBank = new Bank();

        when(bankRepository.save(any(Bank.class))).thenAnswer(returnsFirstArg());

        Bank bank = bankService.save(expectedBank);

        assertThat(bank).isEqualTo(expectedBank);

        verify(bankRepository, times(1)).save(expectedBank);
    }

    @Test
    @DisplayName("Получение всех банков")
    void getAllBanksTest(){
        Bank bank1 = new Bank();
        Bank bank2 = new Bank();

        when(bankRepository.findAll()).thenReturn(List.of(bank1, bank2));

        Collection<Bank> banks = bankService.getAll();

        assertThat(banks).containsExactly(bank1, bank2);

        verify(bankRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение банка по id")
    void getBankByIdTest(){
        long id = 12L;
        Bank expectedBank = new Bank();

        when(bankRepository.findById(id)).thenReturn(Optional.of(expectedBank));

        Optional<Bank> bank = bankService.getById(id);

        assertThat(bank).contains(expectedBank);

        verify(bankRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Получение банка по id, банк не найден")
    void getBankByIdNotFoundTest(){
        long id = 12L;

        when(bankRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Bank> bank = bankService.getById(id);

        assertThat(bank).isEmpty();

        verify(bankRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Получение банка по компании")
    void getBankByCompanyTest(){
        Bank expectedBank = new Bank();
        Company company = new Company();

        when(bankRepository.findByCompany(company)).thenReturn(Optional.of(expectedBank));

        Optional<Bank> bank = bankService.getByCompany(company);

        assertThat(bank).contains(expectedBank);

        verify(bankRepository, times(1)).findByCompany(company);
    }

    @Test
    @DisplayName("Получение банка по компании, банк не найден")
    void getBankByCompanyNotFoundTest(){
        Company company = new Company();

        when(bankRepository.findByCompany(company)).thenReturn(Optional.empty());

        Optional<Bank> bank = bankService.getByCompany(company);

        assertThat(bank).isEmpty();

        verify(bankRepository, times(1)).findByCompany(company);
    }
}
