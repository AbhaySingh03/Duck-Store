package com.digitalharbor.duckstore.controller;

import com.digitalharbor.duckstore.dto.OrderRequest;
import com.digitalharbor.duckstore.dto.OrderResponse;
import com.digitalharbor.duckstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderResponse place(@Valid @RequestBody OrderRequest request) {
        return service.place(request);
    }
}
