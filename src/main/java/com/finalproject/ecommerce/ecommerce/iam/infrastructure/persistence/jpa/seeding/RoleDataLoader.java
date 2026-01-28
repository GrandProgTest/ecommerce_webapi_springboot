package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.seeding;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class RoleDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(RoleDataLoader.class);

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.stream(Roles.values()).forEach(roleName -> {
                if (!roleRepository.existsByName(roleName)) {
                    Role role = new Role(roleName);
                    roleRepository.save(role);
                    logger.info("Role {} created", roleName.name());
                } else {
                    logger.info("Role {} already exists", roleName.name());
                }
            });
        };
    }
}
