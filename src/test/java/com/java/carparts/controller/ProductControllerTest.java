package com.java.carparts.controller;

import com.java.carparts.domain.dto.ProductCreateRequest;
import com.java.carparts.domain.dto.ProductResponse;
import com.java.carparts.domain.dto.ProductUpdateRequest;
import com.java.carparts.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private ProductController controller() {
        return new ProductController(productService);
    }

    @Test
    void listAll_returnsWhatServiceReturns() {
        List<ProductResponse> expected = List.of(new ProductResponse());
        when(productService.listAll()).thenReturn(expected);

        assertThat(controller().listAll()).isSameAs(expected);
    }

    @Test
    void getById_delegatesWithGivenId() {
        ProductResponse expected = new ProductResponse();
        when(productService.getById(7)).thenReturn(expected);

        assertThat(controller().getById(7)).isSameAs(expected);
    }

    @Test
    void create_delegatesRequestToService() {
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU", "Name", "CAT", "Manu", null, new BigDecimal("1.00"), 1);
        ProductResponse expected = new ProductResponse();
        when(productService.create(request)).thenReturn(expected);

        assertThat(controller().create(request)).isSameAs(expected);
    }

    @Test
    void update_delegatesIdAndRequestToService() {
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Name", "CAT", "Manu", null, new BigDecimal("1.00"), 1);
        ProductResponse expected = new ProductResponse();
        when(productService.update(3, request)).thenReturn(expected);

        assertThat(controller().update(3, request)).isSameAs(expected);
    }

    @Test
    void delete_delegatesIdToService() {
        controller().delete(5);

        verify(productService).delete(5);
    }
}
