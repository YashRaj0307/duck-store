package com.yash.duckstore.controller;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.service.DuckService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ducks")
public class DuckController {

    private final DuckService duckService;

    public DuckController(DuckService duckService) {
        this.duckService = duckService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Duck addDuck(@RequestBody Duck duck) {
        return duckService.addDuck(duck);
    }

    @GetMapping
    public List<Duck> getAllDucks() {
        return duckService.getAllDucks();
    }
}