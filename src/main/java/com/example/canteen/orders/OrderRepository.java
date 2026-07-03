package com.example.canteen.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

        List<Order> findByUserEmail(String userEmail);

        List<Order> findByUserEmailAndServedFalseAndPaymentStatus(
                        String userEmail,
                        String paymentStatus);

        List<Order> findByServedFalseAndPaymentStatus(
                        String paymentStatus);

        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        long countByServedFalseAndCreatedAtBetween(
                        LocalDateTime start,
                        LocalDateTime end);

        @Query("""
                            SELECT COALESCE(SUM(o.totalAmount), 0)
                            FROM Order o
                            WHERE o.paymentStatus = 'SUCCESS'
                              AND o.createdAt BETWEEN :start AND :end
                        """)
        Double getTodayRevenue(LocalDateTime start, LocalDateTime end);

        @Query("""
                            SELECT o
                            FROM Order o
                            WHERE o.userEmail = :email
                              AND (o.served = true OR o.paymentStatus = 'REFUNDED')
                        """)
        List<Order> getHistory(@Param("email") String email);
}