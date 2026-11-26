package com.java.toystore.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToyResponse {
    private Integer id;
    private String sku;
    private String name;
    private String category;
    private String brand;
    private String recommendedAge;
    private BigDecimal price;
    private int stock;
}
