package com.example.canteen.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.canteen.menu.MenuItem;
import com.example.canteen.menu.MenuRepository;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MenuRepository menuRepository;

    public Order saveOrder(Order order) {

        order.setPaymentStatus("PENDING");

        double total = 0;

        for (OrderItem item : order.getItems()) {

            MenuItem menuItem = menuRepository.findByName(item.getName())
                    .orElseThrow(() -> new RuntimeException(item.getName() + " not found"));

            if (!menuItem.isAvailable()) {
                throw new RuntimeException(menuItem.getName() + " is currently unavailable");
            }

            // Update latest price
            item.setPrice(menuItem.getPrice());

            item.setOrder(order);

            total += menuItem.getPrice() * item.getQty();
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