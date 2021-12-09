package com.synloans.loans.security;

import com.synloans.loans.model.entity.Role;
import com.synloans.loans.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@RequiredArgsConstructor
public class RolesSetup implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;

    boolean alreadySetup = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        createRoleIfNotFound(UserRole.ROLE_BANK);
        createRoleIfNotFound(UserRole.ROLE_COMPANY);

        alreadySetup = true;
    }


    @Transactional
    void createRoleIfNotFound(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}