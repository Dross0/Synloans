package com.synloans.loans.service

import com.synloans.loans.model.entity.Bank
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.Role
import com.synloans.loans.model.entity.User
import com.synloans.loans.repositories.RoleRepository
import com.synloans.loans.repositories.UserRepository
import com.synloans.loans.security.UserRole
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

class UserServiceTest extends Specification{
    private UserService userService
    private UserRepository userRepository
    private CompanyService companyService
    private BankService bankService
    private RoleRepository roleRepository
    private BCryptPasswordEncoder passwordEncoder

    def setup(){
        userRepository = Mock(UserRepository)
        companyService = Mock(CompanyService)
        bankService = Mock(BankService)
        roleRepository = Mock(RoleRepository)
        passwordEncoder = Mock(BCryptPasswordEncoder)
        userService = new UserService(userRepository, companyService, bankService, roleRepository)
        userService.setPasswordEncoder(passwordEncoder)
    }


    def "Тест. Успешный поиск пользователя по username для UserDetails"(){
        given:
            def username = "dr"
            def user = Stub(User)
        when:
            def foundUser = userService.loadUserByUsername(username)
        then:
            1 * userRepository.findUserByUsername(username) >> user
            foundUser == user
    }

    def "Тест. Пользователь не найден по username для UserDetails"(){
        given:
            def username = "dr"
        when:
            def foundUser = userService.loadUserByUsername(username)
        then:
            1 * userRepository.findUserByUsername(username) >> null
            thrown(UsernameNotFoundException)
    }

    def "Тест. Получение пользователя по username"(){
        when:
            def foundUser = userService.getUserByUsername(username)
        then:
            1 * userRepository.findUserByUsername(username) >> user
            foundUser == user
        where:
            username || user
            "dross"  || Stub(User)
            "kfl"    || null
    }

    def "Тест. Получение пользователя по id"(){
        when:
            def foundUser = userService.getUserById(id)
        then:
            1 * userRepository.findById(id) >> userOp
            foundUser == userOp.orElse(null)
        where:
            id || userOp
            1  || Optional.of(Stub(User))
            2  || Optional.empty()
    }

    def "Тест. Получение всех пользователей"(){
        given:
            def users = [Stub(User), Stub(User), Stub(User)]
        when:
            def allUsers = userService.getAllUsers()
        then:
            1 * userRepository.findAll() >> users
            allUsers == users
    }

    def "Тест. Сохранение пользователя"(){
        given:
            def user = Stub(User)
        when:
            def savedUser = userService.saveUser(user)
        then:
            1 * userRepository.save(_ as User) >> {User u -> u}
            savedUser == user
    }

    def "Тест. Создание пользователя"(){
        given:
            def user = Stub(User)
            def username = "dross"
            user.username >> username
        when:
            def createdUser = userService.createUser(user)
        then:
            1 * userRepository.findUserByUsername(username) >> null
            1 * userRepository.save(_ as User) >> {User u -> u}
            createdUser == user
    }

    def "Тест. Создание пользователя с существующим usename"(){
        given:
            def user = Stub(User)
            def username = "dross"
            user.username >> username
        when:
            def createdUser = userService.createUser(user)
        then:
            1 * userRepository.findUserByUsername(username) >> Stub(User)
            0 * userRepository.save(_)
            createdUser == null
    }

    def "Тест. Создание пользователя c user == null"(){
        when:
            def createdUser = userService.createUser(null)
        then:
            0 * userRepository.findUserByUsername(_)
            0 * userRepository.save(_)
            createdUser == null
    }

    def "Тест. Удаление пользователя по id"(){
        given:
            int times = exist ? 1 : 0
        when:
            def status = userService.deleteById(id)
        then:
            1 * userRepository.existsById(id) >> exist
            times * userRepository.deleteById(id)

            status == exist
        where:
            id || exist
            1  || true
            13 || false
    }

    def "Тест. Создание пользователя как компания"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp

            }
            def encodedPassword = "encPass"
            passwordEncoder.encode(password) >> encodedPassword
            def companyRole = Stub(Role)
        when:
            def user = userService.createCorpUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> companyByInnAndKppOp
            if (companyByInnAndKppOp.isEmpty()){
                1 * companyService.create(companyInfo) >> newCompany
            }
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * userRepository.save(_ as User) >> {User u -> u}
            user.username == username
            user.company == companyByInnAndKppOp.orElse(newCompany)
            user.password == encodedPassword
            user.roles == [companyRole] as Set
        where:
            companyByInnAndKppOp       || newCompany
            Optional.empty()           || Stub(Company)
            Optional.of(Stub(Company)) || null
    }

    def "Тест. Ошибка при получении компании при создании пользователя компании"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp

            }

        when:
            def user = userService.createCorpUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> null
            0 * userRepository.save(_)
            user == null
    }


    def "Тест. Создание пользователя как существующий банк"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp
            }
            def encodedPassword = "encPass"
            passwordEncoder.encode(password) >> encodedPassword
            def companyRole = Stub(Role)
            def bankRole = Stub(Role)

            def company = Stub(Company)
        when:
            def user = userService.createBankUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.of(company)
            1 * bankService.getByCompany(company) >> Stub(Bank)
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * roleRepository.findByName(UserRole.ROLE_BANK) >> bankRole
            1 * userRepository.save(_ as User) >> {User u -> u}
            user.username == username
            user.company == company
            user.password == encodedPassword
            user.roles == [companyRole, bankRole] as Set
    }

    def "Тест. Создание пользователя банка для компании которая не является банком"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp
            }
            def company = Stub(Company)
        when:
            userService.createBankUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.of(company)
            1 * bankService.getByCompany(company) >> null
            thrown(IllegalArgumentException)
    }

    def "Тест. Ошибка при создании банка для нового пользователя"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp
            }
            def company = Stub(Company)
        when:
            def user = userService.createBankUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> company
            1 * bankService.createBank(company) >> null
            user == null
    }

    def "Тест. Создание пользователя как новый банк"(){
        given:
            def username = "dross"
            def password = "pass"
            def inn = "123"
            def kpp = "234"
            def companyInfo = Stub(Company){
                it.inn >> inn
                it.kpp >> kpp
            }
            def encodedPassword = "encPass"
            passwordEncoder.encode(password) >> encodedPassword
            def companyRole = Stub(Role)
            def bankRole = Stub(Role)

            def company = Stub(Company)
        when:
            def user = userService.createBankUser(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> company
            1 * bankService.createBank(company) >> Stub(Bank)
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * roleRepository.findByName(UserRole.ROLE_BANK) >> bankRole
            1 * userRepository.save(_ as User) >> {User u -> u}
            user.username == username
            user.company == company
            user.password == encodedPassword
            user.roles == [companyRole, bankRole] as Set
    }
}
