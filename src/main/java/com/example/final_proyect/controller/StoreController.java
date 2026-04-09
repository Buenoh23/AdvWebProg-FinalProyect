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

    // 1. Declare all the services and repos this controller needs
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    // 2. The Constructor: This is where Spring Boot "wires" them together
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
            // 3. Find out exactly who is logged in right now
            String email = principal.getName();
            
            // 4. Use the wired UserRepository to get their real user record
            User customer = userRepo.findByEmail(email); 

            // Extract the cart items from the HTML form
            Map<Long, Integer> cartItems = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("qty_"))
                .filter(e -> !e.getValue().trim().isEmpty() && Integer.parseInt(e.getValue()) > 0)
                .collect(Collectors.toMap(
                    e -> Long.parseLong(e.getKey().replace("qty_", "")),
                    e -> Integer.parseInt(e.getValue())
                ));

            // 5. Use the wired OrderService to safely create the order!
            orderService.createOrder(customer.getId(), cartItems); 

            return "redirect:/order/history?success=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/order/build?error=" + e.getMessage();
        }
    }

    // 1. Show the Customer's Order History
    @GetMapping("/order/history")
    public String showOrderHistory(Principal principal, Model model) {
        String email = principal.getName();
        User customer = userRepo.findByEmail(email);
        
        // Fetch only the orders belonging to this specific customer
        model.addAttribute("orders", orderService.getMyOrders(customer.getId()));
        return "order-history";
    }

    // 2. Show the specific Order Receipt (Details)
    @GetMapping("/order/{id}")
    public String showOrderReceipt(@org.springframework.web.bind.annotation.PathVariable Long id, Principal principal, Model model) {
        String email = principal.getName();
        User customer = userRepo.findByEmail(email);
        
        var order = orderService.getOrderById(id);

        // SECURITY CHECK: Ownership Enforcement! 
        // If the order doesn't exist, or it belongs to a different customer, block them!
        if (order == null || !order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/403"; 
        }

        model.addAttribute("order", order);
        model.addAttribute("items", orderService.getOrderItems(id));
        return "order-receipt";
    }

    // 3. The Forbidden Error Page
    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }
}