package com.example.final_proyect.controller;

import com.example.final_proyect.entity.User;
import com.example.final_proyect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Show the custom Login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // 2. Show the Registration page
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    // 3. Process the Registration form submission
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password) {

        // Check if email already exists
        if (userRepo.findByEmail(email) != null) {
            // Redirect back to register with an error flag
            return "redirect:/register?error=emailExists";
        }

        // Create the new customer
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password)); // Hash it!
        newUser.setRole("CUSTOMER"); // Hardcoded to CUSTOMER for safety

        userRepo.save(newUser);

        // Redirect to login page with a success message
        return "redirect:/login?registered=true";
    }
}