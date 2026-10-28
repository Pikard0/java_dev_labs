package com.java.toystore.repository;

import com.java.toystore.domain.model.Toy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToyRepository extends JpaRepository<Toy, Integer> {

    List<Toy> findByStockLessThanEqualOrderByStockAsc(int stock);
}
