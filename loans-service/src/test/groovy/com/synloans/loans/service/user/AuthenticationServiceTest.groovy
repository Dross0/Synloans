package com.synloans.loans.service.user

import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.user.Role
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.repository.user.RoleRepository
import com.synloans.loans.security.UserRole
import com.synloans.loans.security.util.JwtService
import com.synloans.loans.service.company.BankService
import com.synloans.loans.service.company.CompanyService
import com.synloans.loans.service.exception.CreateUserException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

class AuthenticationServiceTest extends Specification{
    private AuthenticationService authenticationService
    private UserService userService
    private CompanyService companyService
    private BankService bankService
    private RoleRepository roleRepository
    private AuthenticationManager authenticationManager
    private JwtService jwtService
    private BCryptPasswordEncoder passwordEncoder

    def setup(){
        userService = Mock(UserService)
        companyService = Mock(CompanyService)
        bankService = Mock(BankService)
        roleRepository = Mock(RoleRepository)
        passwordEncoder = Mock(BCryptPasswordEncoder)
        authenticationManager = Mock(AuthenticationManager)
        jwtService = Mock(JwtService)
        authenticationService= new AuthenticationService(
                userService,
                companyService,
                bankService,
                roleRepository,
                authenticationManager,
                jwtService,
                passwordEncoder
        )
    }


    def "Тест. Регистрация пользователя как компания"(){
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
            def user = authenticationService.register(username, password, companyInfo, false)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> companyByInnAndKppOp
            if (companyByInnAndKppOp.isEmpty()){
                1 * companyService.create(companyInfo) >> newCompany
            }
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * userService.saveUser(_ as User) >> { User u -> u}
            user.username == username
            user.company == companyByInnAndKppOp.orElse(newCompany)
            user.password == encodedPassword
            user.roles == [companyRole] as Set
        where:
            companyByInnAndKppOp       || newCompany
            Optional.empty()           || Stub(Company)
            Optional.of(Stub(Company)) || null
    }

    def "Тест. Ошибка при получении компании при регистрации пользователя компании"(){
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
            authenticationService.register(username, password, companyInfo, false)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> null
            0 * userService.saveUser(_ as User)
            thrown(CreateUserException)
    }


    def "Тест. Регистрация пользователя как существующий банк"(){
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
            def user = authenticationService.registerBank(username, password, companyInfo)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.of(company)
            1 * bankService.getByCompany(company) >> Stub(Bank)
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * roleRepository.findByName(UserRole.ROLE_BANK) >> bankRole
            1 * userService.saveUser(_ as User) >> { User u -> u}
            user.username == username
            user.company == company
            user.password == encodedPassword
            user.roles == [companyRole, bankRole] as Set
    }

    def "Тест. Регистрация пользователя банка для компании которая не является банком"(){
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
            authenticationService.register(username, password, companyInfo, true)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.of(company)
            1 * bankService.getByCompany(company) >> null
            thrown(CreateUserException)
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
            authenticationService.register(username, password, companyInfo, true)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> company
            1 * bankService.createBank(company) >> null
            thrown(CreateUserException)
    }

    def "Тест. Регистрация пользователя как новый банк"(){
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
            def user = authenticationService.register(username, password, companyInfo, true)
        then:
            1 * companyService.getByInnAndKpp(inn, kpp) >> Optional.empty()
            1 * companyService.create(companyInfo) >> company
            1 * bankService.createBank(company) >> Stub(Bank)
            1 * roleRepository.findByName(UserRole.ROLE_COMPANY) >> companyRole
            1 * roleRepository.findByName(UserRole.ROLE_BANK) >> bankRole
            1 * userService.saveUser(_ as User) >> { User u -> u}
            user.username == username
            user.company == company
            user.password == encodedPassword
            user.roles == [companyRole, bankRole] as Set
    }

    def "Тест. Неуспешный логин пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
        when:
            def jwt = authenticationService.login(email, password)
        then:
            1 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)) >> {throw new BadCredentialsException("")}
            thrown(BadCredentialsException)
    }

    def "Тест. Успешный логин пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
            def generatedToken = "tokenValue"
            def user = Stub(User)
        when:
            def jwt = authenticationService.login(email, password)
        then:
            1 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
            1 * userService.loadUserByUsername(email) >> user
            1 * jwtService.generateToken(user) >> generatedToken
            jwt == generatedToken
    }
}
