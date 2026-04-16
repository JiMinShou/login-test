package com.example.authapp.config;

import com.example.authapp.entity.Role;
import com.example.authapp.entity.User;
import com.example.authapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminSeedProperties properties;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminSeedProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (!properties.isEnabled() || userRepository.existsByEmailIgnoreCase(properties.getEmail())) {
            return;
        }
        User admin = new User();
        admin.setUsername(properties.getUsername());
        admin.setEmail(properties.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Admin seed user created: {}", properties.getEmail());
    }
}
