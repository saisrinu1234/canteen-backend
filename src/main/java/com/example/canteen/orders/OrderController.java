package com.example.canteen.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.canteen.menu.MenuRepository;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController

@RequestMapping("/orders")

public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MenuRepository menuRepository;

    // ✅ Place Order
    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(
            @RequestBody Order order,
            Principal principal) {

        // 🔐 Get logged-in user email from JWT
        String email = principal.getName();

        order.setUserEmail(email);

        Order savedOrder = orderService.saveOrder(order);

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/my/pending")
    public List<Order> getMyPendingOrders(Principal principal) {

        String email = principal.getName(); // ✅ comes from JWT

        return orderService.getPendingOrdersByUser(email);
    }

    @GetMapping("/served")
    public List<Order> getMyOrderHistoryList(Principal principal) {

        String email = principal.getName(); // ✅ comes from JWT

        return orderService.gethistory(email);
    }

    @GetMapping("/admin/pending-orders")
    public ResponseEntity<?> getPendingOrders() {
        return ResponseEntity.ok(orderService.getAllPendingOrders());
    }

    @PutMapping("/admin/serve/{id}")
    public ResponseEntity<?> markAsServed(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markAsServed(id));
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    public DashboardStats getStats() {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long totalItems = menuRepository.count();

        long todayOrders = orderRepository.countByCreatedAtBetween(start, end);

        long pendingOrders = orderRepository.countByServedFalseAndCreatedAtBetween(start, end);

        Double revenue = orderRepository.getTodayRevenue(start, end);

        return new DashboardStats(
                totalItems,
                todayOrders,
                pendingOrders,
                revenue == null ? 0 : revenue);
    }

}