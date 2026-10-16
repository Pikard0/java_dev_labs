package com.java.toystore.service;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.domain.mapper.ToyMapper;
import com.java.toystore.domain.model.Toy;
import com.java.toystore.exception.ToyNotFoundException;
import com.java.toystore.repository.ToyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToyServiceTest {

    @Mock
    private ToyRepository toyRepository;

    @Mock
    private ToyMapper toyMapper;

    @InjectMocks
    private ToyService toyService;

    private Toy toy(int id) {
        Toy toy = new Toy();
        toy.setId(id);
        toy.setSku("SKU-" + id);
        toy.setName("Toy " + id);
        toy.setCategory("TOYS");
        toy.setBrand("LEGO");
        toy.setPrice(new BigDecimal("10.00"));
        toy.setStock(5);
        return toy;
    }

    @Test
    void listAll_mapsEveryToy() {
        Toy p1 = toy(1);
        Toy p2 = toy(2);
        ToyResponse r1 = new ToyResponse();
        ToyResponse r2 = new ToyResponse();
        when(toyRepository.findAll()).thenReturn(List.of(p1, p2));
        when(toyMapper.toResponse(p1)).thenReturn(r1);
        when(toyMapper.toResponse(p2)).thenReturn(r2);

        List<ToyResponse> result = toyService.listAll();

        assertThat(result).containsExactly(r1, r2);
    }

    @Test
    void listAll_noToys_returnsEmptyList() {
        when(toyRepository.findAll()).thenReturn(List.of());

        assertThat(toyService.listAll()).isEmpty();
    }

    @Test
    void getById_toyExists_returnsMappedResponse() {
        Toy toy = toy(1);
        ToyResponse response = new ToyResponse();
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));
        when(toyMapper.toResponse(toy)).thenReturn(response);

        assertThat(toyService.getById(1)).isSameAs(response);
    }

    @Test
    void getById_toyMissing_throwsToyNotFoundException() {
        when(toyRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> toyService.getById(404))
                .isInstanceOf(ToyNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        ToyCreateRequest request = new ToyCreateRequest(
                "SKU-9", "Toy Car", "TOYS", "LEGO", null, new BigDecimal("12.00"), 10);
        Toy entity = toy(9);
        Toy saved = toy(9);
        ToyResponse response = new ToyResponse();
        when(toyMapper.toEntity(request)).thenReturn(entity);
        when(toyRepository.save(entity)).thenReturn(saved);
        when(toyMapper.toResponse(saved)).thenReturn(response);

        assertThat(toyService.create(request)).isSameAs(response);
        verify(toyRepository).save(entity);
    }

    @Test
    void update_toyExists_appliesChangesAndSaves() {
        Toy existing = toy(1);
        ToyUpdateRequest request = new ToyUpdateRequest(
                "Updated Toy", "BLOCKS", "Hasbro", "5+", new BigDecimal("20.00"), 3);
        ToyResponse response = new ToyResponse();
        when(toyRepository.findById(1)).thenReturn(Optional.of(existing));
        when(toyRepository.save(existing)).thenReturn(existing);
        when(toyMapper.toResponse(existing)).thenReturn(response);

        ToyResponse result = toyService.update(1, request);

        verify(toyMapper).updateEntityFromRequest(request, existing);
        assertThat(result).isSameAs(response);
    }

    @Test
    void update_toyMissing_throwsToyNotFoundException_beforeTouchingMapper() {
        when(toyRepository.findById(404)).thenReturn(Optional.empty());

        ToyUpdateRequest request = new ToyUpdateRequest(
                "Name", "CAT", "Manu", null, new BigDecimal("1.00"), 1);

        assertThatThrownBy(() -> toyService.update(404, request))
                .isInstanceOf(ToyNotFoundException.class);

        verifyNoInteractions(toyMapper);
    }

    @Test
    void delete_toyExists_deletesById() {
        when(toyRepository.existsById(1)).thenReturn(true);

        toyService.delete(1);

        verify(toyRepository).deleteById(1);
    }

    @Test
    void delete_toyMissing_throwsToyNotFoundException_andNeverCallsDelete() {
        when(toyRepository.existsById(404)).thenReturn(false);

        assertThatThrownBy(() -> toyService.delete(404))
                .isInstanceOf(ToyNotFoundException.class);

        verify(toyRepository, never()).deleteById(any());
    }
}
