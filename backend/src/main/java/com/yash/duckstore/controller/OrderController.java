package com.yash.duckstore.controller;

import com.yash.duckstore.dto.OrderRequest;
import com.yash.duckstore.dto.OrderResponse;
import com.yash.duckstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse priceOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.priceOrder(request);
    }
}
