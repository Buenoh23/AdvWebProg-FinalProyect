package com.example.final_proyect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // BCrypt password hasher required by the assignment
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tells Spring to use our Database user service and the BCrypt encoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // CRITICAL FIX: Pass the userDetailsService directly into the constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**").permitAll() // Allow styling
                .requestMatchers("/register", "/login").permitAll() // Anyone can register/login
                .requestMatchers("/admin/**").hasRole("ADMIN") // ONLY Admins can access /admin URLs
                .anyRequest().authenticated() // Everything else requires you to be logged in
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/catalog", true) // After login, go to catalog
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403") // If a customer tries to open an admin page, show HTTP 403
            );

        return http.build();
    }
}