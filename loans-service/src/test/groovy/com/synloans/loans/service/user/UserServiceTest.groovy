package com.synloans.loans.service.user


import com.synloans.loans.model.entity.User
import com.synloans.loans.repository.user.UserRepository
import com.synloans.loans.service.exception.CreateUserException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

class UserServiceTest extends Specification{
    private UserService userService
    private UserRepository userRepository

    def setup(){
        userRepository = Mock(UserRepository)
        userService = new UserService(userRepository)
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
            def username = "dross"
            user.username >> username
        when:
            def savedUser = userService.saveUser(user)
        then:
            1 * userRepository.findUserByUsername(username) >> null
            1 * userRepository.save(_ as User) >> {User u -> u}
            savedUser == user
    }

    def "Тест. Сохранение пользователя с существующим usename"(){
        given:
            def user = Stub(User)
            def username = "dross"
            user.username >> username
        when:
            userService.saveUser(user)
        then:
            1 * userRepository.findUserByUsername(username) >> Stub(User)
            0 * userRepository.save(_)
            thrown(CreateUserException)
    }

    def "Тест. Сохранение пользователя c user == null"(){
        when:
            userService.saveUser(null)
        then:
            0 * userRepository.findUserByUsername(_)
            0 * userRepository.save(_)
            thrown(CreateUserException)
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
}
