package com.study.order.controller;

import com.study.order.dto.OrderRequest;
import com.study.order.dto.OrderResponse;
import com.study.order.exception.PaymentDeclinedException;
import com.study.order.service.OrderOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderOrchestrator orderOrchestrator;

	@PostMapping
	public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
		try {
			OrderResponse response = orderOrchestrator.createOrder(request);
			return ResponseEntity.ok(response);
		} catch (PaymentDeclinedException e) {
			return ResponseEntity.badRequest().body("결제 거부: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("주문 실패: " + e.getMessage());
		}
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable int orderId) {
		return ResponseEntity.ok(orderOrchestrator.getOrder(orderId));
	}
}