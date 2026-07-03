package com.example.canteen.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order saveOrder(Order order) {
        order.setPaymentStatus("PENDING");

        // 🔥 VERY IMPORTANT
        // for (OrderItem item : order.getItems()) {
        // item.setOrder(order);
        // }
        double total = 0;

        // Calculate total amount
        for (OrderItem item : order.getItems()) {
            total += item.getPrice() * item.getQty();
            item.setOrder(order);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public List<Order> getPendingOrdersByUser(String email) {
        return orderRepository.findByUserEmailAndServedFalseAndPaymentStatus(
                email,
                "SUCCESS");
    }

    public List<Order> getAllPendingOrders() {
        return orderRepository.findByServedFalseAndPaymentStatus(
                "SUCCESS");
    }

    public Order markAsServed(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setServed(true);

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> gethistory(String email) {
        return orderRepository.getHistory(email);
    }
}