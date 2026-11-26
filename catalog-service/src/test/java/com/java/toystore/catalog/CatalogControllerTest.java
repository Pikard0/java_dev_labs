package com.java.toystore.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointConfirmsCatalogServiceIsAvailable() throws Exception {
        mockMvc.perform(get("/api/catalog/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("catalog-service is running"));
    }

    @Test
    void toysEndpointReturnsSharedToyResponseDto() throws Exception {
        mockMvc.perform(get("/api/catalog/toys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("TOY-001"))
                .andExpect(jsonPath("$[1].name").value("Building blocks"));
    }
}
