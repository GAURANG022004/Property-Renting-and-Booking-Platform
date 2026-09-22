package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.security.AuthResponse;
import com.gaurang.property_rental.dto.security.LoginRequest;
import com.gaurang.property_rental.dto.security.RegisterRequest;
import com.gaurang.property_rental.model.User;
import com.gaurang.property_rental.repository.UserRepository;
import com.gaurang.property_rental.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                Set.of("TENANT"),
                request.firstName().trim(),
                request.lastName().trim(),
                request.phoneNumber().trim()
        );

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail(), saved.getId(), saved.getRoles());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRoles());
        return new AuthResponse(token);
    }
}
