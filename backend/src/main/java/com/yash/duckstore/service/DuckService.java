package com.yash.duckstore.service;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.repository.DuckRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DuckService {

    private final DuckRepository duckRepository;

    public DuckService(DuckRepository duckRepository) {
        this.duckRepository = duckRepository;
    }

    public Duck addDuck(Duck duck) {
        return duckRepository.save(duck);
    }

    public List<Duck> getAllDucks() {
        return duckRepository.findAll();
    }
}