package com.example.final_proyect.config;

import com.example.final_proyect.entity.Category;
import com.example.final_proyect.entity.Product;
import com.example.final_proyect.entity.User;
import com.example.final_proyect.repository.CategoryRepository;
import com.example.final_proyect.repository.ProductRepository;
import com.example.final_proyect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepo, CategoryRepository categoryRepo, 
                      ProductRepository productRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.findByEmail("admin@example.com") == null) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123")); 
            admin.setRole("ADMIN");
            userRepo.save(admin);
            System.out.println("Seeded Admin Account.");
        }

        if (productRepo.count() == 0) {
            
            Category apparel = new Category();
            apparel.setName("Apparel");
            apparel = categoryRepo.save(apparel);

            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics = categoryRepo.save(electronics);

            createProduct("Campus Hoodie", "Warm and cozy", "45.00", 100, apparel);
            createProduct("Coffee Mug", "Ceramic with logo", "12.50", 50, apparel);
            createProduct("USB Flash Drive", "32GB storage", "15.00", 200, electronics);
            createProduct("Laptop Sticker", "Vinyl decal", "3.00", 0, apparel);
            createProduct("Wireless Mouse", "Bluetooth mouse", "25.00", 15, electronics);
            createProduct("Textbook Organizer", "Keep desk clean", "18.00", 30, apparel);

            System.out.println("Seeded 6 Products for Pagination Testing.");
        }
    }

    private void createProduct(String name, String desc, String price, int stock, Category cat) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStockQty(stock);
        p.setActive(true);
        p.setCategory(cat);
        productRepo.save(p);
    }
}