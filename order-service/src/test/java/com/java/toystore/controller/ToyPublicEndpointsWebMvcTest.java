package com.java.toystore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.exception.ToyNotFoundException;
import com.java.toystore.service.ToyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ToyController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ToyPublicEndpointsWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ToyService toyService;

    @Test
    void getToys_returnsJsonArray() throws Exception {
        ToyResponse response = new ToyResponse();
        response.setId(1);
        response.setName("Building Blocks");
        response.setPrice(new BigDecimal("45.99"));
        when(toyService.listAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/toys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Building Blocks"))
                .andExpect(jsonPath("$[0].price").value(45.99));
    }

    @Test
    void getToyById_notFound_returns404WithErrorBody() throws Exception {
        when(toyService.getById(404)).thenThrow(new ToyNotFoundException(404));

        mockMvc.perform(get("/api/toys/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Toy not found: 404"));
    }

    @Test
    void createToy_missingRequiredFields_returns400_serviceNeverCalled() throws Exception {
        ToyCreateRequest invalid = new ToyCreateRequest(
                "", "", "", "", null, null, 0);

        mockMvc.perform(post("/api/toys")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createToy_validRequest_returns201() throws Exception {
        ToyCreateRequest valid = new ToyCreateRequest(
                "SKU-1", "Toy Car", "TOYS", "LEGO", null, new BigDecimal("10.00"), 5);
        ToyResponse response = new ToyResponse();
        response.setId(1);
        when(toyService.create(any(ToyCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/toys")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isCreated());
    }
}
