package com.java.toystore.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import com.java.toystore.validation.ValidToyCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidToyCategory
public class ToyUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String brand;

    private String recommendedAge;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Min(0)
    private int stock;
}
