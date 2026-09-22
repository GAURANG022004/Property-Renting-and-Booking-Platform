package com.gaurang.property_rental.config;

import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.adminEmail:admin@pr.in}")
    private String adminEmail;

    @Value("${app.adminPassword:Admin@123}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Preserve access for accounts created before the USER role was renamed to TENANT.
        userRepository.findAll().forEach(user -> {
            if (user.getRoles().remove("USER")) {
                user.getRoles().add("TENANT");
                userRepository.save(user);
            }
        });

        if (userRepository.findByEmail(adminEmail).isPresent()) return;

        User admin = new User(
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Set.of("ADMIN")
        );
        userRepository.save(admin);
    }
}
