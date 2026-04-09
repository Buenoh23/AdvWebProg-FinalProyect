package com.example.final_proyect.config;

import com.example.final_proyect.entity.User;
import com.example.final_proyect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // If the admin doesn't exist yet, create it!
        if (userRepo.findByEmail("admin@example.com") == null) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@example.com");
            // Hash the password securely!
            admin.setPasswordHash(passwordEncoder.encode("admin123")); 
            admin.setRole("ADMIN");
            
            userRepo.save(admin);
            System.out.println("============= SEEDER =============");
            System.out.println("Admin account created:");
            System.out.println("Email: admin@example.com");
            System.out.println("Password: admin123");
            System.out.println("==================================");
        }
    }
}