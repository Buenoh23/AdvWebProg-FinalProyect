package com.example.final_proyect.controller;

import com.example.final_proyect.entity.Product;
import com.example.final_proyect.entity.User;
import com.example.final_proyect.repository.UserRepository;
import com.example.final_proyect.service.CategoryService;
import com.example.final_proyect.service.OrderService;
import com.example.final_proyect.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StoreController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    public StoreController(ProductService productService, CategoryService categoryService, 
                           OrderService orderService, UserRepository userRepo) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userRepo = userRepo;
    }

    @GetMapping("/catalog")
    public String showCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean inStock,
            Model model) {

        Page<Product> productPage = productService.getCatalog(name, categoryId, inStock, page, size, sortBy, sortDir);
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.findAll());
        
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("inStock", inStock);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("size", size);

        return "catalog"; 
    }

    @GetMapping("/product/{id}")
    public String showProductDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null || !product.isActive()) {
            return "redirect:/catalog?error=NotFound";
        }
        model.addAttribute("product", product);
        return "product-details";
    }

    @GetMapping("/order/build")
    public String showOrderBuilder(Model model) {
        model.addAttribute("products", productService.findAll().stream().filter(Product::isActive).toList());
        return "order-builder";
    }

    @PostMapping("/order/place")
    public String placeOrder(@RequestParam Map<String, String> allParams, Principal principal) {
        try {
            String email = principal.getName();

            User customer = userRepo.findByEmail(email); 

            Map<Long, Integer> cartItems = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("qty_"))
                .filter(e -> !e.getValue().trim().isEmpty() && Integer.parseInt(e.getValue()) > 0)
                .collect(Collectors.toMap(
                    e -> Long.parseLong(e.getKey().replace("qty_", "")),
                    e -> Integer.parseInt(e.getValue())
                ));

            orderService.createOrder(customer.getId(), cartItems); 

            return "redirect:/order/history?success=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/order/build?error=" + e.getMessage();
        }
    }

    @GetMapping("/order/history")
    public String showOrderHistory(Principal principal, Model model) {
        String email = principal.getName();
        User customer = userRepo.findByEmail(email);

        model.addAttribute("orders", orderService.getMyOrders(customer.getId()));
        return "order-history";
    }

    @GetMapping("/order/{id}")
    public String showOrderReceipt(@org.springframework.web.bind.annotation.PathVariable Long id, Principal principal, Model model) {
        String email = principal.getName();
        User customer = userRepo.findByEmail(email);
        
        var order = orderService.getOrderById(id);

        if (order == null || !order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/403"; 
        }

        model.addAttribute("order", order);
        model.addAttribute("items", orderService.getOrderItems(id));
        return "order-receipt";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }
}