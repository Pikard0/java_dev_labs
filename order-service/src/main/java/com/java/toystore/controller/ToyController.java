package com.java.toystore.controller;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.service.ToyService;
import com.java.toystore.validation.AdminEndpoint;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/toys")
@RequiredArgsConstructor
public class ToyController {

    private final ToyService toyService;

    @GetMapping
    public List<ToyResponse> listAll() {
        return toyService.listAll();
    }

    @GetMapping("/{id}")
    public ToyResponse getById(@PathVariable Integer id) {
        return toyService.getById(id);
    }

    @PostMapping
    @AdminEndpoint
    @ResponseStatus(HttpStatus.CREATED)
    public ToyResponse create(@Valid @RequestBody ToyCreateRequest request) {
        return toyService.create(request);
    }

    @PutMapping("/{id}")
    @AdminEndpoint
    public ToyResponse update(@PathVariable Integer id, @Valid @RequestBody ToyUpdateRequest request) {
        return toyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @AdminEndpoint
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        toyService.delete(id);
    }
}
