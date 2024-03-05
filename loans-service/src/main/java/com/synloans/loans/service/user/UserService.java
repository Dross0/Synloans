package com.synloans.loans.service.user;

import com.synloans.loans.model.entity.user.User;
import org.springframework.security.core.Authentication;

import java.util.Collection;

public interface UserService {

    User getCurrentUser(Authentication authentication);

    User getCurrentUser();

    User getUserByUsername(String username);

    Collection<User> getAllUsers();

    User getUserById(Long id);

    User saveUser(User user);

    boolean deleteById(Long id);

}
