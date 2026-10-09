package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.ProductCreateRequest;
import com.java.carparts.domain.dto.ProductResponse;
import com.java.carparts.domain.dto.ProductUpdateRequest;
import com.java.carparts.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProductMapperImpl.class)
class ProductMapperImplTest {

    @Autowired
    private ProductMapper mapper;
    @Test
    void toResponse_mapsAllFields() {
        Product product = new Product();
        product.setId(1);
        product.setSku("BRK-PAD-001");
        product.setName("Brake Pad");
        product.setCategory("BRAKES");
        product.setManufacturer("Bosch");
        product.setCompatibleModel("Corolla");
        product.setPrice(new BigDecimal("45.99"));
        product.setStock(10);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getSku()).isEqualTo("BRK-PAD-001");
        assertThat(response.getName()).isEqualTo("Brake Pad");
        assertThat(response.getCategory()).isEqualTo("BRAKES");
        assertThat(response.getManufacturer()).isEqualTo("Bosch");
        assertThat(response.getCompatibleModel()).isEqualTo("Corolla");
        assertThat(response.getPrice()).isEqualByComparingTo("45.99");
        assertThat(response.getStock()).isEqualTo(10);
    }

    @Test
    void toEntity_mapsCreateRequestFields() {
        ProductCreateRequest request = new ProductCreateRequest(
                "OIL-FLT-014", "Oil Filter", "ENGINE", "Mann-Filter",
                "Golf", new BigDecimal("9.50"), 300);

        Product entity = mapper.toEntity(request);

        assertThat(entity.getSku()).isEqualTo("OIL-FLT-014");
        assertThat(entity.getName()).isEqualTo("Oil Filter");
        assertThat(entity.getPrice()).isEqualByComparingTo("9.50");
        assertThat(entity.getStock()).isEqualTo(300);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void updateEntityFromRequest_overwritesMutableFields_leavesIdAndSkuAlone() {
        Product existing = new Product();
        existing.setId(7);
        existing.setSku("SPK-PLG-007");
        existing.setName("Old Name");
        existing.setCategory("OLD");
        existing.setManufacturer("Old Manu");
        existing.setPrice(new BigDecimal("1.00"));
        existing.setStock(1);

        ProductUpdateRequest request = new ProductUpdateRequest(
                "New Name", "NEW", "New Manu", "New Model", new BigDecimal("99.99"), 50);

        mapper.updateEntityFromRequest(request, existing);

        assertThat(existing.getId()).isEqualTo(7);
        assertThat(existing.getSku()).isEqualTo("SPK-PLG-007");
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCategory()).isEqualTo("NEW");
        assertThat(existing.getPrice()).isEqualByComparingTo("99.99");
        assertThat(existing.getStock()).isEqualTo(50);
    }
}
