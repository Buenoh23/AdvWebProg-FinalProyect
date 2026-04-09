package com.example.final_proyect.service;

import com.example.final_proyect.entity.Product;
import com.example.final_proyect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    public void deactivateProduct(Long id) {
        Product product = findById(id);
        if (product != null) {
            product.setActive(false);
            productRepo.save(product);
        }
    }

    public Page<Product> getCatalog(String name, Long categoryId, Boolean inStock, 
                                    int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepo.searchCatalog(name, categoryId, inStock, pageable);
    }
}