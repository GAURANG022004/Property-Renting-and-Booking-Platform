package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRolesContaining(String role);
}
