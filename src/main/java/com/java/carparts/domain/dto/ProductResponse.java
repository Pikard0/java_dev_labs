package com.java.carparts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String sku;
    private String name;
    private String category;
    private String manufacturer;
    private String compatibleModel;
    private BigDecimal price;
    private int stock;
}
