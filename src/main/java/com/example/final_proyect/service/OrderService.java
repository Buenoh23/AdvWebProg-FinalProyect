package com.example.final_proyect.service;

import com.example.final_proyect.entity.OrderEntity;
import com.example.final_proyect.entity.OrderItem;
import com.example.final_proyect.entity.Product;
import com.example.final_proyect.entity.User;
import com.example.final_proyect.repository.OrderItemRepository;
import com.example.final_proyect.repository.OrderRepository;
import com.example.final_proyect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductService productService;
    private final UserRepository userRepo;

    public OrderService(OrderRepository orderRepo, OrderItemRepository orderItemRepo,
                        ProductService productService, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productService = productService;
        this.userRepo = userRepo;
    }

    // 1. Create a new multi-item order
    @Transactional
    public OrderEntity createOrder(Long customerId, Map<Long, Integer> cartItems) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Setup the initial Order record
        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("NEW");
        order.setTotal(BigDecimal.ZERO); // We will calculate this securely below

        // Save the order first so we have an ID to attach the OrderItems to
        order = orderRepo.save(order);

        BigDecimal finalTotal = BigDecimal.ZERO;

        // Loop through the "cart" (Map of ProductId -> Quantity)
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();

            if (qty == null || qty <= 0) continue; // Skip items with 0 quantity

            Product product = productService.findById(productId);
            
            if (product == null || !product.isActive()) {
                throw new IllegalArgumentException("Product is not available.");
            }

            if (product.getStockQty() < qty) {
                throw new IllegalArgumentException("Not enough stock for: " + product.getName());
            }

            // Deduct from inventory
            product.setStockQty(product.getStockQty() - qty);
            productService.saveProduct(product); // Save the new inventory count

            // Create the individual Order Item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQty(qty);
            item.setUnitPrice(product.getPrice()); // Snapshot the price NOW!

            orderItemRepo.save(item);

            // Calculate the total for this item row (qty * price) and add to the grand total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            finalTotal = finalTotal.add(itemTotal);
        }

        // Failsafe: Prevent empty orders
        if (finalTotal.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Order must contain at least one valid item.");
        }

        // Save the final calculated total
        order.setTotal(finalTotal);
        return orderRepo.save(order);
    }

    // 2. Update status (Admin Only)
    @Transactional
    public OrderEntity updateOrderStatus(Long orderId, String newStatus) {
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        String currentStatus = order.getStatus();

        // Enforce the Terminal State rule
        if ("FULFILLED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change the status of a terminal order.");
        }

        // Enforce the Stock Restoration rule
        if ("CANCELLED".equals(newStatus)) {
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
            for (OrderItem item : items) {
                Product p = item.getProduct();
                p.setStockQty(p.getStockQty() + item.getQty()); // Give the stock back
                productService.saveProduct(p);
            }
        }

        order.setStatus(newStatus);
        return orderRepo.save(order);
    }

    // 3. Helper methods to fetch data for the UI
    public List<OrderEntity> getMyOrders(Long customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    public List<OrderEntity> getAllOrders() {
        return orderRepo.findAll();
    }

    public OrderEntity getOrderById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepo.findByOrderId(orderId);
    }
}
