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

    @Transactional
    public OrderEntity createOrder(Long customerId, Map<Long, Integer> cartItems) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("NEW");
        order.setTotal(BigDecimal.ZERO);

        order = orderRepo.save(order);

        BigDecimal finalTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();

            if (qty == null || qty <= 0) continue;

            Product product = productService.findById(productId);
            
            if (product == null || !product.isActive()) {
                throw new IllegalArgumentException("Product is not available.");
            }

            if (product.getStockQty() < qty) {
                throw new IllegalArgumentException("Not enough stock for: " + product.getName());
            }

            product.setStockQty(product.getStockQty() - qty);
            productService.saveProduct(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQty(qty);
            item.setUnitPrice(product.getPrice());

            orderItemRepo.save(item);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            finalTotal = finalTotal.add(itemTotal);
        }

        if (finalTotal.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Order must contain at least one valid item.");
        }

        order.setTotal(finalTotal);
        return orderRepo.save(order);
    }

    @Transactional
    public OrderEntity updateOrderStatus(Long orderId, String newStatus) {
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        String currentStatus = order.getStatus();

        if ("FULFILLED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change the status of a terminal order.");
        }

        if ("CANCELLED".equals(newStatus)) {
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
            for (OrderItem item : items) {
                Product p = item.getProduct();
                p.setStockQty(p.getStockQty() + item.getQty());
                productService.saveProduct(p);
            }
        }

        order.setStatus(newStatus);
        return orderRepo.save(order);
    }

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
