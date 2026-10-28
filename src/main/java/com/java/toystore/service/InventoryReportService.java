package com.java.toystore.service;

import com.java.toystore.domain.model.Toy;
import com.java.toystore.repository.ToyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReportService {

    private final ToyRepository toyRepository;

    @Transactional(readOnly = true)
    public int publishLowStockReport(int minimumStock, String reportName) {
        List<Toy> lowStockToys = toyRepository.findByStockLessThanEqualOrderByStockAsc(minimumStock);
        log.info("Report '{}' contains {} toys with stock at or below {}", reportName, lowStockToys.size(), minimumStock);
        return lowStockToys.size();
    }
}
