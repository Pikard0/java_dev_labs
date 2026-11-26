package com.java.toystore.repository;

import com.java.toystore.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.OffsetDateTime;
import com.java.toystore.domain.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.items oi " +
            "JOIN FETCH oi.toy " +
            "WHERE o.customerUsername = :username")
    List<Order> findByCustomerUsername(@Param("username") String username);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items oi JOIN FETCH oi.toy")
    List<Order> findAllWithItems();

    List<Order> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(OrderStatus status, OffsetDateTime createdAt);
}
