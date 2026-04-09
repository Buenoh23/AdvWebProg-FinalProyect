package com.example.final_proyect.controller;

import com.example.final_proyect.entity.Category;
import com.example.final_proyect.entity.Product;
import com.example.final_proyect.service.CategoryService;
import com.example.final_proyect.service.OrderService;
import com.example.final_proyect.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminController(OrderService orderService, ProductService productService, CategoryService categoryService) {
        this.orderService = orderService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        return "admin-dashboard";
    }

    @PostMapping("/category/create")
    public String createCategory(@RequestParam String name) {
        Category category = new Category();
        category.setName(name);
        categoryService.createCategory(category);
        return "redirect:/admin/dashboard?success=Category Created";
    }

    @PostMapping("/product/create")
    public String createProduct(@RequestParam String name, @RequestParam String description,
                                @RequestParam BigDecimal price, @RequestParam Integer stockQty,
                                @RequestParam Long categoryId) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setActive(true);
        product.setCategory(categoryService.findById(categoryId));
        
        productService.saveProduct(product);
        return "redirect:/admin/dashboard?success=Product Created";
    }

    @GetMapping("/product/{id}/edit")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "admin-edit-product";
    }

    @PostMapping("/product/{id}/edit")
    public String editProduct(@PathVariable Long id, @RequestParam String name, 
                              @RequestParam String description, @RequestParam BigDecimal price, 
                              @RequestParam Integer stockQty, @RequestParam Long categoryId) {
        Product product = productService.findById(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setCategory(categoryService.findById(categoryId));
        
        productService.saveProduct(product);
        return "redirect:/admin/dashboard?success=Product Updated";
    }

    @PostMapping("/product/{id}/deactivate")
    public String deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return "redirect:/admin/dashboard?success=Product Deactivated";
    }

    @PostMapping("/order/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return "redirect:/admin/dashboard?success=Order Status Updated";
        } catch (IllegalStateException e) {
            return "redirect:/admin/dashboard?error=" + e.getMessage();
        }
    }
}