package com.java.toystore.domain.mapper;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.domain.model.Toy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ToyMapperImpl.class)
class ToyMapperImplTest {

    @Autowired
    private ToyMapper mapper;
    @Test
    void toResponse_mapsAllFields() {
        Toy toy = new Toy();
        toy.setId(1);
        toy.setSku("BLK-SET-001");
        toy.setName("Building Blocks");
        toy.setCategory("BLOCKS");
        toy.setBrand("LEGO");
        toy.setRecommendedAge("6+");
        toy.setPrice(new BigDecimal("45.99"));
        toy.setStock(10);

        ToyResponse response = mapper.toResponse(toy);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getSku()).isEqualTo("BLK-SET-001");
        assertThat(response.getName()).isEqualTo("Building Blocks");
        assertThat(response.getCategory()).isEqualTo("BLOCKS");
        assertThat(response.getBrand()).isEqualTo("LEGO");
        assertThat(response.getRecommendedAge()).isEqualTo("6+");
        assertThat(response.getPrice()).isEqualByComparingTo("45.99");
        assertThat(response.getStock()).isEqualTo(10);
    }

    @Test
    void toEntity_mapsCreateRequestFields() {
        ToyCreateRequest request = new ToyCreateRequest(
                "DOL-014", "Fashion Doll", "TOYS", "Mattel",
                "Golf", new BigDecimal("9.50"), 300);

        Toy entity = mapper.toEntity(request);

        assertThat(entity.getSku()).isEqualTo("DOL-014");
        assertThat(entity.getName()).isEqualTo("Fashion Doll");
        assertThat(entity.getPrice()).isEqualByComparingTo("9.50");
        assertThat(entity.getStock()).isEqualTo(300);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void updateEntityFromRequest_overwritesMutableFields_leavesIdAndSkuAlone() {
        Toy existing = new Toy();
        existing.setId(7);
        existing.setSku("PLU-BEAR-007");
        existing.setName("Old Name");
        existing.setCategory("OLD");
        existing.setBrand("Old Brand");
        existing.setPrice(new BigDecimal("1.00"));
        existing.setStock(1);

        ToyUpdateRequest request = new ToyUpdateRequest(
                "New Name", "NEW", "New Manu", "New Model", new BigDecimal("99.99"), 50);

        mapper.updateEntityFromRequest(request, existing);

        assertThat(existing.getId()).isEqualTo(7);
        assertThat(existing.getSku()).isEqualTo("PLU-BEAR-007");
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCategory()).isEqualTo("NEW");
        assertThat(existing.getPrice()).isEqualByComparingTo("99.99");
        assertThat(existing.getStock()).isEqualTo(50);
    }
}
