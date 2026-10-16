package com.java.toystore.repository;

import com.java.toystore.domain.model.Toy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToyRepository extends JpaRepository<Toy, Integer> {
}
