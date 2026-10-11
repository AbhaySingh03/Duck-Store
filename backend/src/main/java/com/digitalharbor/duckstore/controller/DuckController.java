package com.digitalharbor.duckstore.controller;

import com.digitalharbor.duckstore.dto.DuckRequest;
import com.digitalharbor.duckstore.dto.DuckResponse;
import com.digitalharbor.duckstore.dto.DuckUpdateRequest;
import com.digitalharbor.duckstore.service.AddResult;
import com.digitalharbor.duckstore.service.DuckService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ducks")
public class DuckController {

    private final DuckService service;

    public DuckController(DuckService service) {
        this.service = service;
    }

    /** GET /api/ducks?sort=asc|desc  (sorted by quantity, default highest stock first) */
    @GetMapping
    public List<DuckResponse> list(@RequestParam(defaultValue = "desc") String sort) {
        return service.list(Sort.Direction.fromString(sort)).stream().map(DuckResponse::from).toList();
    }

    /** 201 when a new record is created, 200 when the quantity was merged into an existing duck. */
    @PostMapping
    public ResponseEntity<DuckResponse> add(@Valid @RequestBody DuckRequest request) {
        AddResult result = service.add(request.color(), request.size(), request.price(), request.quantity());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(DuckResponse.from(result.duck()));
    }

    @PutMapping("/{id}")
    public DuckResponse update(@PathVariable int id, @Valid @RequestBody DuckUpdateRequest request) {
        return DuckResponse.from(service.update(id, request.price(), request.quantity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}
