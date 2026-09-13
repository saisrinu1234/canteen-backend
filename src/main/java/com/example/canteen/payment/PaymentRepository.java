package com.example.canteen.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.canteen.orders.Order;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment,Long>{

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByOrder(Order order);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.order = :order")
    Optional<Payment> findByOrderForUpdate(@Param("order") Order order);

}
