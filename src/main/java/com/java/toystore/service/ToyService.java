package com.java.toystore.service;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.aop.MeasureExecutionTime;
import com.java.toystore.aop.TrackExecution;
import com.java.toystore.domain.mapper.ToyMapper;
import com.java.toystore.domain.model.Toy;
import com.java.toystore.exception.ToyNotFoundException;
import com.java.toystore.repository.ToyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToyService {

    private final ToyRepository toyRepository;
    private final ToyMapper toyMapper;

    @Transactional(readOnly = true)
    @MeasureExecutionTime
    public List<ToyResponse> listAll() {
        return toyRepository.findAll().stream()
                .map(toyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ToyResponse getById(Integer id) {
        return toyMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    @TrackExecution
    public ToyResponse create(ToyCreateRequest request) {
        Toy toy = toyMapper.toEntity(request);
        return toyMapper.toResponse(toyRepository.save(toy));
    }

    @Transactional
    public ToyResponse update(Integer id, ToyUpdateRequest request) {
        Toy toy = findOrThrow(id);
        toyMapper.updateEntityFromRequest(request, toy);
        return toyMapper.toResponse(toyRepository.save(toy));
    }

    @Transactional
    public void delete(Integer id) {
        if (!toyRepository.existsById(id)) {
            throw new ToyNotFoundException(id);
        }
        toyRepository.deleteById(id);
    }

    private Toy findOrThrow(Integer id) {
        return toyRepository.findById(id)
                .orElseThrow(() -> new ToyNotFoundException(id));
    }
}
