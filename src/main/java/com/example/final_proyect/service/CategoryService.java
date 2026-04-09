package com.example.final_proyect.service;

import com.example.final_proyect.entity.Category;
import com.example.final_proyect.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    public Category createCategory(Category category) {
        return categoryRepo.save(category);
    }

    public Category findById(Long id) {
        return categoryRepo.findById(id).orElse(null);
    }
}