package com.yash.duckstore.service;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.dto.DuckCreateRequest;
import com.yash.duckstore.dto.DuckUpdateRequest;
import com.yash.duckstore.exception.BusinessConflictException;
import com.yash.duckstore.exception.ResourceNotFoundException;
import com.yash.duckstore.repository.DuckRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DuckService {

    private final DuckRepository duckRepository;

    public DuckService(DuckRepository duckRepository) {
        this.duckRepository = duckRepository;
    }

    @Transactional
    public Duck addDuck(DuckCreateRequest request) {
        duckRepository.upsertAndMergeQuantity(
                request.color().name(),
                request.size().name(),
                request.price(),
                request.quantity());

        // The PostgreSQL upsert atomically creates or increments the matching active row.
        return duckRepository.findByColorAndSizeAndPriceAndDeletedFalse(
                        request.color(), request.size(), request.price())
                .orElseThrow(() -> new IllegalStateException(
                        "Duck was written but could not be read back"));
    }

    @Transactional(readOnly = true)
    public List<Duck> getAllDucks() {
        return duckRepository.findAllByDeletedFalseOrderByQuantityAscIdAsc();
    }

    @Transactional(readOnly = true)
    public Duck getDuck(long id) {
        return duckRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active duck not found: " + id));
    }

    @Transactional
    public Duck updateDuck(long id, DuckUpdateRequest request) {
        Duck duck = getDuck(id);

        if (duckRepository.existsByColorAndSizeAndPriceAndDeletedFalseAndIdNot(
                duck.getColor(), duck.getSize(), request.price(), id)) {
            throw new BusinessConflictException(
                    "An active duck with the same color, size, and price already exists");
        }

        duck.updatePriceAndQuantity(request.price(), request.quantity());
        try {
            return duckRepository.saveAndFlush(duck);
        } catch (DataIntegrityViolationException ex) {
            // Protect against a concurrent request creating the same unique combination.
            throw new BusinessConflictException(
                    "The update conflicts with an existing active duck");
        }
    }

    @Transactional
    public void deleteDuck(long id) {
        Duck duck = getDuck(id);
        duck.markDeleted();
        duckRepository.save(duck);
    }
}
