package com.yash.duckstore.controller;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.dto.DuckCreateRequest;
import com.yash.duckstore.dto.DuckResponse;
import com.yash.duckstore.dto.DuckUpdateRequest;
import com.yash.duckstore.service.DuckService;
import jakarta.validation.Valid;
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
    public DuckResponse addDuck(@Valid @RequestBody DuckCreateRequest request) {
        return DuckResponse.from(duckService.addDuck(request));
    }

    @GetMapping
    public List<DuckResponse> getAllDucks() {
        return duckService.getAllDucks().stream().map(DuckResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DuckResponse getDuck(@PathVariable long id) {
        return DuckResponse.from(duckService.getDuck(id));
    }

    @PutMapping("/{id}")
    public DuckResponse updateDuck(
            @PathVariable long id,
            @Valid @RequestBody DuckUpdateRequest request) {
        return DuckResponse.from(duckService.updateDuck(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDuck(@PathVariable long id) {
        duckService.deleteDuck(id);
    }
}
