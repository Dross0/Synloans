package com.synloans.loans.service.user.impl;

import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.user.UserRepository;
import com.synloans.loans.service.exception.CreateUserException;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null){
            log.error("Пользователь с usename={} не найден", username);
            throw new UsernameNotFoundException("Пользователь с username=" + username + " не найден");
        }
        return user;
    }

    @Override
    public User getCurrentUser(Authentication authentication){
        String username = authentication.getName();
        User curUser = getUserByUsername(username);
        if (curUser == null){
            log.error("Не удалось найти текущего пользователя с username={}", username);
            throw new UserUnauthorizedException("Не удалось найти текущего пользователя с username=" + username);
        }
        return curUser;
    }


    @Override
    public User getUserByUsername(String username){
        return userRepository.findUserByUsername(username);
    }

    @Override
    public Collection<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
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
    @Override
    public boolean deleteById(Long id){
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
