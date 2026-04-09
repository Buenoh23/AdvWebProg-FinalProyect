package com.example.final_proyect.service;

import com.example.final_proyect.entity.Product;
import com.example.final_proyect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }

    public Product findById(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    // REGLA DEL PROYECTO: Soft Delete
    public void deactivateProduct(Long id) {
        Product product = findById(id);
        if (product != null) {
            product.setActive(false); // No lo borramos, solo lo ocultamos
            productRepo.save(product);
        }
    }
}