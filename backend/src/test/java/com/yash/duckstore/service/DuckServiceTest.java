package com.yash.duckstore.service;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.dto.DuckCreateRequest;
import com.yash.duckstore.dto.DuckUpdateRequest;
import com.yash.duckstore.exception.BusinessConflictException;
import com.yash.duckstore.exception.ResourceNotFoundException;
import com.yash.duckstore.repository.DuckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuckServiceTest {

    @Mock
    DuckRepository duckRepository;

    @InjectMocks
    DuckService duckService;

    @Test
    void addDuckUsesAtomicUpsertAndReturnsMergedActiveRow() {
        var request = new DuckCreateRequest(DuckColor.RED, DuckSize.XLARGE,
                new BigDecimal("12.50"), 3);
        Duck stored = new Duck(DuckColor.RED, DuckSize.XLARGE,
                new BigDecimal("12.50"), 8);
        when(duckRepository.findByColorAndSizeAndPriceAndDeletedFalse(
                DuckColor.RED, DuckSize.XLARGE, new BigDecimal("12.50")))
                .thenReturn(Optional.of(stored));

        Duck result = duckService.addDuck(request);

        verify(duckRepository).upsertAndMergeQuantity("RED", "XLARGE",
                new BigDecimal("12.50"), 3);
        assertEquals(8, result.getQuantity());
    }

    @Test
    void updateRejectsDuplicateActivePriceCombination() {
        Duck existing = new Duck(DuckColor.RED, DuckSize.MEDIUM,
                new BigDecimal("10.00"), 5);
        when(duckRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(duckRepository.existsByColorAndSizeAndPriceAndDeletedFalseAndIdNot(
                DuckColor.RED, DuckSize.MEDIUM, new BigDecimal("11.00"), 1L)).thenReturn(true);

        assertThrows(BusinessConflictException.class, () -> duckService.updateDuck(
                1L, new DuckUpdateRequest(new BigDecimal("11.00"), 7)));
        verify(duckRepository, never()).saveAndFlush(any());
    }

    @Test
    void getDuckRejectsMissingOrSoftDeletedRows() {
        when(duckRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> duckService.getDuck(999L));
    }
}
