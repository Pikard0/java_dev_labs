package com.java.toystore.controller;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.service.ToyService;
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
class ToyControllerTest {

    @Mock
    private ToyService toyService;

    private ToyController controller() {
        return new ToyController(toyService);
    }

    @Test
    void listAll_returnsWhatServiceReturns() {
        List<ToyResponse> expected = List.of(new ToyResponse());
        when(toyService.listAll()).thenReturn(expected);

        assertThat(controller().listAll()).isSameAs(expected);
    }

    @Test
    void getById_delegatesWithGivenId() {
        ToyResponse expected = new ToyResponse();
        when(toyService.getById(7)).thenReturn(expected);

        assertThat(controller().getById(7)).isSameAs(expected);
    }

    @Test
    void create_delegatesRequestToService() {
        ToyCreateRequest request = new ToyCreateRequest(
                "SKU", "Name", "CAT", "Manu", null, new BigDecimal("1.00"), 1);
        ToyResponse expected = new ToyResponse();
        when(toyService.create(request)).thenReturn(expected);

        assertThat(controller().create(request)).isSameAs(expected);
    }

    @Test
    void update_delegatesIdAndRequestToService() {
        ToyUpdateRequest request = new ToyUpdateRequest(
                "Name", "CAT", "Manu", null, new BigDecimal("1.00"), 1);
        ToyResponse expected = new ToyResponse();
        when(toyService.update(3, request)).thenReturn(expected);

        assertThat(controller().update(3, request)).isSameAs(expected);
    }

    @Test
    void delete_delegatesIdToService() {
        controller().delete(5);

        verify(toyService).delete(5);
    }
}
